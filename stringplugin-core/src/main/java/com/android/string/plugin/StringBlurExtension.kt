package com.android.string.plugin

/**
 * @author chancey
 * @date   2023/9/5   16:07
 **/
abstract class StringBlurExtension {
    var key: String = ""
    var enable: Boolean = false
    var whiteList: List<String> = emptyList()
    var encodePackages: List<String> = emptyList()
    var pkg: String = "stringblur"
    var alias: String = "StringBlur"
}