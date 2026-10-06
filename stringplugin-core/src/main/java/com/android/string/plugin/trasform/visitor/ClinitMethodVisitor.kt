package com.android.string.plugin.trasform.visitor

import com.android.string.plugin.data.Constant
import com.android.string.plugin.field.StringFiled
import com.android.string.plugin.trasform.ClassVisitorController
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author chancey
 * @date   2023/12/9   20:33
 **/
class ClinitMethodVisitor(
    mv: MethodVisitor,
    controller: ClassVisitorController,
    methodName: String?,
    maxLocals: Int = 0
) : StringDeferringMethodVisitor(mv, controller, methodName, maxLocals) {
    private var temp: String? = null

    override fun visitCode() {
        super.visitCode()
        // Here init static final fields.
        controller.staticFinalFields.forEach {
            if (it.keep) {
                return@forEach
            }
            if (!it.force && !controller.overflow(it.value)) {
                return@forEach
            }
            controller.write(it.value, mv, methodName)
            super.visitFieldInsn(
                Opcodes.PUTSTATIC,
                controller.currentClassName,
                it.name,
                StringFiled.DESC
            )
        }
    }

    override fun flushPending(value: String, skipReason: String?) {
        // Here init static or static final fields, but we must check field name int 'visitFieldInsn'
        temp = value
        if (skipReason != null) {
            controller.reportIgnored(methodName, value, skipReason)
            writePlainLdc(value)
            return
        }
        controller.write(value, mv, methodName)
    }

    override fun resetPendingState() {
        temp = null
    }

    override fun shouldKeepFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?): Boolean {
        return opcode == Opcodes.PUTSTATIC &&
            descriptor == StringFiled.DESC &&
            controller.currentClassName == owner &&
            controller.isKeepStaticField(name)
    }

    override fun visitFieldInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?
    ) {
        // 先冲刷暂存的 LDC（含 keep 字段判定），temp 才能反映即将 PUTSTATIC 的值
        flushBeforeFieldInsn(opcode, owner, name, descriptor)
        if (opcode == Opcodes.PUTSTATIC &&
            descriptor == StringFiled.DESC &&
            controller.currentClassName == owner &&
            temp != null
        ) {
            run End@{
                controller.staticFields.forEach {
                    if (it.name == name) {
                        temp = null
                        return@End
                    }
                }
                controller.staticFinalFields.forEach {
                    if (it.name == name && it.value.isNullOrBlank()) {
                        it.value = temp
                        temp = null
                        return@End
                    }
                }
            }
        }
        temp = null
        super.visitFieldInsn(opcode, owner, name, descriptor)
    }
}
