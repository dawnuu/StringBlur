package com.android.string.plugin.task.build

import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.BaseFile
import com.squareup.javawriter.JavaWriter
import javax.lang.model.element.Modifier

class ReverseEncodeImplFile : BaseFile() {
    override fun write(writer: JavaWriter, applicationId: String, mode: Mode) {
        val pkg = Constant.PLUGIN_CLASS_PACKAGE.format(applicationId)
        writer.emitPackage(pkg)
            .beginType(
                Constant.REVERSE_IMPL_CLASS_NAME,
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
            .emitStatement("return reverse(data)")
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
            .emitStatement("return reverse(data)")
            .endMethod()
            .beginMethod(
                ByteArray::class.java.simpleName,
                "reverse",
                mutableSetOf(Modifier.PRIVATE),
                ByteArray::class.java.simpleName,
                "data"
            )
            .emitStatement(
                "int left = 0;\n" +
                        "        int right = data.length - 1;\n" +
                        "        while (left < right) {\n" +
                        "            byte temp = data[left];\n" +
                        "            data[left] = data[right];\n" +
                        "            data[right] = temp;\n" +
                        "            left++;\n" +
                        "            right--;\n" +
                        "        }\n" +
                        "        return data"
            )
            .endMethod()
            .endType()
    }

    override fun getFileName(applicationId: String) = "${Constant.REVERSE_IMPL_CLASS_NAME}.java"
}