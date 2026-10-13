package com.android.string.plugin.util

import org.junit.Assert.assertEquals
import org.junit.Test

class Utf8CharsetTest {

    // 恒等变换：只看 IString 默认方法在加解密前后用的是什么字符集，不受 JVM 默认字符集影响
    private val identity = object : com.android.string.plugin.IString {
        override fun encrypt(data: ByteArray, key: String) = data
        override fun decrypt(data: ByteArray, key: ByteArray) = data
    }

    @Test
    fun encryptsUtf8BytesRegardlessOfDefaultCharset() {
        val plain = "字符串加密测试：你好，世界"
        val encoded = identity.encryptBytes(plain, "k")
        val raw = android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP)
        assertEquals(plain.toByteArray(Charsets.UTF_8).toList(), raw.toList())
    }

    @Test
    fun decryptsAsUtf8() {
        val plain = "字符串加密测试：你好，世界"
        val encoded = android.util.Base64.encode(plain.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
        assertEquals(plain, identity.decryptBytes(encoded, "k".toByteArray()))
    }
}
