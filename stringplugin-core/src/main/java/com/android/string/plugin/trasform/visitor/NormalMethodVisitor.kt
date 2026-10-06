package com.android.string.plugin.trasform.visitor

import com.android.string.plugin.field.StringFiled
import com.android.string.plugin.trasform.ClassVisitorController
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author chancey
 * @date   2023/12/9   20:33
 **/
class NormalMethodVisitor(
    mv: MethodVisitor,
    controller: ClassVisitorController,
    methodName: String?,
    maxLocals: Int = 0
) : StringDeferringMethodVisitor(mv, controller, methodName, maxLocals) {

    override fun flushPending(value: String, skipReason: String?) {
        if (skipReason != null) {
            controller.reportIgnored(methodName, value, skipReason)
            writePlainLdc(value)
            return
        }
        // If the value is a static final field
        run End@{
            controller.staticFinalFields.forEach {
                if (value == it.value) {
                    super.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        controller.currentClassName,
                        it.name,
                        StringFiled.DESC
                    )
                    return@End
                }
            }
            // local variables
            controller.write(value, mv, methodName)
        }
    }
}
