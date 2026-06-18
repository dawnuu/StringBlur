package com.android.string.plugin.trasform

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.string.plugin.data.Constant
import com.android.string.plugin.trasform.parameters.StringBlurInstrumentationParameters
import org.objectweb.asm.ClassVisitor

abstract class StringBlurClassTransform :
    AsmClassVisitorFactory<StringBlurInstrumentationParameters> {

    override fun isInstrumentable(classData: ClassData): Boolean {
        val className = classData.className
        val params = parameters.get()
        val whiteList = params.whiteList.get()

        val isInWhiteList = whiteList.any { whiteEntry ->
            className.endsWith(whiteEntry) || className.startsWith(whiteEntry)
        }

        if (isInWhiteList) {
            return false
        }

        return isInEncodePackages(className)
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return with(parameters.get()) {
            StringBlurClassVisitor(
                nextClassVisitor,
                key.get(),
                bytesMode.get(),
                applicationId.get(),
                modes.get(),
                reportPath.orNull,
                minLength.get(),
                selectionStrategy.get(),
                performanceWeight.get(),
                securityWeight.get()
            )
        }
    }

    private fun isInEncodePackages(className: String): Boolean {
        val encodePackages = parameters.get().encodePackages.get()
        if (encodePackages.isEmpty()) {
            return true
        }
        for (encodePackage in encodePackages) {
            if (className.startsWith(encodePackage)) {
                return true
            }
        }
        return false
    }
}
