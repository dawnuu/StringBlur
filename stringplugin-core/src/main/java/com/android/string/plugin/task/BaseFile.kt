package com.android.string.plugin.task

import com.squareup.javawriter.JavaWriter
import java.io.File
import java.io.FileWriter

/**
 * @author chancey
 * @date   2023/9/5   18:58
 **/
abstract class BaseFile {

    fun create(path: File, data: StringBlurTaskData) {
        val file = File(path, getFileName(data))
        JavaWriter(FileWriter(file)).use {
            write(it, data)
        }
    }

    abstract fun write(writer: JavaWriter, data: StringBlurTaskData)

    abstract fun getFileName(data: StringBlurTaskData): String
}