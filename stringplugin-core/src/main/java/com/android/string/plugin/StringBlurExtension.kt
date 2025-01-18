package com.android.string.plugin

/**
 * @author chancey
 * @date   2023/9/5   16:07
 **/
abstract class StringBlurExtension {
    var key: Any? = null
    var useBytes: Boolean = false
    var enable: Boolean = false
    var whiteList: List<String> = emptyList()
    var encodePackages: List<String>? = emptyList()
}