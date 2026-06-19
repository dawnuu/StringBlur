package com.android.string.plugin.task.build

import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.BaseFile
import com.squareup.javawriter.JavaWriter
import javax.lang.model.element.Modifier

/**
 * 字节反转加密算法专用文件生成器
 * 生成的代码使用反转算法实现最高性能
 *
 * @author chancey
 * @date 2023/9/5
 **/
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
            .emitStatement("int left = 0")
            .emitStatement("int right = data.length - 1")
            .beginControlFlow("while (left < right)")
            .emitStatement("byte temp = data[left]")
            .emitStatement("data[left] = data[right]")
            .emitStatement("data[right] = temp")
            .emitStatement("left++")
            .emitStatement("right--")
            .endControlFlow()
            .emitStatement("return data")
            .endMethod()
            .endType()
    }

    override fun getFileName(applicationId: String) = "${Constant.REVERSE_IMPL_CLASS_NAME}.java"
}