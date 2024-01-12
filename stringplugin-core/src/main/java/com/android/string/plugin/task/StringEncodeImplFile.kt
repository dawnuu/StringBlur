package com.android.string.plugin.task

import android.util.Base64
import com.android.string.plugin.data.Constant
import com.squareup.javawriter.JavaWriter
import javax.lang.model.element.Modifier

/**
 * 生成java代码
 * IString接口实现类，内容见com.android.string.plugin.files.StringEncodeImpl
 * 这里只生成解密方法
 * @author chancey
 * @date   2023/9/5   18:35
 **/
class StringEncodeImplFile : BaseFile() {
    override fun write(writer: JavaWriter, applicationId: String) {
        writer.emitPackage(Constant.PLUGIN_PACKAGE.format(applicationId))
            .emitImports(Base64::class.java)
            .beginType(
                Constant.PLUGIN_IMPL_CLASS_NAME,
                "class",
                mutableSetOf(Modifier.FINAL),
                Constant.PLUGIN_INTERFACE_CLASS_NAME
            )
            .emitAnnotation(Override::class.java)
            .beginMethod(
                String::class.java.simpleName,
                "decryptBytes",
                mutableSetOf(Modifier.PUBLIC),
                ByteArray::class.java.simpleName,
                "data",
                ByteArray::class.java.simpleName,
                "key"
            )
            .emitStatement("return new String(decrypt(Base64.decode(data, Base64.NO_WRAP), key))")
            .endMethod()
            .emitAnnotation(Override::class.java)
            .beginMethod(
                String::class.java.simpleName,
                "decryptString",
                mutableSetOf(Modifier.PUBLIC),
                String::class.java.simpleName,
                "data",
                String::class.java.simpleName,
                "key"
            )
            .emitStatement("return decryptBytes(Base64.decode(data, Base64.NO_WRAP), key.getBytes())")
            .endMethod()
            .endType()
    }

    override fun getFileName(applicationId: String) = "${Constant.PLUGIN_IMPL_CLASS_NAME}.java"
}