package com.android.string.plugin.trasform

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.string.plugin.task.StringBlurTaskData
import com.android.string.plugin.util.WhileLists
import org.objectweb.asm.ClassVisitor

/**
 * @author chancey
 * @date   2023/9/5   20:36
 **/
abstract class StringBlurClassTransform : AsmClassVisitorFactory<InstrumentationParameters.None> {

    companion object {
        private lateinit var key: String
        private lateinit var data: StringBlurTaskData
        private lateinit var encodePackages: List<String>
        fun setParams(
            key: String,
            data: StringBlurTaskData,
            whileList: List<String>,
            encodePackages: List<String>
        ) {
            this.key = key
            this.data = data
            this.encodePackages = encodePackages
            WhileLists.add("${data.applicationId}.${data.pkg}.${data.alias}")
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
    ) = StringBlurClassVisitor(nextClassVisitor, key, data)

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