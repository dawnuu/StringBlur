package com.android.string.plugin

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.string.plugin.task.StringBlurTask
import com.android.string.plugin.task.StringBlurTaskData
import com.android.string.plugin.trasform.StringBlurClassTransform
import com.android.string.plugin.util.Logger
import groovy.xml.XmlParser
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.nio.charset.Charset

private const val PLUGIN_NAME = "stringblur"

class StringBlurPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create(PLUGIN_NAME, StringBlurExtension::class.java)
        val extension = target.extensions.findByType(BaseExtension::class.java)
            ?: throw GradleException(String.format(Logger.text("请添加插件")))
        val components = target.extensions.getByType(AndroidComponentsExtension::class.java)
        components.onVariants {
            val stringblur = target.extensions.getByType(StringBlurExtension::class.java)
            if (!stringblur.enable) {
                Logger.log("功能关闭")
                return@onVariants
            }
            if (stringblur.key.isBlank()) {
                throw GradleException(Logger.text("加密key不能为空"))
            }
            //获取applicationId
            val applicationId = getApplicationId(target, extension)
            if (applicationId.isBlank()) {
                throw GradleException(Logger.text("无法获取applicationId"))
            }
            val stringBlurTaskData = StringBlurTaskData(
                applicationId = applicationId,
                pkg = stringblur.pkg,
                alias = stringblur.alias
            )
            val whileList = stringblur.whiteList
            val encodePackages = stringblur.encodePackages
            StringBlurClassTransform.setParams(
                stringblur.key,
                stringBlurTaskData,
                whileList,
                encodePackages
            )
            it.instrumentation.transformClassesWith(
                StringBlurClassTransform::class.java,
                InstrumentationScope.ALL
            ) {}
            it.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS)
            if (extension is AppExtension) {
                extension.applicationVariants.all { variant ->
                    StringBlurTask.execute(target, variant, stringBlurTaskData)
                }
            } else if (extension is LibraryExtension) {
                extension.libraryVariants.all { variant ->
                    StringBlurTask.execute(target, variant, stringBlurTaskData)
                }
            }
        }
    }

    private fun getApplicationId(target: Project, extension: BaseExtension): String {
        var applicationId = ""
        val file = target.file("src/main/AndroidManifest.xml")
        if (file.exists()) {
            applicationId = XmlParser().parseText(file.readText(Charset.defaultCharset()))
                .attribute("package")?.toString().orEmpty()
        }
        if (applicationId.isBlank()) {
            applicationId = extension.namespace.orEmpty()
        }
        return applicationId
    }
}