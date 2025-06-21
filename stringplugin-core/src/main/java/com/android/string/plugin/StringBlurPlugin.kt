package com.android.string.plugin

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.string.plugin.data.Constant
import com.android.string.plugin.task.StringBlurTask
import com.android.string.plugin.trasform.StringBlurClassTransform
import com.android.string.plugin.util.Logger
import com.android.string.plugin.util.generator.KeyGenerator
import com.android.string.plugin.util.generator.RandomGenerator
import groovy.xml.XmlParser
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.nio.charset.Charset
import java.util.Properties

class StringBlurPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create(Constant.PLUGIN_NAME, StringBlurExtension::class.java)
        val extension = target.extensions.findByType(BaseExtension::class.java)
            ?: throw GradleException(String.format(Logger.text("请添加插件")))
        val components =
            target.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
                ?: target.extensions.getByType(LibraryAndroidComponentsExtension::class.java)
        components.onVariants {
            val stringblur = target.extensions.getByType(StringBlurExtension::class.java)
            if (!stringblur.enable) {
                Logger.log("功能关闭")
                return@onVariants
            }
            val generator = when (stringblur.key) {
                is String -> KeyGenerator(stringblur.key as String)
                is Int -> RandomGenerator(stringblur.key as Int)
                else -> null
            } ?: throw GradleException(Logger.text("加密key不能为空"))
            //获取applicationId
            val applicationId = getApplicationId(target, extension)
            if (applicationId.isBlank()) {
                throw GradleException(Logger.text("无法获取applicationId"))
            }
            it.instrumentation.transformClassesWith(
                StringBlurClassTransform::class.java,
                InstrumentationScope.ALL
            ) { params ->
                params.setParams(generator, applicationId, stringblur)
            }
            appendImplementations(target)
            it.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_CLASSES)
            if (extension is AppExtension) {
                extension.applicationVariants.all { variant ->
                    StringBlurTask.execute(
                        target,
                        variant,
                        applicationId,
                        stringblur.customEncodeClass
                    )
                }
            } else if (extension is LibraryExtension) {
                extension.libraryVariants.all { variant ->
                    StringBlurTask.execute(
                        target,
                        variant,
                        applicationId,
                        stringblur.customEncodeClass
                    )
                }
            }
        }
    }

    private fun appendImplementations(project: Project) {
        val properties = Properties().apply {
            load(File("gradle.properties").inputStream())
        }
        project.dependencies.add(
            "implementation",
            "com.android.string.plugin:common:${properties.getValue("VERSION")}"
        )
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