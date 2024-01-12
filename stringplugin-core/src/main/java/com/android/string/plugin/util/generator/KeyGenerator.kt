package com.android.string.plugin.util.generator

/**
 * @author chancey
 * @date   2024/1/12   10:13
 **/
class KeyGenerator(private val key: String) : Generator {
    override fun generate() = key
}