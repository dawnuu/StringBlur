package com.android.string.plugin.trasform.visitor

import com.android.string.plugin.trasform.ClassVisitorController
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author chancey
 * @date   2023/12/9   20:33
 **/
class InitMethodVisitor(mv: MethodVisitor, private val controller: ClassVisitorController) :
    MethodVisitor(Opcodes.ASM9, mv) {

    override fun visitLdcInsn(value: Any?) {
        // We don't care about whether the field is final or normal
        if (value is String && controller.overflow(value)) {
            controller.write(value, mv)
            return
        }
        super.visitLdcInsn(value)
    }
}