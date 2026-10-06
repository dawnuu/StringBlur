package com.android.string.plugin.trasform.visitor

import com.android.string.plugin.field.StringFiled
import com.android.string.plugin.trasform.ClassVisitorController
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author chancey
 * @date   2023/12/9   20:33
 **/
class InitMethodVisitor(
    mv: MethodVisitor,
    controller: ClassVisitorController,
    methodName: String?,
    maxLocals: Int = 0
) : StringDeferringMethodVisitor(mv, controller, methodName, maxLocals) {

    override fun flushPending(value: String, skipReason: String?) {
        // We don't care about whether the field is final or normal
        if (skipReason != null) {
            controller.reportIgnored(methodName, value, skipReason)
            writePlainLdc(value)
            return
        }
        controller.write(value, mv, methodName)
    }

    override fun shouldKeepFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?): Boolean {
        return opcode == Opcodes.PUTFIELD &&
            descriptor == StringFiled.DESC &&
            controller.currentClassName == owner &&
            controller.isKeepInstanceField(name)
    }
}
