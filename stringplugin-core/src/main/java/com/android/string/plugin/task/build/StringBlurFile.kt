package com.android.string.plugin.task.build

import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.BaseFile
import com.android.string.plugin.util.KeyDerivation
import com.android.string.plugin.util.ModeUtils
import com.squareup.javawriter.JavaWriter
import javax.lang.model.element.Modifier

/**
 * 生成解密调用类。
 * 类名与解密方法名由[com.android.string.plugin.util.EntryNames]按配置派生，
 * 不再使用固定的 StringBlur/decrypt。
 * 调用点只带每个字符串自己的 tag，key 在运行时由 [master] 与 tag 重建，
 * 重建逻辑与 [KeyDerivation.derive] 逐行对应。
 * @author chancey
 * @date   2023/9/5   17:24
 **/
class StringBlurFile(private val master: IntArray) : BaseFile() {

    fun create(path: java.io.File, applicationId: String, modes: List<Mode>, className: String, methodName: String) {
        val file = java.io.File(path, "$className.java")
        JavaWriter(java.io.FileWriter(file)).use {
            write(it, applicationId, modes, className, methodName)
        }
    }

    override fun create(path: java.io.File, applicationId: String, modes: List<Mode>) {
        create(path, applicationId, modes, Constant.PLUGIN_CLASS_NAME, "decrypt")
    }

    override fun write(writer: JavaWriter, applicationId: String, mode: Mode) {
        write(writer, applicationId, listOf(mode), Constant.PLUGIN_CLASS_NAME, "decrypt")
    }

    private fun write(writer: JavaWriter, applicationId: String, modes: List<Mode>, className: String, methodName: String) {
        val pkg = Constant.PLUGIN_CLASS_PACKAGE.format(applicationId)
        val imports = modes.map { ModeUtils.getEncodeImplClassFilePath(it, applicationId) }
        writer.emitPackage(pkg)
            .emitImports(imports)
            .beginType(
                className,
                "class",
                mutableSetOf(Modifier.PUBLIC, Modifier.FINAL),
            )
        modes.forEachIndexed { index, currentMode ->
            val implName = ModeUtils.getEncodeImplClassName(currentMode)
            writer.emitField(
                implName,
                "IMPL_$index",
                mutableSetOf(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
                "new $implName()"
            )
        }
        // 主密钥材料不再以 new int[]{...} 字面量出现（那是最扎眼的静态单点，
        // 而且简单的 ^mask 会被 javac/R8 常量折叠回原值）。
        // 改为编码成字符串常量，运行时用带下标的循环解回：循环 + charAt + Character.digit
        // 不会被编译器/优化器折叠成明文数组，静态扫描也搜不到可识别的密钥数组。
        val salt = masterSalt(master)
        writer.emitField(
            "String",
            "D",
            mutableSetOf(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
            "\"" + encodeMaster(master, salt) + "\""
        )
        writer.emitField(
            "int[]",
            "M",
            mutableSetOf(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
            "u()"
        )
        writer
            .beginMethod(
                String::class.java.simpleName,
                methodName,
                mutableSetOf(Modifier.PUBLIC, Modifier.STATIC),
                String::class.java.simpleName,
                "value",
                "int",
                "tag",
                "int",
                "mode"
            )
        emitDecrypt(writer, modes, "decryptString(value, k(tag))")
        writer
            .endMethod()
            .beginMethod(
                String::class.java.simpleName,
                methodName,
                mutableSetOf(Modifier.PUBLIC, Modifier.STATIC),
                ByteArray::class.java.simpleName,
                "value",
                "int",
                "tag",
                "int",
                "mode"
            )
        emitDecrypt(writer, modes, "decryptBytes(value, k(tag).getBytes(java.nio.charset.StandardCharsets.UTF_8))")
        writer.endMethod()
        writeKeyMethod(writer)
        writeUnpackMethod(writer, master.size, masterSalt(master))
        writer.endType()
    }

    /** 运行时把编码字符串 [D] 解回主密钥材料。与 [encodeMaster] 互逆，逐位对应。 */
    private fun writeUnpackMethod(writer: JavaWriter, size: Int, salt: Int) {
        writer
            .beginMethod(
                "int[]",
                "u",
                mutableSetOf(Modifier.PRIVATE, Modifier.STATIC)
            )
            .emitStatement("int[] m = new int[$size]")
            .beginControlFlow("for (int i = 0; i < $size; i++)")
            .emitStatement("int x = 0")
            .beginControlFlow("for (int j = 0; j < 4; j++)")
            .emitStatement("int p = (i * 4 + j) * 2")
            .emitStatement(
                "int bb = ((Character.digit(D.charAt(p), 16) << 4) | " +
                    "Character.digit(D.charAt(p + 1), 16)) ^ (($salt >>> (j * 8)) & 0xff)"
            )
            .emitStatement("x = (x << 8) | (bb & 0xff)")
            .endControlFlow()
            .emitStatement("m[i] = x")
            .endControlFlow()
            .emitStatement("return m")
            .endMethod()
    }

    companion object {
        /** 由主密钥材料稳定派生的掩码（配置不变则不变），仅用于让编码串不等于原始摘要 */
        internal fun masterSalt(master: IntArray): Int {
            var s = 0x5bd1e995.toInt()
            for (m in master) s = s xor m
            return s
        }

        /**
         * 把主密钥材料编码为十六进制字符串：每个 int 取大端 4 字节，逐字节异或 [salt] 的对应字节。
         * 与运行时 u()（见 [writeUnpackMethod]）逐位互逆，由 KeyDerivationRoundTripTest 校验。
         */
        internal fun encodeMaster(master: IntArray, salt: Int): String {
            val sb = StringBuilder(master.size * 8)
            for (m in master) {
                for (j in 0 until 4) {
                    val raw = (m ushr ((3 - j) * 8)) and 0xff
                    val masked = raw xor ((salt ushr (j * 8)) and 0xff)
                    sb.append("0123456789abcdef"[(masked ushr 4) and 0xf])
                    sb.append("0123456789abcdef"[masked and 0xf])
                }
            }
            return sb.toString()
        }
    }

    /** 与 [KeyDerivation.derive] 逐行对应 */
    private fun writeKeyMethod(writer: JavaWriter) {
        writer
            .beginMethod(
                String::class.java.simpleName,
                "k",
                mutableSetOf(Modifier.PRIVATE, Modifier.STATIC),
                "int",
                "tag"
            )
            .emitStatement("long h = tag")
            .beginControlFlow("for (int i = 0; i < M.length; i++)")
            .emitStatement("h = h * ${KeyDerivation.MULTIPLIER}L + M[i]")
            .endControlFlow()
            .beginControlFlow("if (h == 0L)")
            .emitStatement("h = 1L")
            .endControlFlow()
            .emitStatement("char[] c = new char[${KeyDerivation.KEY_LENGTH}]")
            .beginControlFlow("for (int i = 0; i < ${KeyDerivation.KEY_LENGTH}; i++)")
            .emitStatement("h ^= h << 13")
            .emitStatement("h ^= h >>> 7")
            .emitStatement("h ^= h << 17")
            .emitStatement("c[i] = \"${KeyDerivation.ALPHABET}\".charAt((int) ((h >>> 33) %% ${KeyDerivation.ALPHABET.length}))")
            .endControlFlow()
            .emitStatement("return new String(c)")
            .endMethod()
    }

    private fun emitDecrypt(writer: JavaWriter, modes: List<Mode>, call: String) {
        if (modes.size == 1) {
            writer.emitStatement("return IMPL_0.$call")
            return
        }
        writer.beginControlFlow("switch (mode)")
        modes.indices.drop(1).forEach { index ->
            writer.emitStatement("case $index: return IMPL_$index.$call")
        }
        writer.emitStatement("default: return IMPL_0.$call")
        writer.endControlFlow()
    }

    override fun getFileName(applicationId: String) = "${Constant.PLUGIN_CLASS_NAME}.java"
}
