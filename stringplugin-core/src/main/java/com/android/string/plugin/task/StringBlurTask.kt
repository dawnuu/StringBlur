package com.android.string.plugin.task

import com.android.build.gradle.api.BaseVariant
import com.android.string.plugin.data.Constant
import com.android.string.plugin.util.Logger
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
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
        val path =
            File(dir.get(), Constant.PLUGIN_PACKAGE.format(applicationId.get()).replace(".", "/"))
        Logger.log("路径$path")
        if (!path.exists()) {
            path.mkdirs()
        }
        IStringFile().create(path, applicationId.get())
        StringEncodeImplFile().create(path, applicationId.get())
        StringBlurFile().create(path, applicationId.get())
    }

    @get:Input
    abstract val applicationId: Property<String>

    @get:Input
    abstract val dir: Property<File>

    companion object {
        fun execute(project: Project, variant: BaseVariant, applicationId: String) {
            val name = variant.name.capitalized()
            val taskName = Constant.BUILD_TASK_NAME.format(name)
            if (project.getTasksByName(taskName, true).isNotEmpty()) {
                return
            }
            val dir =
                File(project.layout.buildDirectory.get().asFile, Constant.OUTPUT_PATH.format(name))
            val provider = project.tasks.register(taskName, StringBlurTask::class.java) {
                it.applicationId.set(applicationId)
                it.dir.set(dir)
            }
            variant.registerJavaGeneratingTask(provider, dir)
        }
    }
}