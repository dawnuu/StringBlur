package com.android.string.plugin.util

/**
 * @author chancey
 * @date   2023/9/5   17:11
 **/
object Logger {
    private const val TAG = "StringBlurPlugin :"
    fun log(any: Any) {
        println("$TAG$any")
    }

    fun text(s: String) = "$TAG$s"
}