package com.android.string.plugin.trasform

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.string.plugin.task.StringBlurTaskData
import com.android.string.plugin.util.WhileLists
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

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

    override fun isInstrumentable(classData: ClassData) = true

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor {
        val className = classContext.currentClassData.className
        return if (WhileLists.contains(className) || !isInEncodePackages(className)) {
            object : ClassVisitor(Opcodes.ASM9, nextClassVisitor) {}
        } else {
            StringBlurClassVisitor(nextClassVisitor, key, data)
        }
    }

    private fun isInEncodePackages(className: String): Boolean {
        for (encodePackage in encodePackages) {
            if (className.startsWith(encodePackage)) {
                return true
            }
        }
        return false
    }
}