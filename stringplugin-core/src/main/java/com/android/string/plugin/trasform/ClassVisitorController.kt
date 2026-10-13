package com.android.string.plugin.trasform

import com.android.string.plugin.field.StringFiled
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.report.StringBlurReport
import com.android.string.plugin.trasform.visitor.ClinitMethodVisitor
import com.android.string.plugin.trasform.visitor.InitMethodVisitor
import com.android.string.plugin.trasform.visitor.NormalMethodVisitor
import com.android.string.plugin.util.AsmWriter
import com.android.string.plugin.util.KeyDerivation
import com.android.string.plugin.util.ModeUtils
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import kotlin.random.Random

/**
 * @author chancey
 * @date   2023/9/6   22:54
 **/
class ClassVisitorController(
    private val wrapperClass: String,
    private val wrapperMethod: String,
    private val master: IntArray,
    private val bytesMode: BytesMode,
    private val modes: List<Mode>,
    private val report: StringBlurReport?,
    private val minLength: Int
) {
    private val asmWriter = AsmWriter(wrapperClass, wrapperMethod)
    internal var bytesMethodWriter: BytesMethodWriter? = null
    internal val reservedMethodNames = mutableSetOf<String>()
    internal fun prepareBytesMethods(isInterface: Boolean, version: Int) {
        // Static interface methods require Java 8; older interfaces use STRING.
        if (!isInterface || (version and 0xffff) >= Opcodes.V1_8) {
            bytesMethodWriter = BytesMethodWriter(currentClassName!!, isInterface, asmWriter, reservedMethodNames)
        }
    }
    // 每个类用各自的种子，避免不同类里第 n 个字符串得到相同的 tag
    private var random = Random(master.contentHashCode())
    var currentClassName: String? = null
        set(value) {
            field = value
            random = Random(master.contentHashCode() * 31 + (value?.hashCode() ?: 0))
        }

    // 类级 @KeepString / @EncryptString（由 StringBlurClassVisitor.visitAnnotation 设置）
    var classKeep: Boolean = false
    var classEncrypt: Boolean = false

    val staticFinalFields = mutableListOf<StringFiled>()
    val staticFields = mutableListOf<StringFiled>()
    val finalFields = mutableListOf<StringFiled>()
    private val fields = mutableListOf<StringFiled>()
    private var isClInitExists = false

    fun markFieldAnnotation(name: String, keep: Boolean, force: Boolean) {
        (staticFinalFields + staticFields + finalFields + fields)
            .filter { it.name == name }
            .forEach {
                if (keep) {
                    it.keep = true
                }
                if (force) {
                    it.force = true
                }
            }
    }

    fun isKeepStaticField(name: String?): Boolean {
        if (name == null) {
            return false
        }
        return staticFields.any { it.name == name && it.keep } ||
                staticFinalFields.any { it.name == name && it.keep }
    }

    fun isForceStaticField(name: String?): Boolean {
        if (name == null) {
            return false
        }
        return staticFields.any { it.name == name && it.force } ||
                staticFinalFields.any { it.name == name && it.force }
    }

    fun isKeepInstanceField(name: String?): Boolean {
        if (name == null) {
            return false
        }
        return finalFields.any { it.name == name && it.keep } ||
                fields.any { it.name == name && it.keep }
    }

    fun visitField(access: Int, name: String?, desc: String?, value: String?) {
        if (classKeep || name.isNullOrBlank() || desc != StringFiled.DESC) {
            return
        }
        val isStatic = (access and Opcodes.ACC_STATIC) != 0
        val isFinal = (access and Opcodes.ACC_FINAL) != 0
        val field = StringFiled(name, value)
        when {
            // static final, in this condition, the value is null or not null.
            isStatic && isFinal -> staticFinalFields += field
            // static, in this condition, the value is null.
            isStatic && !isFinal -> staticFields += field
            // final, in this condition, the value is null or not null.
            !isStatic && isFinal -> finalFields += field
            // normal, in this condition, the value is null.
            else -> fields += field
        }
    }

    fun visitEnd(mv: MethodVisitor) {
        mv.visitCode()
        // Here init static final fields.
        staticFinalFields.forEach {
            if (it.keep) {
                return@forEach
            }
            if (!it.force && !shouldEncrypt(it.value)) {
                return@forEach
            }
            write(it.value, mv)
            mv.visitFieldInsn(Opcodes.PUTSTATIC, currentClassName, it.name, StringFiled.DESC)
        }
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(1, 0)
        mv.visitEnd()
    }

    fun isVisitClInitMethod(): Boolean {
        return !isClInitExists && staticFinalFields.any { !it.keep }
    }

    fun visitMethod(
        access: Int,
        mv: MethodVisitor,
        name: String?,
        maxLocals: Int = 0
    ): MethodVisitor {
        return when (name) {
            // If clinit exists meaning the static fields (not final) would have be inited here.
            "<clinit>" -> {
                isClInitExists = true
                ClinitMethodVisitor(mv, this, name, maxLocals)
            }
            // Here init final(not static) and normal fields
            "<init>" -> InitMethodVisitor(mv, this, name, maxLocals)
            else -> NormalMethodVisitor(mv, this, name, maxLocals)
        }
    }


    fun shouldEncrypt(data: String?): Boolean {
        return data != null && ModeUtils.getEncodeImpl(modes.first())
            .hasContent(data.toByteArray()) && data.length >= minLength
    }

    fun reportEncrypted(
        methodName: String?,
        data: String?,
        mode: Mode,
        selectedBytesMode: BytesMode
    ) {
        report?.let {
            it.encrypted(
                currentClassName,
                methodName,
                data,
                mode.name,
                selectedBytesMode.name
            )
        }
    }

    fun reportIgnored(methodName: String?, value: Any?, reason: String) {
        report?.let {
            it.ignored(
                currentClassName,
                methodName,
                value,
                reason
            )
        }
    }

    fun reportIgnoredLdc(methodName: String?, value: Any?) {
        if (value is String && !ModeUtils.getEncodeImpl(modes.first())
                .hasContent(value.toByteArray())
        ) {
            reportIgnored(methodName, value, "emptyString")
        } else if (value is String && value.length < minLength) {
            reportIgnored(methodName, value, "tooShort")
        } else if (value !is String) {
            reportIgnored(methodName, value, "notStringLdc")
        }
    }

    fun write(data: String?, mv: MethodVisitor, methodName: String? = null) {
        val modeIndex = selectModeIndex()
        val mode = modes[modeIndex]
        val selectedBytesMode = selectBytesMode()
        val stringBlurWrapper = ModeUtils.getEncodeImpl(mode)
        reportEncrypted(methodName, data, mode, selectedBytesMode)
        if (selectedBytesMode == BytesMode.BYTES) {
            writeByBytes(data, stringBlurWrapper, modeIndex, mv)
        } else {
            writeByString(data, stringBlurWrapper, modeIndex, mv)
        }
    }

    private fun selectBytesMode(): BytesMode {
        if (bytesMethodWriter == null) return BytesMode.STRING
        return when (bytesMode) {
            BytesMode.STRING -> BytesMode.STRING
            BytesMode.BYTES -> BytesMode.BYTES
            BytesMode.RANDOM -> if (random.nextBoolean()) BytesMode.BYTES else BytesMode.STRING
        }
    }

    private fun selectModeIndex(): Int {
        if (modes.size == 1) {
            return 0
        }

        return random.nextInt(modes.size)
    }

    private fun writeByString(
        data: String?,
        stringBlurWrapper: com.android.string.plugin.IString,
        modeIndex: Int,
        mv: MethodVisitor
    ) {
        val tag = random.nextInt()
        val encodeText = stringBlurWrapper.encryptString(data, KeyDerivation.derive(master, tag))
        asmWriter.write(encodeText, tag, modeIndex, mv)
    }

    private fun writeByBytes(
        data: String?,
        stringBlurWrapper: com.android.string.plugin.IString,
        modeIndex: Int,
        mv: MethodVisitor
    ) {
        val tag = random.nextInt()
        val encodeText = stringBlurWrapper.encryptBytes(data, KeyDerivation.derive(master, tag))
        bytesMethodWriter!!.write(encodeText, tag, modeIndex, mv)
    }
}
