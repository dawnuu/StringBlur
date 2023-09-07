package com.android.string.plugin.util

/**
 * @author chancey
 * @date   2023/9/5   20:48
 **/
object WhileLists {
    private val whileLists =
        mutableSetOf("R2?(\$[a-zA-Z0-9]+)?$", "BuildConfig$", "StringEncodeImpl$", "IString$")

    fun add(className: String) {
        whileLists += className
    }

    fun add(classList: List<String>) {
        whileLists += classList
    }

    fun contains(className: String): Boolean {
        var contains = false
        for (whileList in whileLists) {
            if (className.contains(Regex(whileList))) {
                contains = true
                Logger.log("白名单:$className")
                break
            }
        }
        return contains
    }
}