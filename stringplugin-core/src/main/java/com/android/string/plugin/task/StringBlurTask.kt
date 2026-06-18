package com.android.string.plugin.task

import com.android.build.api.variant.Variant
import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.report.StringBlurReport
import com.android.string.plugin.task.build.StringBlurFile
import com.android.string.plugin.util.Logger
import com.android.string.plugin.util.ModeUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
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
        val reportFile = reportPath.asFile.get()

        StringBlurReport.init(
            reportFile.absolutePath,
            variantName.get(),
            modeList.joinToString(",") { it.name },
            bytesMode.get().name
        )

        val child = Constant.PLUGIN_CLASS_PACKAGE.format(appId).replace(".", "/")
        val path = File(dir.get().asFile, child)
        Logger.log("路径$path")
        if (!path.exists()) {
            path.mkdirs()
        }
        modeList.forEach { mode ->
            ModeUtils.getEncodeImplFile(mode).create(path, appId, mode)
        }
        StringBlurFile().create(path, appId, modeList)

        StringBlurReport.generateSummary(reportFile.absolutePath)
        Logger.log("StringBlur performance report generated: ${reportFile.absolutePath}")
    }

    @get:Input
    abstract val applicationId: Property<String>

    @get:Input
    abstract val variantName: Property<String>

    @get:Input
    abstract val bytesMode: Property<BytesMode>

    @get:OutputDirectory
    abstract val dir: DirectoryProperty

    @get:OutputFile
    abstract val reportPath: RegularFileProperty

    @get:Input
    abstract val modes: ListProperty<Mode>

    companion object {
        private fun String.capitalizeCompat(): String {
            return replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        }

        fun execute(
            project: Project,
            variant: Variant,
            applicationId: Provider<String>,
            modes: List<Mode>,
            reportFileProvider: Provider<java.io.File>,
            bytesMode: BytesMode
        ) {
            val name = variant.name.capitalizeCompat()
            val taskName = "generate${Constant.PLUGIN_CLASS_NAME}$name"
            val provider = project.tasks.register(taskName, StringBlurTask::class.java) { task ->
                task.applicationId.set(applicationId)
                task.variantName.set(variant.name)
                task.bytesMode.set(bytesMode)
                task.reportPath.fileProvider(reportFileProvider)
                task.modes.addAll(modes)
            }
            variant.sources.java?.addGeneratedSourceDirectory(provider, StringBlurTask::dir)
        }
    }
}
