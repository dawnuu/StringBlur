package com.android.string.plugin.trasform

import com.android.string.plugin.IString
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * @author chancey
 * @date   2023/9/5   22:23
 **/
class StringBlurClassVisitor(
    cv: ClassVisitor,
    key: String,
    useBytes: Boolean,
    applicationId: String,
    stringWrapper: IString,
    reportPath: String,
    minLength: Int,
) : ClassVisitor(Opcodes.ASM9, cv) {
    private val controller = ClassVisitorController(applicationId, key, useBytes, stringWrapper, reportPath, minLength)
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        controller.currentClassName = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitEnd() {
        if (controller.isVisitClInitMethod()) {
            controller.visitEnd(
                super.visitMethod(
                    Opcodes.ACC_STATIC,
                    "<clinit>",
                    "()V",
                    null,
                    null
                )
            )
        }
        super.visitEnd()
    }

    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        controller.visitField(access, name, descriptor, value as? String)
        return super.visitField(access, name, descriptor, signature, value)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
            ?: return super.visitMethod(access, name, descriptor, signature, exceptions)
        return controller.visitMethod(access, mv, name)
    }
}