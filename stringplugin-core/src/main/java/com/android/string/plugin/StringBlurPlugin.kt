package com.android.string.plugin

import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.string.plugin.data.Constant
import com.android.string.plugin.task.StringBlurTask
import com.android.string.plugin.trasform.StringBlurClassTransform
import com.android.string.plugin.util.EntryNames
import com.android.string.plugin.util.Logger
import com.android.string.plugin.util.SeedBuilder
import com.android.string.plugin.util.ModeUtils
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
            
            val isDebugBuild = variant.buildType == "debug"
            if (isDebugBuild && !stringblur.enableWhenDebug) {
                Logger.log("Debug模式下加密已关闭")
                return@onVariants
            }
            // AGP 8.13 起 namespace 为 Provider<String>，且在 onVariants 阶段不可 .get()，
            // 全部入口名与包装类路径必须惰性求值，否则会得到 "property 'namespace'" 这类垃圾值
            val applicationIdProvider = variant.namespace
            val modes = ModeUtils.resolveModes(stringblur.modes)
            // 种子由配置稳定派生：配置不变则产物不变（保证增量构建与构建缓存），
            // 解密入口名与运行时主密钥材料都由它得出
            val seedProvider = applicationIdProvider.map { appId ->
                SeedBuilder.build(GRADLE_VERSION, variant.name, appId, modes, stringblur.bytesMode, stringblur.seed)
            }
            val wrapperNamesProvider = seedProvider.map { EntryNames.derive(it) }
            val wrapperClassName = wrapperNamesProvider.map { it.first }
            val wrapperMethodName = wrapperNamesProvider.map { it.second }
            val wrapperClass = applicationIdProvider.zip(wrapperNamesProvider) { appId, names ->
                "${Constant.PLUGIN_CLASS_PACKAGE.format(appId).replace(".", "/")}/${names.first}"
            }
            val reportFile = target.layout.buildDirectory
                .file("reports/${Constant.PLUGIN_NAME}/${variant.name}.txt")
            val reportPathString = reportFile.map { it.asFile.absolutePath }
            val reportPathFile = reportFile.map { it.asFile }

            variant.instrumentation.transformClassesWith(
                StringBlurClassTransform::class.java,
                InstrumentationScope.ALL
            ) { params ->
                params.setParams(seedProvider, applicationIdProvider, stringblur, variant.name, reportPathString, modes, wrapperClass, wrapperMethodName)
            }

            variant.instrumentation.setAsmFramesComputationMode(FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_CLASSES)

            StringBlurTask.execute(
                target,
                variant,
                applicationIdProvider,
                modes,
                reportPathFile,
                stringblur.bytesMode,
                seedProvider,
                wrapperClassName,
                wrapperMethodName
            )
        }

        appendImplementations(target)
    }

    private fun appendImplementations(project: Project) {
        project.dependencies.add(
            "implementation",
            "io.github.dawnuu:common:$GRADLE_VERSION"
        )
    }
}
