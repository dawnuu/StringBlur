package com.android.string.plugin

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.string.plugin.data.Constant
import com.android.string.plugin.report.StringBlurReport
import com.android.string.plugin.task.StringBlurTask
import com.android.string.plugin.trasform.StringBlurClassTransform
import com.android.string.plugin.util.Logger
import com.android.string.plugin.util.ModeUtils
import com.android.string.plugin.util.generator.KeyGenerator
import com.android.string.plugin.util.generator.RandomGenerator
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class StringBlurPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create(Constant.PLUGIN_NAME, StringBlurExtension::class.java)
        val components =
            target.extensions.findByType(ApplicationAndroidComponentsExtension::class.java)
                ?: target.extensions.findByType(LibraryAndroidComponentsExtension::class.java)
                ?: throw GradleException(Logger.text("请在 Android 项目中使用此插件"))

        components.onVariants { variant ->
            val stringblur = target.extensions.getByType(StringBlurExtension::class.java)
            if (!stringblur.enable) {
                Logger.log("功能关闭")
                return@onVariants
            }
            
            // 检查debug模式设置
            val isDebugBuild = variant.buildType == "debug"
            if (isDebugBuild && !stringblur.enableWhenDebug) {
                Logger.log("Debug模式下加密已关闭")
                return@onVariants
            }
            val generator = when (stringblur.key) {
                is String -> KeyGenerator(stringblur.key as String)
                is Int -> RandomGenerator(stringblur.key as Int)
                else -> null
            } ?: throw GradleException(Logger.text("加密key不能为空"))

            // 使用 Provider 避免配置阶段 namespace 未就绪的问题
            val applicationId = variant.namespace
            val modes = ModeUtils.resolveModes(stringblur.modes)
            val reportPath = target.layout.buildDirectory
                .file("reports/${Constant.PLUGIN_NAME}/${variant.name}.txt")
                .map { it.asFile.absolutePath }

            StringBlurReport.init(
                reportPath.get(),
                variant.name,
                modes.joinToString(",") { it.name },
                stringblur.bytesMode.name
            )

            variant.instrumentation.transformClassesWith(
                StringBlurClassTransform::class.java,
                InstrumentationScope.ALL
            ) { params ->
                params.setParams(generator, applicationId, stringblur, variant.name, reportPath, modes)
            }

            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_CLASSES)

            StringBlurTask.execute(
                target,
                variant,
                applicationId,
                modes
            )
        }

        appendImplementations(target)
    }

    private fun appendImplementations(project: Project) {
        project.dependencies.add(
            "implementation",
            "com.android.string.plugin:common:1.1.6"
        )
    }
}
