package com.android.string.plugin

import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode

/**
 * @author chancey
 * @date   2023/9/5   16:07
 **/
abstract class StringBlurExtension {
    var enable: Boolean = false
    var seed: String = "" // 可选的盐，改动会让产物整体变化；不是密钥，无需保密
    var whiteList: List<String> = emptyList()
    var encodePackages: List<String>? = emptyList()
    var modes: List<Mode> = listOf(Mode.DEFAULT)
    var bytesMode: BytesMode = BytesMode.STRING
    var minLength: Int = 0
    var enableWhenDebug: Boolean = false // debug模式下是否启用加密，默认false
    var includeDependencies: Boolean = false // 是否连同第三方依赖一起加密，默认false（仅处理当前项目）

}