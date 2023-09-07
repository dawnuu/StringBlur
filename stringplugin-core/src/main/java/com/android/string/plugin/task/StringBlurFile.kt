package com.android.string.plugin.task

import com.squareup.javawriter.JavaWriter
import java.io.File
import java.io.FileWriter
import javax.lang.model.element.Modifier

/**
 * 生成java代码：StringBlur.java
 * @author chancey
 * @date   2023/9/5   17:24
 **/
class StringBlurFile {

    fun create(path: File, stringBlurTaskData: StringBlurTaskData) {
        val outputFile = File(path, "${stringBlurTaskData.alias}.java")
        JavaWriter(FileWriter(outputFile)).use {
            it.emitPackage("${stringBlurTaskData.applicationId}.${stringBlurTaskData.pkg}")
            it.emitEmptyLine()
            it.emitImports("${stringBlurTaskData.applicationId}.${stringBlurTaskData.pkg}.${StringEncodeImplFile.CLASS_NAME}")
            it.emitEmptyLine()
            it.beginType(
                stringBlurTaskData.alias,
                "class",
                mutableSetOf(Modifier.PUBLIC, Modifier.FINAL),
            )
            it.emitField(
                StringEncodeImplFile.CLASS_NAME,
                "IMPL",
                mutableSetOf(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
                "new ${StringEncodeImplFile.CLASS_NAME}()"
            )
            it.beginMethod(
                String::class.java.simpleName,
                "decrypt",
                mutableSetOf(Modifier.PUBLIC, Modifier.STATIC),
                String::class.java.simpleName,
                "value",
                String::class.java.simpleName,
                "key"
            )
            it.emitStatement("return IMPL.decrypt(value,key)")
            it.endMethod()
            it.emitEmptyLine()
            it.endType()
        }
    }
}