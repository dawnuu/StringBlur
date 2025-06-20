package com.android.string.plugin.wrapper

import com.android.string.plugin.IString
import com.android.string.plugin.demo_files.StringEncodeImpl

/**
 * @author chancey
 * @date   2025/6/20
 **/
class StringBlurWrapper(customEncodeClass: String?) : IString {
    private val impl = if (customEncodeClass.isNullOrBlank()) {
        StringEncodeImpl()
    } else {
        Class.forName(customEncodeClass).getDeclaredConstructor().newInstance() as IString
    }

    override fun encrypt(data: ByteArray?, key: String?): ByteArray? {
        return impl.encrypt(data, key)
    }

    override fun decrypt(data: ByteArray?, key: ByteArray?): ByteArray? {
        return impl.decrypt(data, key)
    }
}