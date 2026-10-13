package com.android.string.plugin.trasform

import com.android.string.plugin.util.AsmWriter
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodNode

/** Keeps byte-array initialization out of the original method's 64 KB Code budget. */
internal class BytesMethodWriter(
    private val owner: String,
    private val isInterface: Boolean,
    private val asmWriter: AsmWriter,
    private val reservedNames: Set<String>
) {
    private val methods = mutableListOf<MethodNode>()
    private var nextId = 0
    private val access = Opcodes.ACC_STATIC or Opcodes.ACC_SYNTHETIC or
        if (isInterface) Opcodes.ACC_PUBLIC else Opcodes.ACC_PRIVATE

    private fun newMethod(descriptor: String): MethodNode {
        var name: String
        do {
            name = "\$stringBlur\$bytes\$${nextId++}"
        } while (name in reservedNames)
        return MethodNode(Opcodes.ASM9, access, name, descriptor, null, null).also {
            methods += it
            it.visitCode()
        }
    }

    fun write(data: ByteArray, tag: Int, modeIndex: Int, target: MethodVisitor) {
        val method = newMethod("()Ljava/lang/String;")
        if (data.size <= CHUNK_SIZE) {
            asmWriter.write(data, tag, modeIndex, method)
        } else {
            asmWriter.write(data.size, method)
            method.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE)
            for (start in data.indices step CHUNK_SIZE) {
                val fill = newMethod("([B)V")
                for (index in start until minOf(start + CHUNK_SIZE, data.size)) {
                    fill.visitVarInsn(Opcodes.ALOAD, 0)
                    asmWriter.write(index, fill)
                    asmWriter.write(data[index].toInt(), fill)
                    fill.visitInsn(Opcodes.BASTORE)
                }
                fill.visitInsn(Opcodes.RETURN)
                fill.visitMaxs(3, 1)
                fill.visitEnd()
                method.visitInsn(Opcodes.DUP)
                method.visitMethodInsn(Opcodes.INVOKESTATIC, owner, fill.name, fill.desc, isInterface)
            }
            asmWriter.writeDecryptBytes(tag, modeIndex, method)
        }
        method.visitInsn(Opcodes.ARETURN)
        method.visitMaxs(4, 0)
        method.visitEnd()
        target.visitMethodInsn(Opcodes.INVOKESTATIC, owner, method.name, method.desc, isInterface)
    }

    fun emit(visitor: ClassVisitor) {
        methods.forEach { it.accept(visitor) }
    }

    companion object {
        // At most 8 bytes of code per array element, leaving ample room below 65536.
        private const val CHUNK_SIZE = 4096
    }
}
