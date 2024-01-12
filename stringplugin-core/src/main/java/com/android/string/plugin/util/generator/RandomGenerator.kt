package com.android.string.plugin.util.generator

import android.util.Base64
import java.security.SecureRandom

/**
 * @author chancey
 * @date   2024/1/12   10:13
 **/
class RandomGenerator(size: Int) : Generator {
    private val secureRandom = SecureRandom()
    private val bytes = ByteArray(size)
    override fun generate(): String {
        secureRandom.nextBytes(bytes)
        return String(Base64.encode(bytes, Base64.NO_WRAP))
    }
}