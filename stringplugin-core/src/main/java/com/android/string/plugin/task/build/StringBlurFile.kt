package com.android.string.plugin.task.build

import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.BaseFile
import com.android.string.plugin.util.ModeUtils
import com.squareup.javawriter.JavaWriter
import javax.lang.model.element.Modifier

/**
 * 生成解密调用类，内容见[com.android.string.plugin.demo_files.StringBlur]
 * @author chancey
 * @date   2023/9/5   17:24
 **/
class StringBlurFile : BaseFile() {

    override fun write(writer: JavaWriter, applicationId: String, mode: Mode) {
        val pkg = Constant.PLUGIN_CLASS_PACKAGE.format(applicationId)
        val className = ModeUtils.getEncodeImplClassName(mode)
        val import = ModeUtils.getEncodeImplClassFilePath(mode, applicationId)
        writer.emitPackage(pkg)
            .emitImports(import)
            .beginType(
                Constant.PLUGIN_CLASS_NAME,
                "class",
                mutableSetOf(Modifier.PUBLIC, Modifier.FINAL),
            )
            .emitField(
                className,
                "IMPL",
                mutableSetOf(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
                "new $className()"
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
            .emitStatement("return IMPL.decryptString(value,key)")
            .endMethod()
            .beginMethod(
                String::class.java.simpleName,
                "decrypt",
                mutableSetOf(Modifier.PUBLIC, Modifier.STATIC),
                ByteArray::class.java.simpleName,
                "value",
                ByteArray::class.java.simpleName,
                "key"
            )
            .emitStatement("return IMPL.decryptBytes(value,key)")
            .endMethod()
            .endType()
    }

    override fun getFileName(applicationId: String) = "${Constant.PLUGIN_CLASS_NAME}.java"
}