package com.android.string.plugin.task

import com.android.string.plugin.mode.Mode
import com.squareup.javawriter.JavaWriter
import java.io.File
import java.io.FileWriter

/**
 * @author chancey
 * @date   2023/9/5   18:58
 **/
abstract class BaseFile {

    fun create(path: File, applicationId: String, mode: Mode) {
        val file = File(path, getFileName(applicationId))
        JavaWriter(FileWriter(file)).use {
            write(it, applicationId, mode)
        }
    }

    open fun create(path: File, applicationId: String, modes: List<Mode>) {
        val file = File(path, getFileName(applicationId))
        JavaWriter(FileWriter(file)).use {
            write(it, applicationId, modes.first())
        }
    }

    abstract fun write(writer: JavaWriter, applicationId: String, mode: Mode)

    abstract fun getFileName(applicationId: String): String
}