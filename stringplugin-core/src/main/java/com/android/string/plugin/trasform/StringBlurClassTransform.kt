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
        private var useBytes = false
        private lateinit var applicationId: String
        private val encodePackages = mutableListOf<String>()
        fun setParams(
            key: String,
            useBytes: Boolean,
            applicationId: String,
            whileList: List<String>,
            encodePackages: List<String>?
        ) {
            this.key = key
            this.useBytes = useBytes
            this.applicationId = applicationId
            //为空则加密全部
            if (encodePackages != null) {
                //将自身添加进加密列表
                this.encodePackages.add(applicationId)
                //追加自定义列表
                this.encodePackages.addAll(encodePackages)
            }
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
    ) = StringBlurClassVisitor(nextClassVisitor, key, useBytes, applicationId)

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