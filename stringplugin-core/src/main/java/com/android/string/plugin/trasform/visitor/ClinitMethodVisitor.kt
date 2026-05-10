package com.android.string.plugin.trasform.visitor

import com.android.string.plugin.field.StringFiled
import com.android.string.plugin.trasform.ClassVisitorController
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author chancey
 * @date   2023/12/9   20:33
 **/
class ClinitMethodVisitor(mv: MethodVisitor, private val controller: ClassVisitorController) :
    MethodVisitor(Opcodes.ASM9, mv) {
    private var temp: String? = null
    override fun visitCode() {
        super.visitCode()
        // Here init static final fields.
        controller.staticFinalFields.forEach {
            if (!controller.overflow(it.value)) {
                return
            }
            controller.write(it.value, mv)
            super.visitFieldInsn(
                Opcodes.PUTSTATIC,
                controller.currentClassName,
                it.name,
                StringFiled.DESC
            )
        }
    }

    override fun visitLdcInsn(value: Any?) {
        // Here init static or static final fields, but we must check field name int 'visitFieldInsn'
        if (value is String && controller.overflow(value)) {
            temp = value
            controller.write(value, mv)
        } else {
            temp = null
            super.visitLdcInsn(value)
        }
    }

    override fun visitFieldInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?
    ) {
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