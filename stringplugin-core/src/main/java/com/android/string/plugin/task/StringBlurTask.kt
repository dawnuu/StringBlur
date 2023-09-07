package com.android.string.plugin.task

import com.android.build.gradle.api.BaseVariant
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
            File(dir.get(), "${data.get().applicationId}/${data.get().pkg}".replace(".", "/"))
        Logger.log("路径$path")
        if (!path.exists()) {
            path.mkdirs()
        }
        IStringFile().create(path, data.get())
        StringEncodeImplFile().create(path, data.get())
        StringBlurFile().create(path, data.get())
    }

    @get:Input
    abstract val data: Property<StringBlurTaskData>

    @get:Input
    abstract val dir: Property<File>

    companion object {
        fun execute(project: Project, variant: BaseVariant, data: StringBlurTaskData) {
            val name = variant.name.capitalized()
            val taskName = "generateStringBlur$name"
            if (project.getTasksByName(taskName, true).isNotEmpty()) {
                return
            }
            val dir = File(
                project.layout.buildDirectory.get().asFile,
                "generated/source/stringblur/$name"
            )
            val provider = project.tasks.register(taskName, StringBlurTask::class.java) {
                it.data.set(data)
                it.dir.set(dir)
            }
            variant.registerJavaGeneratingTask(provider, dir)
        }
    }
}