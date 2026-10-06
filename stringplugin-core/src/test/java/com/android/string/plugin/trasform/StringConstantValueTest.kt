package com.android.string.plugin.trasform

import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.mode.SelectionStrategy
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

class StringConstantValueTest {

    @Test
    fun removesConstantValueWhenStaticFinalStringIsEncrypted() {
        val inputWriter = ClassWriter(0)
        inputWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "test/Constants", null, "java/lang/Object", null)
        inputWriter.visitField(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL,
            "URL",
            "Ljava/lang/String;",
            null,
            "https://example.test/long-value"
        ).visitEnd()
        inputWriter.visitEnd()

        val outputWriter = ClassWriter(0)
        val visitor = StringBlurClassVisitor(
            outputWriter,
            "test/StringBlur",
            "decrypt",
            "test-key",
            BytesMode.STRING,
            listOf(Mode.DEFAULT),
            null,
            3,
            SelectionStrategy.RANDOM,
            0.5,
            0.5
        )
        ClassReader(inputWriter.toByteArray()).accept(visitor, 0)

        val classNode = ClassNode(Opcodes.ASM9)
        ClassReader(outputWriter.toByteArray()).accept(classNode, 0)

        assertNull(classNode.fields.single { it.name == "URL" }.value)
        assertNotNull(classNode.methods.singleOrNull { it.name == "<clinit>" })
    }
}
