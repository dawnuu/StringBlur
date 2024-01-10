package com.android.string.plugin.trasform

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.string.plugin.data.Constant
import com.android.string.plugin.util.WhileLists
import org.objectweb.asm.ClassVisitor

/**
 * @author chancey
 * @date   2023/9/5   20:36
 **/
abstract class StringBlurClassTransform : AsmClassVisitorFactory<InstrumentationParameters.None> {

    companion object {
        private lateinit var key: String
        private lateinit var applicationId: String
        private lateinit var encodePackages: List<String>
        fun setParams(
            key: String,
            applicationId: String,
            whileList: List<String>,
            encodePackages: List<String>
        ) {
            this.key = key
            this.applicationId = applicationId
            this.encodePackages = encodePackages
            WhileLists.add(Constant.PLUGIN_CLASS_PACKAGE.format(applicationId))
            WhileLists.add(whileList)
        }
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val className = classData.className
        return !WhileLists.contains(className) && isInEncodePackages(className)
    }

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ) = StringBlurClassVisitor(nextClassVisitor, key, applicationId)

    private fun isInEncodePackages(className: String): Boolean {
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