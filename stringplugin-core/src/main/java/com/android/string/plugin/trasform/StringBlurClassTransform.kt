package com.android.string.plugin.trasform

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.string.plugin.trasform.parameters.StringBlurInstrumentationParameters
import com.android.string.plugin.util.Logger
import com.android.string.plugin.wrapper.StringBlurWrapper
import org.objectweb.asm.ClassVisitor

/**
 * @author chancey
 * @date   2023/9/5   20:36
 **/
abstract class StringBlurClassTransform :
    AsmClassVisitorFactory<StringBlurInstrumentationParameters> {

    override fun isInstrumentable(classData: ClassData): Boolean {
        val className = classData.className
        val whiteList = parameters.get().whiteList.get()
        
        val isInWhiteList = whiteList.any { whiteEntry ->
            className.endsWith(whiteEntry) || className.startsWith(whiteEntry)
        }
        
        if (isInWhiteList) {
            Logger.log("白名单:$className")
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
                useBytes.get(),
                applicationId.get(),
                StringBlurWrapper(mode.get())
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
