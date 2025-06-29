package com.android.string.plugin.data

/**
 * @author chancey
 * @date   2024/1/10   21:31
 **/
object Constant {
    /**
     * 插件名称
     */
    const val PLUGIN_NAME = "stringblur"

    /**
     * 工具类名
     */
    const val PLUGIN_CLASS_NAME = "StringBlur"

    /**
     * 加解密类路径
     */
    const val PLUGIN_CLASS_PACKAGE = "%s.$PLUGIN_NAME"

    /**
     * 加解密类完整路径
     */
    const val PLUGIN_CLASS_FILE_PATH = "$PLUGIN_CLASS_PACKAGE.$PLUGIN_CLASS_NAME"

    /**
     *  默认加解密实现类类名
     */
    const val DEFAULT_IMPL_CLASS_NAME = "DefaultEncodeImpl"

    /**
     * xor加解密实现类类名
     */
    const val XOR_IMPL_CLASS_NAME = "XorEncodeImpl"

    /**
     * 加解密抽象接口
     */
    const val ABSTRACT_CLASS_NAME = "com.android.string.plugin.IString"

    /**
     * 默认用于输出到应用层的加密类完整路径
     */
    const val DEFAULT_IMPL_CLASS_FILE_PATH = "$PLUGIN_CLASS_PACKAGE.$DEFAULT_IMPL_CLASS_NAME"
    const val XOR_IMPL_CLASS_FILE_PATH = "$PLUGIN_CLASS_PACKAGE.$XOR_IMPL_CLASS_NAME"
}