package com.android.string.plugin.task

import com.android.build.api.variant.Variant
import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.build.StringBlurFile
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
import org.gradle.configurationcache.extensions.capitalized
import java.io.File
import javax.inject.Inject

/**
 * @author chancey
 * @date   2023/9/5   16:58
 **/
@CacheableTask
abstract class StringBlurTask @Inject constructor() : DefaultTask() {
    @TaskAction
    fun injectSource() {
        val child = Constant.PLUGIN_CLASS_PACKAGE.format(applicationId.get()).replace(".", "/")
        val path = File(dir.get().asFile, child)
        Logger.log("路径$path")
        if (!path.exists()) {
            path.mkdirs()
        }
        val modes = modes.get()
        modes.forEach { mode ->
            ModeUtils.getEncodeImplFile(mode).create(path, applicationId.get(), mode)
        }
        StringBlurFile().create(path, applicationId.get(), modes)
    }

    @get:Input
    abstract val applicationId: Property<String>

    @get:OutputDirectory
    abstract val dir: DirectoryProperty

    @get:Input
    abstract val modes: ListProperty<Mode>

    companion object {
        fun execute(
            project: Project,
            variant: Variant,
            applicationId: Provider<String>,
            modes: List<Mode>
        ) {
            val name = variant.name.capitalized()
            val taskName = "generate${Constant.PLUGIN_CLASS_NAME}$name"
            val provider = project.tasks.register(taskName, StringBlurTask::class.java) {
                it.applicationId.set(applicationId)
                it.modes.addAll(modes)
            }
            variant.sources.java?.addGeneratedSourceDirectory(provider, StringBlurTask::dir)
        }
    }
}
