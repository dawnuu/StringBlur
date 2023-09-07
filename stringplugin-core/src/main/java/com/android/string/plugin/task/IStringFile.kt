package com.android.string.plugin.task

import com.squareup.javawriter.JavaWriter

/**
 * 生成java代码
 * IString接口类，内容见com.android.string.plugin.stringblur.IString
 * @author chancey
 * @date   2023/9/5   20:14
 **/
private const val CLASS_NAME = "IString"

class IStringFile : BaseFile() {
    override fun write(writer: JavaWriter, data: StringBlurTaskData) {
        writer.emitPackage("${data.applicationId}.${data.pkg}")
            .beginType(CLASS_NAME, "interface")
            .beginMethod(
                String::class.java.simpleName,
                "encrypt",
                mutableSetOf(),
                String::class.java.simpleName,
                "data",
                String::class.java.simpleName,
                "key"
            )
            .endMethod()
            .beginMethod(
                String::class.java.simpleName,
                "decrypt",
                mutableSetOf(),
                String::class.java.simpleName,
                "data",
                String::class.java.simpleName,
                "key"
            )
            .endMethod()
            .beginMethod(
                Boolean::class.java.simpleName,
                "overflow",
                mutableSetOf(),
                String::class.java.simpleName,
                "data",
                String::class.java.simpleName,
                "key"
            )
            .endMethod()
            .endType()
    }

    override fun getFileName(data: StringBlurTaskData) = "$CLASS_NAME.java"

}