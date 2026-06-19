package com.android.string.plugin.task.build

import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.BaseFile
import com.squareup.javawriter.JavaWriter
import javax.lang.model.element.Modifier

/**
 * 生成java代码
 * IString接口实现类，内容见[com.android.string.plugin.demo_files.DefaultEncodeImpl]
 * @author chancey
 * @date   2023/9/5   18:35
 **/
class DefaultEncodeImplFile : BaseFile() {
    override fun write(writer: JavaWriter, applicationId: String, mode: Mode) {
        val pkg = Constant.PLUGIN_CLASS_PACKAGE.format(applicationId)
        writer.emitPackage(pkg)
            .beginType(
                Constant.DEFAULT_IMPL_CLASS_NAME,
                "class",
                mutableSetOf(Modifier.PUBLIC, Modifier.FINAL),
                null,
                Constant.ABSTRACT_CLASS_NAME
            )
            .emitAnnotation(Override::class.java)
            .beginMethod(
                ByteArray::class.java.simpleName,
                "encrypt",
                mutableSetOf(Modifier.PUBLIC),
                ByteArray::class.java.simpleName,
                "data",
                String::class.java.simpleName,
                "key"
            )
            .emitStatement("int lenKey = key.length()")
            .emitStatement("int j = 0")
            .beginControlFlow("for (int i = 0; i < data.length; i++)")
            .beginControlFlow("if (j >= lenKey)")
            .emitStatement("j = 0")
            .endControlFlow()
            .emitStatement("data[i] = (byte) (data[i] + key.charAt(j))")
            .emitStatement("j++")
            .endControlFlow()
            .emitStatement("return data")
            .endMethod()
            .emitAnnotation(Override::class.java)
            .beginMethod(
                ByteArray::class.java.simpleName,
                "decrypt",
                mutableSetOf(Modifier.PUBLIC),
                ByteArray::class.java.simpleName,
                "data",
                ByteArray::class.java.simpleName,
                "key"
            )
            .emitStatement("int lenKey = key.length")
            .emitStatement("int j = 0")
            .beginControlFlow("for (int i = 0; i < data.length; i++)")
            .beginControlFlow("if (j >= lenKey)")
            .emitStatement("j = 0")
            .endControlFlow()
            .emitStatement("data[i] = (byte) (data[i] - key[j])")
            .emitStatement("j++")
            .endControlFlow()
            .emitStatement("return data")
            .endMethod()
            .endType()
    }

    override fun getFileName(applicationId: String) = "${Constant.DEFAULT_IMPL_CLASS_NAME}.java"
}