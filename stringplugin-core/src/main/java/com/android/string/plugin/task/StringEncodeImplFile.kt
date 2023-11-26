package com.android.string.plugin.task

import android.util.Base64
import com.squareup.javawriter.JavaWriter
import java.io.UnsupportedEncodingException
import javax.lang.model.element.Modifier

/**
 * @author chancey
 * @date   2023/9/5   18:35
 **/
class StringEncodeImplFile : BaseFile() {
    companion object {
        const val CLASS_NAME = "StringEncodeImpl"
    }

    override fun write(writer: JavaWriter, data: StringBlurTaskData) {
        writer.emitPackage("${data.applicationId}.${data.pkg}")
            .emitImports(UnsupportedEncodingException::class.java)
            .emitImports(Base64::class.java)
            .beginType(CLASS_NAME, "class", mutableSetOf(Modifier.FINAL), null, "IString")
            .emitField(
                String::class.java.simpleName,
                "CHARSET_NAME_UTF_8",
                mutableSetOf(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL),
                "\"utf-8\""
            )
            .emitAnnotation(Override::class.java)
            .beginMethod(
                String::class.java.simpleName,
                "encrypt",
                mutableSetOf(Modifier.PUBLIC),
                String::class.java.simpleName,
                "data",
                String::class.java.simpleName,
                "key"
            )
            .emitStatement(getEncrypt())
            .endMethod()
            .emitAnnotation(Override::class.java)
            .beginMethod(
                String::class.java.simpleName,
                "decrypt",
                mutableSetOf(Modifier.PUBLIC),
                String::class.java.simpleName,
                "data",
                String::class.java.simpleName,
                "key"
            )
            .emitStatement(getDecrypt())
            .endMethod()
            .emitAnnotation(Override::class.java)
            .beginMethod(
                Boolean::class.java.simpleName,
                "overflow",
                mutableSetOf(Modifier.PUBLIC),
                String::class.java.simpleName,
                "data",
                String::class.java.simpleName,
                "key"
            )
            .emitStatement("return data != null && !data.isBlank()")
            .endMethod()
            .beginMethod(
                ByteArray::class.java.simpleName,
                "encrypt",
                mutableSetOf(Modifier.PRIVATE, Modifier.STATIC),
                ByteArray::class.java.simpleName,
                "data",
                String::class.java.simpleName,
                "key"
            )
            .emitStatement(getStaticEncrypt())
            .endMethod()
            .beginMethod(
                ByteArray::class.java.simpleName,
                "decrypt",
                mutableSetOf(Modifier.PRIVATE, Modifier.STATIC),
                ByteArray::class.java.simpleName,
                "data",
                String::class.java.simpleName,
                "key"
            )
            .emitStatement(getStaticDecrypt())
            .endMethod()
            .endType()
    }

    private fun getStaticEncrypt(): String {
        return "int lenKey = key.length();int j = 0;for (int i = 0; i < data.length; i++) {if (j >= lenKey) {j = 0;}data[i] = (byte) (data[i] + key.charAt(j));j++;}return data"
    }

    private fun getStaticDecrypt(): String {
        return "int lenKey = key.length();int j = 0;for (int i = 0; i < data.length; i++) {if (j >= lenKey) {j = 0;}data[i] = (byte) (data[i] - key.charAt(j));j++;}return data"
    }

    private fun getEncrypt(): String {
        return "String newData;try {newData = new String(Base64.encode(encrypt(data.getBytes(CHARSET_NAME_UTF_8), key), Base64.NO_WRAP));} catch (UnsupportedEncodingException e) {newData = new String(Base64.encode(encrypt(data.getBytes(), key), Base64.NO_WRAP));}return newData"
    }

    private fun getDecrypt(): String {
        return "String newData;try {newData = new String(decrypt(Base64.decode(data, Base64.NO_WRAP), key), CHARSET_NAME_UTF_8);} catch (UnsupportedEncodingException e) {newData = new String(decrypt(Base64.decode(data, Base64.NO_WRAP), key));}return newData"
    }

    override fun getFileName(data: StringBlurTaskData) = "${CLASS_NAME}.java"
}