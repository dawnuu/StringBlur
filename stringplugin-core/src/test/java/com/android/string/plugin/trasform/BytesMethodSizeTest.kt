package com.android.string.plugin.trasform

import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.util.KeyDerivation
import com.android.string.plugin.util.ModeUtils
import org.junit.Assert.*
import org.junit.Test
import org.objectweb.asm.*
import org.objectweb.asm.tree.ClassNode

class BytesMethodSizeTest {
    @Test
    fun manyStringsDoNotExpandIntoTheOriginalMethod() {
        for (mode in listOf(BytesMode.BYTES, BytesMode.RANDOM)) {
            val value = "SDK configuration 中文🙂".repeat(8)
            val bytes = transform(sample(value, count = 300), mode)
            val node = ClassNode().also { ClassReader(bytes).accept(it, 0) }
            assertTrue(node.methods.any { it.access and Opcodes.ACC_SYNTHETIC != 0 })
            assertFalse(node.methods.single { it.name == "value" }.instructions.any {
                it.opcode == Opcodes.NEWARRAY || it.opcode == Opcodes.BASTORE
            })
            assertEquals(value, invoke(bytes))
        }
    }

    @Test
    fun longCiphertextIsSplitAndStillDecrypts() {
        // Base64 ciphertext spans 64,000 bytes, including indices above SIPUSH range.
        val value = "x".repeat(48_000)
        val bytes = transform(sample(value), BytesMode.BYTES)
        val node = ClassNode().also { ClassReader(bytes).accept(it, 0) }
        assertTrue(node.methods.count { it.desc == "([B)V" } > 1)
        assertEquals(value, invoke(bytes))
    }

    @Test
    fun methodAlreadyNear64KbRemainsValid() {
        val value = "large-sdk-literal".repeat(100)
        assertEquals(value, invoke(transform(sample(value, padding = 65_000), BytesMode.BYTES)))
    }

    @Test
    fun helpersAvoidOriginalNamesAndSupportJava8Interfaces() {
        for (isInterface in listOf(false, true)) {
            val bytes = transform(sample("interface-or-class", isInterface = isInterface), BytesMode.BYTES)
            assertEquals("interface-or-class", invoke(bytes))
            val node = ClassNode().also { ClassReader(bytes).accept(it, 0) }
            assertEquals(node.methods.size, node.methods.map { it.name to it.desc }.toSet().size)
            assertTrue(node.methods.any { it.name == "\$stringBlur\$bytes\$1" })
        }
    }

    @Test
    fun staticFieldInitializationAlsoUsesBoundedHelpers() {
        for (version in listOf(Opcodes.V1_7, Opcodes.V1_8)) {
            val value = if (version == Opcodes.V1_7) "legacy-interface" else "x".repeat(48_000)
            val writer = ClassWriter(0)
            writer.visit(version, Opcodes.ACC_PUBLIC or Opcodes.ACC_INTERFACE or Opcodes.ACC_ABSTRACT,
                "test/BytesSample", null, "java/lang/Object", null)
            writer.visitField(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL,
                "VALUE", "Ljava/lang/String;", null, value).visitEnd()
            writer.visitEnd()
            val bytes = transform(writer.toByteArray(), BytesMode.BYTES)
            assertEquals(value, load(bytes).getField("VALUE").get(null))
        }
    }

    private fun sample(value: String, count: Int = 1, padding: Int = 0, isInterface: Boolean = false): ByteArray {
        val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC or
            (if (isInterface) Opcodes.ACC_INTERFACE or Opcodes.ACC_ABSTRACT else 0),
            "test/BytesSample", null, "java/lang/Object", null)
        val method = writer.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, "value", "()Ljava/lang/String;", null, null)
        method.visitCode()
        repeat(padding) { method.visitInsn(Opcodes.NOP) }
        repeat(count) {
            method.visitLdcInsn(value)
            if (it != count - 1) method.visitInsn(Opcodes.POP)
        }
        method.visitInsn(Opcodes.ARETURN)
        method.visitMaxs(0, 0)
        method.visitEnd()
        // Declared after the transformed method to exercise reservation of future names.
        val collision = writer.visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
            "\$stringBlur\$bytes\$0", "()Ljava/lang/String;", null, null)
        collision.visitCode()
        collision.visitInsn(Opcodes.ACONST_NULL)
        collision.visitInsn(Opcodes.ARETURN)
        collision.visitMaxs(0, 0)
        collision.visitEnd()
        writer.visitEnd()
        return writer.toByteArray()
    }

    private fun transform(input: ByteArray, bytesMode: BytesMode): ByteArray {
        val writer = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        ClassReader(input).accept(StringBlurClassVisitor(writer,
            Type.getInternalName(BytesMethodSizeTest::class.java), "decrypt", master,
            bytesMode, listOf(Mode.XOR), null, 0), ClassReader.EXPAND_FRAMES)
        // ASM serialization itself rejects any Code attribute over 65535 bytes.
        return writer.toByteArray()
    }

    private fun load(bytes: ByteArray): Class<*> = object : ClassLoader(javaClass.classLoader) {
        fun sample() = defineClass("test.BytesSample", bytes, 0, bytes.size)
    }.sample()

    private fun invoke(bytes: ByteArray): Any? = load(bytes).getMethod("value").invoke(null)

    companion object {
        private val master = KeyDerivation.master("method-size-test")

        @JvmStatic
        fun decrypt(data: ByteArray, tag: Int, mode: Int): String {
            require(mode == 0)
            return ModeUtils.getEncodeImpl(Mode.XOR).decryptBytes(data, KeyDerivation.derive(master, tag).toByteArray())
        }

        @JvmStatic
        fun decrypt(data: String, tag: Int, mode: Int): String {
            require(mode == 0)
            return ModeUtils.getEncodeImpl(Mode.XOR).decryptString(data, KeyDerivation.derive(master, tag))
        }
    }
}
