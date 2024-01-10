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
     * 工具接口类名
     */
    const val PLUGIN_INTERFACE_CLASS_NAME = "IString"

    /**
     * 工具实现类类名
     */
    const val PLUGIN_IMPL_CLASS_NAME = "StringEncodeImpl"

    /**
     * 工具类包名，包名.插件名.StringBlur
     */
    const val PLUGIN_CLASS_PACKAGE = "%s.$PLUGIN_NAME.$PLUGIN_CLASS_NAME"

    /**
     * 工具接口类包名，包名.插件名.IString
     */
    const val PLUGIN_INTERFACE_CLASS_PACKAGE = "%s.$PLUGIN_NAME.$PLUGIN_INTERFACE_CLASS_NAME"

    /**
     * 工具实现类包名，包名.插件名.StringEncodeImpl
     */
    const val PLUGIN_IMPL_CLASS_PACKAGE = "%s.$PLUGIN_NAME.$PLUGIN_IMPL_CLASS_NAME"

    /**
     * 插件包名，其实就是存放插件的包路径
     */
    const val PLUGIN_PACKAGE = "%s.$PLUGIN_NAME"

    /**
     *工具类输出路径
     *
     */
    const val OUTPUT_PATH = "generated/source/$PLUGIN_NAME/%s"

    /**
     * 构建任务名
     */
    const val BUILD_TASK_NAME = "generate$PLUGIN_CLASS_NAME%s"
}