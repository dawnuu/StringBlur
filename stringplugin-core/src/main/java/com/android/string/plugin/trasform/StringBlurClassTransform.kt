package com.android.string.plugin.trasform

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.string.plugin.trasform.parameters.StringBlurInstrumentationParameters
import org.objectweb.asm.ClassVisitor

abstract class StringBlurClassTransform :
    AsmClassVisitorFactory<StringBlurInstrumentationParameters> {

    override fun isInstrumentable(classData: ClassData): Boolean {
        val className = classData.className
        val params = parameters.get()
        val whiteList = params.whiteList.get()

        val isInWhiteList = isWhiteListed(className, whiteList)
        return !isInWhiteList && isInEncodePackages(className)
    }

    private fun isWhiteListed(className: String, whiteList: List<String>): Boolean {
        val normalizedClass = className.replace('/', '.')
        return whiteList.any { whiteEntry ->
            when {
                whiteEntry.endsWith(".") -> normalizedClass.startsWith(whiteEntry)
                whiteEntry.contains(".") -> {
                    normalizedClass == whiteEntry || normalizedClass.startsWith("$whiteEntry.")
                }
                else -> {
                    val simpleName = normalizedClass.substringAfterLast('.')
                    simpleName == whiteEntry || simpleName.startsWith("$whiteEntry$")
                }
            }
        }
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        return with(parameters.get()) {
            StringBlurClassVisitor(
                nextClassVisitor,
                wrapperClass.get(),
                wrapperMethod.get(),
                key.get(),
                bytesMode.get(),
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
