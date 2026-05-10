package com.android.string.plugin

import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode

/**
 * @author chancey
 * @date   2023/9/5   16:07
 **/
abstract class StringBlurExtension {
    var key: Any? = null
    var enable: Boolean = false
    var whiteList: List<String> = emptyList()
    var encodePackages: List<String>? = emptyList()
    var modes: List<Mode> = listOf(Mode.DEFAULT)
    var bytesMode: BytesMode = BytesMode.STRING
    var minLength: Int = 0
}