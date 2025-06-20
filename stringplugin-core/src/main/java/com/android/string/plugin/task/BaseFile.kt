package com.android.string.plugin.task

import com.squareup.javawriter.JavaWriter
import java.io.File
import java.io.FileWriter

/**
 * @author chancey
 * @date   2023/9/5   18:58
 **/
abstract class BaseFile {

    fun create(path: File, applicationId: String, customEncodeClass: String?) {
        val file = File(path, getFileName(applicationId))
        JavaWriter(FileWriter(file)).use {
            write(it, applicationId, customEncodeClass)
        }
    }

    abstract fun write(writer: JavaWriter, applicationId: String, customEncodeClass: String?)

    abstract fun getFileName(applicationId: String): String
}