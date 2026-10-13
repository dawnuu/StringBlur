package com.android.string.plugin.util

import java.security.MessageDigest

/**
 * 逐字符串密钥派生。
 * 构建期用 [master] 与 [derive] 为每个字符串算出独立 key；运行时的解密入口类里
 * 生成了与 [derive] 逐行对应的 Java 实现（见 StringBlurFile），两边必须保持一致，
 * 由 KeyDerivationRoundTripTest 编译生成代码并比对。
 *
 * @author chancey
 * @date   2026/10/12
 **/
object KeyDerivation {

    const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    const val KEY_LENGTH = 16
    const val MULTIPLIER = -7046029254386353131L // 0x9E3779B97F4A7C15

    /** 由配置派生的 seed 得到 8 个 int 的主密钥材料 */
    fun master(seed: String): IntArray {
        val digest = MessageDigest.getInstance("SHA-256").digest("master|$seed".toByteArray(Charsets.UTF_8))
        return IntArray(8) { i ->
            ((digest[i * 4].toInt() and 0xff) shl 24) or
                ((digest[i * 4 + 1].toInt() and 0xff) shl 16) or
                ((digest[i * 4 + 2].toInt() and 0xff) shl 8) or
                (digest[i * 4 + 3].toInt() and 0xff)
        }
    }

    /** 由主密钥材料与字符串的 tag 派生该字符串的 key（纯 ASCII，长度 [KEY_LENGTH]） */
    fun derive(master: IntArray, tag: Int): String {
        var h = tag.toLong()
        for (m in master) {
            h = h * MULTIPLIER + m
        }
        if (h == 0L) {
            h = 1L
        }
        val chars = CharArray(KEY_LENGTH)
        for (i in 0 until KEY_LENGTH) {
            h = h xor (h shl 13)
            h = h xor (h ushr 7)
            h = h xor (h shl 17)
            chars[i] = ALPHABET[((h ushr 33) % ALPHABET.length).toInt()]
        }
        return String(chars)
    }
}
