package com.android.string.plugin.util

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * @author chancey
 * @date   2024/1/12   14:08
 **/
class AsmWriter(private val className: String) {

    fun write(data: String, key: String, mv: MethodVisitor) {
        write(data, key, 0, mv)
    }

    fun write(data: String, key: String, modeIndex: Int, mv: MethodVisitor) {
        mv.visitLdcInsn(data)
        mv.visitLdcInsn(key)
        write(modeIndex, mv)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            className,
            "decrypt",
            "(Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;",
            false
        )
    }

    fun write(data: ByteArray, key: String, mv: MethodVisitor) {
        write(data, key, 0, mv)
    }

    fun write(data: ByteArray, key: String, modeIndex: Int, mv: MethodVisitor) {
        write(data, mv)
        write(key.toByteArray(), mv)
        write(modeIndex, mv)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            className,
            "decrypt",
            "([B[BI)Ljava/lang/String;",
            false
        )
    }

    private fun write(value: ByteArray, mv: MethodVisitor) {
        write(value.size, mv)
        mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE)
        var i = 0
        while (i < value.size) {
            mv.visitInsn(Opcodes.DUP)
            write(i, mv)
            write(value[i].toInt(), mv)
            mv.visitInsn(Type.BYTE_TYPE.getOpcode(Opcodes.IASTORE))
            i++
        }
    }

    private fun write(value: Int, mv: MethodVisitor) {
        when (value) {
            in -1..5 -> mv.visitInsn(Opcodes.ICONST_0 + value)
            in Byte.MIN_VALUE..Byte.MAX_VALUE -> mv.visitIntInsn(Opcodes.BIPUSH, value)
            in Short.MAX_VALUE..Short.MAX_VALUE -> mv.visitIntInsn(Opcodes.SIPUSH, value)
            else -> mv.visitLdcInsn(value)
        }
    }
}