package com.android.string.plugin.util

/**
 * @author chancey
 * @date   2023/9/5   20:48
 **/
object WhileLists {
    private val whileLists = mutableSetOf("BuildConfig", "R2", "R", "StringEncodeImpl", "IString")
    fun add(className: String) {
        whileLists += className
    }

    fun add(classList: List<String>) {
        whileLists += classList
    }

    fun contains(className: String): Boolean {
        for (whileList in whileLists) {
            if (whileList == getShortName(className) || className.startsWith(whileList)) {
                Logger.log("白名单:$className")
                return true
            }
        }
        return false
    }

    private fun getShortName(className: String): String {
        return className.substring(className.lastIndexOf(".") + 1)
    }
}