package com.android.string.plugin.trasform.parameters

import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.string.plugin.StringBlurExtension
import com.android.string.plugin.data.Constant
import com.android.string.plugin.util.WhileLists
import com.android.string.plugin.util.generator.Generator
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

/**
 * @author chancey
 * @date   2025/6/20
 **/
abstract class StringBlurInstrumentationParameters : InstrumentationParameters {
    @get:Input
    abstract val key: Property<String>

    @get:Input
    abstract val useBytes: Property<Boolean>

    @get:Input
    abstract val applicationId: Property<String>

    @get:Input
    abstract val encodePackages: ListProperty<String>

    @get:Input
    abstract val customEncodeClass: Property<String>

    fun setParams(generator: Generator, applicationId: String, extension: StringBlurExtension) {
        this.key.set(generator.generate())
        this.useBytes.set(extension.useBytes)
        this.applicationId.set(applicationId)
        this.customEncodeClass.set(extension.customEncodeClass.orEmpty())
        //为空则加密全部
        if (extension.encodePackages != null) {
            //将自身添加进加密列表
            this.encodePackages.add(applicationId)
            //追加自定义列表
            this.encodePackages.addAll(extension.encodePackages!!)
        }
        WhileLists.add(Constant.PLUGIN_CLASS_PACKAGE.format(applicationId))
        //将加密类添加到白名单
        if (!extension.customEncodeClass.isNullOrBlank()) {
            WhileLists.add(extension.customEncodeClass!!)
        }
        WhileLists.add(extension.whiteList)
    }
}