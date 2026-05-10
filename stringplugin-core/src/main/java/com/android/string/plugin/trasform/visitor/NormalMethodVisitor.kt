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
    private val access: Int,
    mv: MethodVisitor,
    private val controller: ClassVisitorController,
    private val methodName: String?
) : MethodVisitor(Opcodes.ASM9, mv) {
    override fun visitLdcInsn(value: Any?) {
        // If the value is a static final field
        if (value is String && controller.overflow(value)) {
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
                if ((access and Opcodes.ACC_STATIC) == 0) {
                    //静态方法不能使用类的final成员变量
                    // If the value is a final field (not static)
                    controller.finalFields.forEach {
                        // if the value of a final field is null, we ignore it
                        if (value == it.value) {
                            super.visitVarInsn(Opcodes.ALOAD, 0)
                            super.visitFieldInsn(
                                Opcodes.GETFIELD,
                                controller.currentClassName,
                                it.name,
                                StringFiled.DESC
                            )
                            return@End
                        }
                    }
                }
                // local variables
                controller.write(value, mv, methodName)
            }
            return
        }
        controller.reportIgnoredLdc(methodName, value)
        super.visitLdcInsn(value)
    }
}