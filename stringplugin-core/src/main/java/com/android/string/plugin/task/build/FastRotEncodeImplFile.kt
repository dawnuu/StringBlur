package com.android.string.plugin.task.build

import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.BaseFile
import com.squareup.javawriter.JavaWriter
import javax.lang.model.element.Modifier

/**
 * 快速旋转加密算法专用文件生成器
 * 生成的代码充分发挥位旋转的性能优势
 *
 * @author chancey
 * @date 2026/6/19
 **/
class FastRotEncodeImplFile : BaseFile() {
    override fun write(writer: JavaWriter, applicationId: String, mode: Mode) {
        val pkg = Constant.PLUGIN_CLASS_PACKAGE.format(applicationId)
        writer.emitPackage(pkg)
            .beginType(
                Constant.FAST_ROT_IMPL_CLASS_NAME,
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
            .emitStatement("if (data == null || data.length == 0 || key == null) return data")
            .emitEmptyLine()
            .emitStatement("// 使用key生成1-7的旋转值")
            .emitStatement("int rotation = Math.abs(key.hashCode()) %% 7 + 1")
            .emitEmptyLine()
            .beginControlFlow("for (int i = 0; i < data.length; i++)")
            .emitStatement("data[i] = rotateLeft(data[i], rotation)")
            .endControlFlow()
            .emitStatement("return data")
            .endMethod()
            .emitEmptyLine()
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
            .emitStatement("if (data == null || data.length == 0 || key == null) return data")
            .emitEmptyLine()
            .emitStatement("int rotation = Math.abs(new String(key).hashCode()) %% 7 + 1")
            .emitEmptyLine()
            .beginControlFlow("for (int i = 0; i < data.length; i++)")
            .emitStatement("data[i] = rotateRight(data[i], rotation)")
            .endControlFlow()
            .emitStatement("return data")
            .endMethod()
            .emitEmptyLine()
            .beginMethod(
                "byte",
                "rotateLeft",
                mutableSetOf(Modifier.PRIVATE, Modifier.STATIC),
                "byte",
                "value",
                "int",
                "positions"
            )
            .emitStatement("return (byte) (((value & 0xFF) << positions) | ((value & 0xFF) >>> (8 - positions)))")
            .endMethod()
            .emitEmptyLine()
            .beginMethod(
                "byte",
                "rotateRight",
                mutableSetOf(Modifier.PRIVATE, Modifier.STATIC),
                "byte",
                "value",
                "int",
                "positions"
            )
            .emitStatement("return (byte) (((value & 0xFF) >>> positions) | ((value & 0xFF) << (8 - positions)))")
            .endMethod()
            .endType()
    }

    override fun getFileName(applicationId: String) = "${Constant.FAST_ROT_IMPL_CLASS_NAME}.java"
}