package com.android.string.plugin.task

import com.android.string.plugin.data.Constant
import com.squareup.javawriter.JavaWriter
import javax.lang.model.element.Modifier

/**
 * 生成java代码：StringBlur.java
 * @author chancey
 * @date   2023/9/5   17:24
 **/
class StringBlurFile : BaseFile() {

    override fun write(writer: JavaWriter, applicationId: String) {
        writer.emitPackage(Constant.PLUGIN_PACKAGE.format(applicationId))
            .emitEmptyLine()
            .emitImports(Constant.PLUGIN_IMPL_CLASS_PACKAGE.format(applicationId))
            .emitEmptyLine()
            .beginType(
                Constant.PLUGIN_CLASS_NAME,
                "class",
                mutableSetOf(Modifier.PUBLIC, Modifier.FINAL),
            )
            .emitField(
                Constant.PLUGIN_IMPL_CLASS_NAME,
                "IMPL",
                mutableSetOf(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
                "new ${Constant.PLUGIN_IMPL_CLASS_NAME}()"
            )
            .beginMethod(
                String::class.java.simpleName,
                "decrypt",
                mutableSetOf(Modifier.PUBLIC, Modifier.STATIC),
                String::class.java.simpleName,
                "value",
                String::class.java.simpleName,
                "key"
            )
            .emitStatement("return IMPL.decrypt(value,key)")
            .endMethod()
            .emitEmptyLine()
            .endType()
    }

    override fun getFileName(applicationId: String) = "${Constant.PLUGIN_CLASS_NAME}.java"
}