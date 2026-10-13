package com.android.string.plugin.util

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * @author chancey
 * @date   2024/1/12   14:08
 **/
class AsmWriter(private val className: String, private val methodName: String = "decrypt") {

    fun write(data: String, tag: Int, modeIndex: Int, mv: MethodVisitor) {
        mv.visitLdcInsn(data)
        write(tag, mv)
        write(modeIndex, mv)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            className,
            methodName,
            "(Ljava/lang/String;II)Ljava/lang/String;",
            false
        )
    }

    fun write(data: ByteArray, tag: Int, modeIndex: Int, mv: MethodVisitor) {
        write(data, mv)
        writeDecryptBytes(tag, modeIndex, mv)
    }

    internal fun writeDecryptBytes(tag: Int, modeIndex: Int, mv: MethodVisitor) {
        write(tag, mv)
        write(modeIndex, mv)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            className,
            methodName,
            "([BII)Ljava/lang/String;",
            false
        )
    }

    internal fun write(value: ByteArray, mv: MethodVisitor) {
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

    internal fun write(value: Int, mv: MethodVisitor) {
        when (value) {
            in -1..5 -> mv.visitInsn(Opcodes.ICONST_0 + value)
            in Byte.MIN_VALUE..Byte.MAX_VALUE -> mv.visitIntInsn(Opcodes.BIPUSH, value)
            in Short.MIN_VALUE..Short.MAX_VALUE -> mv.visitIntInsn(Opcodes.SIPUSH, value)
            else -> mv.visitLdcInsn(value)
        }
    }
}
