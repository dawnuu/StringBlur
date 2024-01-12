package com.android.string.plugin.task

import com.android.string.plugin.data.Constant
import com.squareup.javawriter.JavaWriter
import javax.lang.model.element.Modifier

/**
 * 生成java代码
 * IString接口类，内容见com.android.string.plugin.stringblur.IString
 * 这里只生成解密方法
 * @author chancey
 * @date   2023/9/5   20:14
 **/
class IStringFile : BaseFile() {
    override fun write(writer: JavaWriter, applicationId: String) {
        writer.emitPackage(Constant.PLUGIN_PACKAGE.format(applicationId))
            .beginType(Constant.PLUGIN_INTERFACE_CLASS_NAME, "abstract class")
            .beginMethod(
                String::class.java.simpleName,
                "decryptBytes",
                mutableSetOf(Modifier.PUBLIC, Modifier.ABSTRACT),
                ByteArray::class.java.simpleName,
                "data",
                ByteArray::class.java.simpleName,
                "key"
            )
            .endMethod()
            .beginMethod(
                String::class.java.simpleName,
                "decryptString",
                mutableSetOf(Modifier.PUBLIC, Modifier.ABSTRACT),
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
                ByteArray::class.java.simpleName,
                "data"
            )
            .emitStatement("return data != null && data.length != 0")
            .endMethod()
            .beginMethod(
                ByteArray::class.java.simpleName,
                "decrypt",
                mutableSetOf(Modifier.PROTECTED, Modifier.STATIC),
                ByteArray::class.java.simpleName,
                "data",
                ByteArray::class.java.simpleName,
                "key"
            )
            .emitStatement(getDecryptCode())
            .endMethod()
            .endType()
    }

    private fun getDecryptCode() = "int lenKey = key.length;\n" +
            "        int j = 0;\n" +
            "        for (int i = 0; i < data.length; i++) {\n" +
            "        if (j >= lenKey) {\n" +
            "            j = 0;\n" +
            "        }\n" +
            "        data[i] = (byte)(data[i] - key[j]);\n" +
            "        j++;\n" +
            "    }\n" +
            "        return data"

    override fun getFileName(applicationId: String) = "${Constant.PLUGIN_INTERFACE_CLASS_NAME}.java"

}