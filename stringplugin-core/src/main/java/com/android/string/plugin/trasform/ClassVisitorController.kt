package com.android.string.plugin.trasform

import com.android.string.plugin.data.Constant
import com.android.string.plugin.field.StringFiled
import com.android.string.plugin.files.StringEncodeImpl
import com.android.string.plugin.trasform.visitor.ClinitMethodVisitor
import com.android.string.plugin.trasform.visitor.InitMethodVisitor
import com.android.string.plugin.trasform.visitor.NormalMethodVisitor
import com.android.string.plugin.util.AsmWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author chancey
 * @date   2023/9/6   22:54
 **/
class ClassVisitorController(
    applicationId: String,
    private val key: String,
    private val useBytes: Boolean
) {
    private val stringBlurClassName =
        Constant.PLUGIN_CLASS_PACKAGE.format(applicationId).replace(".", "/")
    var currentClassName: String? = null
    private val stringEncodeImpl = StringEncodeImpl()
    val staticFinalFields = mutableListOf<StringFiled>()
    val staticFields = mutableListOf<StringFiled>()
    val finalFields = mutableListOf<StringFiled>()
    private val fields = mutableListOf<StringFiled>()
    private var isClInitExists = false
    fun visitField(access: Int, name: String?, desc: String?, value: String?) {
        if (name.isNullOrBlank() || desc != StringFiled.DESC) {
            return
        }
        val isStatic = (access and Opcodes.ACC_STATIC) != 0
        val isFinal = (access and Opcodes.ACC_FINAL) != 0
        val field = StringFiled(name, value)
        when {
            // static final, in this condition, the value is null or not null.
            isStatic && isFinal -> staticFinalFields += field
            // static, in this condition, the value is null.
            isStatic && !isFinal -> staticFields += field
            // final, in this condition, the value is null or not null.
            !isStatic && isFinal -> finalFields += field
            // normal, in this condition, the value is null.
            else -> fields += field
        }
    }

    fun visitEnd(mv: MethodVisitor) {
        mv.visitCode()
        // Here init static final fields.
        staticFinalFields.forEach {
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
        return !isClInitExists && staticFinalFields.isNotEmpty()
    }

    fun visitMethod(access: Int, mv: MethodVisitor, name: String?): MethodVisitor {
        return when (name) {
            // If clinit exists meaning the static fields (not final) would have be inited here.
            "<clinit>" -> {
                isClInitExists = true
                ClinitMethodVisitor(mv, this)
            }
            // Here init final(not static) and normal fields
            "<init>" -> InitMethodVisitor(mv, this)
            else -> NormalMethodVisitor(access, mv, this)
        }
    }


    fun overflow(data: String?) = stringEncodeImpl.overflow(data?.toByteArray())

    fun write(data: String?, mv: MethodVisitor) {
        if (useBytes) {
            writeByBytes(data, mv)
        } else {
            writeByString(data, mv)
        }
    }

    private fun writeByString(data: String?, mv: MethodVisitor) {
        val encodeText = stringEncodeImpl.encryptString(data, key)
        AsmWriter(stringBlurClassName).write(encodeText, key, mv)
    }

    private fun writeByBytes(data: String?, mv: MethodVisitor) {
        val encodeText = stringEncodeImpl.encryptBytes(data, key)
        AsmWriter(stringBlurClassName).write(encodeText, key, mv)
    }
}