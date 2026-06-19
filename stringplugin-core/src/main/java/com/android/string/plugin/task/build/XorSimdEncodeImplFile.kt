package com.android.string.plugin.task.build

import com.android.string.plugin.data.Constant
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.BaseFile
import com.squareup.javawriter.JavaWriter
import javax.lang.model.element.Modifier

/**
 * SIMD优化的XOR加密算法专用文件生成器
 * 生成的代码充分发挥SIMD批量处理性能优势
 *
 * @author chancey
 * @date 2026/6/19
 **/
class XorSimdEncodeImplFile : BaseFile() {
    override fun write(writer: JavaWriter, applicationId: String, mode: Mode) {
        val pkg = Constant.PLUGIN_CLASS_PACKAGE.format(applicationId)
        writer.emitPackage(pkg)
            .beginType(
                Constant.XOR_SIMD_IMPL_CLASS_NAME,
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
            .emitStatement("byte[] keyBytes = key.getBytes()")
            .emitStatement("if (keyBytes.length == 0) return data")
            .emitEmptyLine()
            .emitStatement("int dataLen = data.length")
            .emitStatement("int keyLen = keyBytes.length")
            .emitEmptyLine()
            .emitStatement("// 8字节批量处理 (SIMD风格优化)")
            .emitStatement("int longLen = dataLen / 8")
            .beginControlFlow("if (longLen > 0 && keyLen >= 8)")
            .emitStatement("long[] keyPattern = generateKeyPattern(keyBytes)")
            .emitEmptyLine()
            .emitStatement("// 批量处理8字节块")
            .beginControlFlow("for (int i = 0; i < longLen; i++)")
            .emitStatement("int offset = i * 8")
            .emitStatement("long dataChunk = ((long) data[offset] & 0xFF) |")
            .emitStatement("               (((long) data[offset + 1] & 0xFF) << 8) |")
            .emitStatement("               (((long) data[offset + 2] & 0xFF) << 16) |")
            .emitStatement("               (((long) data[offset + 3] & 0xFF) << 24) |")
            .emitStatement("               (((long) data[offset + 4] & 0xFF) << 32) |")
            .emitStatement("               (((long) data[offset + 5] & 0xFF) << 40) |")
            .emitStatement("               (((long) data[offset + 6] & 0xFF) << 48) |")
            .emitStatement("               (((long) data[offset + 7] & 0xFF) << 56)")
            .emitEmptyLine()
            .emitStatement("long encrypted = dataChunk ^ keyPattern[i %% keyPattern.length]")
            .emitEmptyLine()
            .emitStatement("data[offset] = (byte) (encrypted & 0xFF)")
            .emitStatement("data[offset + 1] = (byte) ((encrypted >> 8) & 0xFF)")
            .emitStatement("data[offset + 2] = (byte) ((encrypted >> 16) & 0xFF)")
            .emitStatement("data[offset + 3] = (byte) ((encrypted >> 24) & 0xFF)")
            .emitStatement("data[offset + 4] = (byte) ((encrypted >> 32) & 0xFF)")
            .emitStatement("data[offset + 5] = (byte) ((encrypted >> 40) & 0xFF)")
            .emitStatement("data[offset + 6] = (byte) ((encrypted >> 48) & 0xFF)")
            .emitStatement("data[offset + 7] = (byte) ((encrypted >> 56) & 0xFF)")
            .endControlFlow()
            .endControlFlow()
            .emitEmptyLine()
            .emitStatement("// 处理剩余字节")
            .beginControlFlow("for (int i = longLen * 8; i < dataLen; i++)")
            .emitStatement("data[i] = (byte) (data[i] ^ keyBytes[i %% keyLen])")
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
            .emitStatement("return encrypt(data, new String(key))")
            .endMethod()
            .emitEmptyLine()
            .beginMethod(
                "long[]",
                "generateKeyPattern",
                mutableSetOf(Modifier.PRIVATE),
                ByteArray::class.java.simpleName,
                "keyBytes"
            )
            .emitStatement("int patternCount = Math.max(1, 256 / keyBytes.length)")
            .emitStatement("long[] patterns = new long[patternCount]")
            .emitEmptyLine()
            .beginControlFlow("for (int i = 0; i < patternCount; i++)")
            .emitStatement("long pattern = 0")
            .beginControlFlow("for (int j = 0; j < 8; j++)")
            .emitStatement("pattern |= ((long) keyBytes[(i * 8 + j) %% keyBytes.length] & 0xFF) << (j * 8)")
            .endControlFlow()
            .emitStatement("patterns[i] = pattern")
            .endControlFlow()
            .emitStatement("return patterns")
            .endMethod()
            .endType()
    }

    override fun getFileName(applicationId: String) = "${Constant.XOR_SIMD_IMPL_CLASS_NAME}.java"
}