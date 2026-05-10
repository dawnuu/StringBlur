package com.android.string.plugin.trasform

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.string.plugin.report.StringBlurReport
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
        val params = parameters.get()
        val reportPath = params.reportPath.get()
        val whiteList = params.whiteList.get()
        StringBlurReport.scanned(reportPath, className)
        
        val isInWhiteList = whiteList.any { whiteEntry ->
            className.endsWith(whiteEntry) || className.startsWith(whiteEntry)
        }
        
        if (isInWhiteList) {
            Logger.log("白名单:$className")
            StringBlurReport.skipped(reportPath, className, "whiteList")
            return false
        }

        val isInEncodePackages = isInEncodePackages(className)
        if (!isInEncodePackages) {
            StringBlurReport.skipped(reportPath, className, "encodePackages")
        }
        return isInEncodePackages
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
                StringBlurWrapper(mode.get()),
                reportPath.get(),
                minLength.get()
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
