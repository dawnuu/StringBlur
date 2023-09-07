package com.android.string.plugin.trasform

import com.android.string.plugin.field.StringFiled
import com.android.string.plugin.stringblur.MD5
import com.android.string.plugin.stringblur.StringEncodeImpl
import com.android.string.plugin.task.StringBlurTaskData
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author chancey
 * @date   2023/9/6   22:54
 **/
class ClassVisitorController(data: StringBlurTaskData, private val key: String) {
    private val stringBlurClassName =
        "${data.applicationId}.${data.pkg}.${data.alias}".replace(".", "/")
    var currentClassName: String? = null
    private val stringEncodeImpl = StringEncodeImpl()
    private val fields = mutableListOf<StringFiled>()
    private var isClInitExists = false
    fun visitField(access: Int, name: String?, desc: String?, value: String?) {
        if (name.isNullOrBlank() || desc != StringFiled.DESC) {
            return
        }
        val isStaticFinalFiled =
            (access and Opcodes.ACC_STATIC) != 0 && (access and Opcodes.ACC_FINAL) != 0
        if (isStaticFinalFiled) {
            fields += StringFiled(name, value)
        }
    }

    fun visitEnd(mv: MethodVisitor) {
        mv.visitCode()
        fields.forEach {
            if (!overflow(it.value)) {
                return@forEach
            }
            write(it.value, mv)
            mv.visitFieldInsn(Opcodes.PUTSTATIC, currentClassName, it.name, StringFiled.DESC)
        }
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(1, 0)
        mv.visitEnd()
    }

    fun isVisitClInitMethod(): Boolean {
        return !isClInitExists && fields.isNotEmpty()
    }

    fun visitMethod(mv: MethodVisitor, name: String?): MethodVisitor {
        isClInitExists = name == "<clinit>"
        return object : MethodVisitor(Opcodes.ASM9, mv) {
            override fun visitLdcInsn(value: Any?) {
                if (value is String && overflow(value) && !isClInitExists) {
                    write(value, mv)
                } else {
                    super.visitLdcInsn(value)
                }
            }

            override fun visitCode() {
                super.visitCode()
                if (isClInitExists) {
                    fields.forEach {
                        if (!overflow(it.value)) {
                            return@forEach
                        }
                        write(it.value, mv)
                        super.visitFieldInsn(
                            Opcodes.PUTSTATIC,
                            currentClassName,
                            it.name,
                            StringFiled.DESC
                        )
                    }
                }
            }
        }
    }


    private fun overflow(data: String?) = stringEncodeImpl.overflow(data, key)

    private fun write(data: String?, mv: MethodVisitor) {
        val encodeKey = MD5.getMessageDigest(key.toByteArray())
        val encodeText = stringEncodeImpl.encrypt(data, encodeKey)
        mv.visitLdcInsn(encodeText)
        mv.visitLdcInsn(encodeKey)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            stringBlurClassName,
            "decrypt",
            "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
            false
        )
    }

}