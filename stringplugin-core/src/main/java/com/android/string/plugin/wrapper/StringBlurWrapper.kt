package com.android.string.plugin.wrapper

import com.android.string.plugin.IString
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.util.ModeUtils

/**
 * @author chancey
 * @date   2025/6/20
 **/
class StringBlurWrapper(mode: Mode) : IString {
    private val impl = ModeUtils.getEncodeImpl(mode)

    override fun encrypt(data: ByteArray?, key: String?): ByteArray? {
        return impl.encrypt(data, key)
    }

    override fun decrypt(data: ByteArray?, key: ByteArray?): ByteArray? {
        return impl.decrypt(data, key)
    }
}