package com.android.string.plugin.trasform.visitor

import org.junit.Assert.assertEquals
import org.junit.Test
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

class ConstantAppendRewriterTest {
    @Test
    fun leavesMergedDynamicAndConstantOperandsUntouched() {
        val method = MethodNode(Opcodes.ASM9, Opcodes.ACC_STATIC, "join", "(ZI)Ljava/lang/String;", null, null)
        val constantBranch = Label()
        val merge = Label()
        method.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        method.visitInsn(Opcodes.DUP)
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        method.visitVarInsn(Opcodes.ILOAD, 0)
        method.visitJumpInsn(Opcodes.IFEQ, constantBranch)
        method.visitVarInsn(Opcodes.ILOAD, 1)
        method.visitJumpInsn(Opcodes.GOTO, merge)
        method.visitLabel(constantBranch)
        method.visitInsn(Opcodes.ICONST_1)
        method.visitLabel(merge)
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false)
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        method.visitInsn(Opcodes.ARETURN)
        method.visitMaxs(3, 2)
        ConstantAppendRewriter.rewrite("test/Sample", method)
        val append = method.instructions.filterIsInstance<MethodInsnNode>().single { it.name == "append" }
        assertEquals("(I)Ljava/lang/StringBuilder;", append.desc)
        assertEquals(0, method.instructions.filterIsInstance<LdcInsnNode>().size)
    }

    @Test
    fun preservesLoneSurrogateChars() {
        val method = MethodNode(Opcodes.ASM9, Opcodes.ACC_STATIC, "join", "()Ljava/lang/String;", null, null)
        method.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder")
        method.visitInsn(Opcodes.DUP)
        method.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
        method.visitLdcInsn(0xd800)
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false)
        method.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false)
        method.visitInsn(Opcodes.ARETURN)
        method.visitMaxs(2, 0)
        ConstantAppendRewriter.rewrite("test/Sample", method)
        assertEquals(0xd800, method.instructions.filterIsInstance<LdcInsnNode>().single().cst)
        assertEquals("(C)Ljava/lang/StringBuilder;", method.instructions.filterIsInstance<MethodInsnNode>().single { it.name == "append" }.desc)
    }
}
