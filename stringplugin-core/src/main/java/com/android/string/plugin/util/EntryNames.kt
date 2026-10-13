package com.android.string.plugin.util

import java.security.MessageDigest

/**
 * 从配置派生运行时解密入口的类名与方法名。
 * 同一配置下名字保持稳定（增量构建与构建缓存不受影响），
 * 不同项目、variant、算法组合的入口互不相同，
 * 使针对固定入口 <appId>.stringblur.StringBlur.decrypt 的通用 hook 脚本失效。
 *
 * @author chancey
 * @date   2026/8/30
 **/
object EntryNames {

    private const val CLASS_PREFIX = "Sb"
    private const val METHOD_PREFIX = "d"
    private const val CLASS_HEX_LENGTH = 8
    private const val METHOD_HEX_LENGTH = 6

    /**
     * 返回 (className, methodName)，均为合法 Java 标识符。
     */
    fun derive(seed: String): Pair<String, String> {
        val hex = MessageDigest.getInstance("SHA-256")
            .digest(seed.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val className = CLASS_PREFIX + hex.substring(0, CLASS_HEX_LENGTH)
        val methodName = METHOD_PREFIX + hex.substring(CLASS_HEX_LENGTH, CLASS_HEX_LENGTH + METHOD_HEX_LENGTH)
        return className to methodName
    }
}
