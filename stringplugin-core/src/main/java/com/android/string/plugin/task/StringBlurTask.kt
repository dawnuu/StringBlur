package com.android.string.plugin.task

import com.android.build.api.variant.Variant
import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.build.StringBlurFile
import com.android.string.plugin.util.KeyDerivation
import com.android.string.plugin.util.Logger
import com.android.string.plugin.util.ModeUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale
import javax.inject.Inject

@CacheableTask
abstract class StringBlurTask @Inject constructor() : DefaultTask() {
    @TaskAction
    fun injectSource() {
        val appId = applicationId.get()
        val modeList = modes.get()

        // This task owns its generated source directory. Clear previous algorithms,
        // wrapper names and packages when configuration changes, not just old wrappers.
        val outputDir = dir.get().asFile
        check(outputDir.deleteRecursively()) { "Cannot clear generated StringBlur sources: $outputDir" }
        val child = Constant.PLUGIN_CLASS_PACKAGE.format(appId).replace(".", "/")
        val path = File(outputDir, child)
        Logger.log("路径$path")
        check(path.mkdirs() || path.isDirectory) { "Cannot create generated StringBlur sources: $path" }
        modeList.forEach { mode ->
            ModeUtils.getEncodeImplFile(mode).create(path, appId)
        }
        StringBlurFile(KeyDerivation.master(seed.get())).create(path, appId, modeList, wrapperClassName.get(), wrapperMethodName.get())
    }

    @get:Input
    abstract val applicationId: Property<String>

    @get:OutputDirectory
    abstract val dir: DirectoryProperty

    @get:Input
    abstract val modes: ListProperty<Mode>

    @get:Input
    abstract val seed: Property<String>

    @get:Input
    abstract val wrapperClassName: Property<String>

    @get:Input
    abstract val wrapperMethodName: Property<String>

    companion object {
        private fun String.capitalizeCompat(): String {
            return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }

        fun execute(
            project: Project,
            variant: Variant,
            applicationId: Provider<String>,
            modes: List<Mode>,
            seed: Provider<String>,
            wrapperClassName: Provider<String>,
            wrapperMethodName: Provider<String>
        ) {
            val name = variant.name.capitalizeCompat()
            val taskName = "generate${Constant.PLUGIN_CLASS_NAME}$name"
            val provider = project.tasks.register(taskName, StringBlurTask::class.java) { task ->
                task.applicationId.set(applicationId)
                task.modes.addAll(modes)
                task.seed.set(seed)
                task.wrapperClassName.set(wrapperClassName)
                task.wrapperMethodName.set(wrapperMethodName)
            }
            variant.sources.java?.addGeneratedSourceDirectory(provider, StringBlurTask::dir)
        }
    }
}
