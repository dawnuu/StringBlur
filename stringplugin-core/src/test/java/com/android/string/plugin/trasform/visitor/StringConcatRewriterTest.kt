package com.android.string.plugin.trasform.visitor

import com.android.string.plugin.trasform.StringBlurClassVisitor
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode

class StringConcatRewriterTest {

    @Test
    fun rewritesStringConcatAndKeepsFixedTextVisibleToStringVisitor() {
        val inputWriter = ClassWriter(0)
        inputWriter.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "test/ConcatSample", null, "java/lang/Object", null)
        val method = inputWriter.visitMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC,
            "join",
            "(Ljava/lang/String;)Ljava/lang/String;",
            null,
            null
        )
        method.visitCode()
        method.visitVarInsn(Opcodes.ALOAD, 0)
        method.visitInvokeDynamicInsn(
            "makeConcatWithConstants",
            "(Ljava/lang/String;)Ljava/lang/String;",
            Handle(
                Opcodes.H_INVOKESTATIC,
                "java/lang/invoke/StringConcatFactory",
                "makeConcatWithConstants",
                "(Ljava/lang/invoke/MethodHandles\$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
                false
            ),
            "prefix=\u0001"
        )
        method.visitInsn(Opcodes.ARETURN)
        method.visitMaxs(1, 1)
        method.visitEnd()
        inputWriter.visitEnd()

        val outputWriter = ClassWriter(0)
        val visitor = StringBlurClassVisitor(
            outputWriter,
            "test/StringBlur",
            "decrypt",
            "test-key",
            com.android.string.plugin.mode.BytesMode.STRING,
            listOf(com.android.string.plugin.mode.Mode.DEFAULT),
            null,
            3,
            com.android.string.plugin.mode.SelectionStrategy.RANDOM,
            0.5,
            0.5
        )
        ClassReader(inputWriter.toByteArray()).accept(visitor, 0)

        val classNode = ClassNode(Opcodes.ASM9)
        ClassReader(outputWriter.toByteArray()).accept(classNode, 0)
        val instructions = classNode.methods.single { it.name == "join" }.instructions.toArray()

        assertTrue(instructions.none { it is InvokeDynamicInsnNode })
        assertTrue(instructions.any {
            it is MethodInsnNode &&
                it.owner == "java/lang/StringBuilder" &&
                it.name == "append"
        })
        assertFalse(instructions.any { it is LdcInsnNode && it.cst == "prefix=" })
    }
}
