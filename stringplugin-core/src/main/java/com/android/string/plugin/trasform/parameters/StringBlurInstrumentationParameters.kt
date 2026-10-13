package com.android.string.plugin.trasform.parameters

import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.string.plugin.StringBlurExtension
import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.report.StringBlurReportService
import com.android.string.plugin.util.ModeUtils
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

/**
 * @author chancey
 * @date   2025/6/20
 **/
abstract class StringBlurInstrumentationParameters : InstrumentationParameters {
    @get:Input
    /** 由配置稳定派生的种子，运行时主密钥材料由它得出 */
    abstract val seed: Property<String>

    @get:Input
    abstract val bytesMode: Property<BytesMode>

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

    // 报告收集器不影响加密产物，不参与缓存指纹；文件写入由构建服务收尾负责。
    @get:Internal
    abstract val reportService: Property<StringBlurReportService>

    /** 运行时解密入口类的内部名（斜线分隔），按配置派生 */
    @get:Input
    abstract val wrapperClass: Property<String>

    /** 运行时解密入口方法名，按配置派生 */
    @get:Input
    abstract val wrapperMethod: Property<String>

    fun setParams(
        seed: Provider<String>,
        applicationId: Provider<String>,
        extension: StringBlurExtension,
        variantName: String,
        reportService: Provider<StringBlurReportService>,
        modes: List<Mode>,
        wrapperClass: Provider<String>,
        wrapperMethod: Provider<String>
    ) {
        this.seed.set(seed)
        this.bytesMode.set(extension.bytesMode)
        this.applicationId.set(applicationId)
        this.modes.addAll(modes)
        this.minLength.set(extension.minLength.coerceAtLeast(0))
        this.variantName.set(variantName)
        this.reportService.set(reportService)
        this.wrapperClass.set(wrapperClass)
        this.wrapperMethod.set(wrapperMethod)
        
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
