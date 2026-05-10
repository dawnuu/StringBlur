package com.android.string.plugin.trasform.parameters

import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.string.plugin.StringBlurExtension
import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.util.ModeUtils
import com.android.string.plugin.util.generator.Generator
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
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
    abstract val whiteList: ListProperty<String>

    @get:Input
    abstract val modes: ListProperty<Mode>

    @get:Input
    abstract val minLength: Property<Int>

    @get:Input
    abstract val variantName: Property<String>

    @get:Input
    abstract val reportPath: Property<String>

    fun setParams(
        generator: Generator,
        applicationId: Provider<String>,
        extension: StringBlurExtension,
        variantName: String,
        reportPath: Provider<String>,
        modes: List<Mode>
    ) {
        this.key.set(generator.generate())
        this.useBytes.set(extension.useBytes)
        this.applicationId.set(applicationId)
        this.modes.addAll(modes)
        this.minLength.set(extension.minLength.coerceAtLeast(0))
        this.variantName.set(variantName)
        this.reportPath.set(reportPath)
        
        this.whiteList.addAll(extension.whiteList)
        this.whiteList.add("BuildConfig")
        this.whiteList.add("R2")
        this.whiteList.add("R")
        this.whiteList.add("IString")
        this.whiteList.add(Constant.DEFAULT_IMPL_CLASS_NAME)
        this.whiteList.add(applicationId.map { Constant.PLUGIN_CLASS_PACKAGE.format(it) })
        modes.forEach { mode ->
            this.whiteList.add(applicationId.map { ModeUtils.getEncodeImplClassFilePath(mode, it) })
        }

        if (extension.encodePackages != null) {
            this.encodePackages.add(applicationId)
            this.encodePackages.addAll(extension.encodePackages!!)
        }
    }
}
