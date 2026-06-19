package com.android.string.plugin.task.build

import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.BaseFile
import com.squareup.javawriter.JavaWriter
import javax.lang.model.element.Modifier

/**
 * 位移加密算法专用文件生成器
 * 生成的代码使用密钥低位进行位移操作
 *
 * @author chancey
 * @date 2023/9/5
 **/
class ShiftEncodeImplFile : BaseFile() {
    override fun write(writer: JavaWriter, applicationId: String, mode: Mode) {
        val pkg = Constant.PLUGIN_CLASS_PACKAGE.format(applicationId)
        writer.emitPackage(pkg)
            .beginType(
                Constant.SHIFT_IMPL_CLASS_NAME,
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
            .emitStatement("return shift(data, key.getBytes(), 1)")
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
            .emitStatement("return shift(data, key, -1)")
            .endMethod()
            .beginMethod(
                ByteArray::class.java.simpleName,
                "shift",
                mutableSetOf(Modifier.PRIVATE),
                ByteArray::class.java.simpleName,
                "data",
                ByteArray::class.java.simpleName,
                "key",
                "int",
                "direction"
            )
            .emitStatement("int lenKey = key.length")
            .beginControlFlow("for (int i = 0; i < data.length; i++)")
            .emitStatement("int offset = key[i %% lenKey] & 0x0F")
            .emitStatement("data[i] = (byte) (data[i] + direction * offset)")
            .endControlFlow()
            .emitStatement("return data")
            .endMethod()
            .endType()
    }

    override fun getFileName(applicationId: String) = "${Constant.SHIFT_IMPL_CLASS_NAME}.java"
}