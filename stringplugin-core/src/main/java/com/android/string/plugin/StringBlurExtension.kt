package com.android.string.plugin

import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.mode.SelectionStrategy

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
    var enableWhenDebug: Boolean = false // debug模式下是否启用加密，默认false
    
    // 智能算法选择配置（默认保持原有随机行为，如需智能选择请设置为SelectionStrategy.SMART）
    var selectionStrategy: SelectionStrategy = SelectionStrategy.RANDOM // 算法选择策略
    var performanceWeight: Double = 0.5 // 性能权重 (0.0-1.0)，仅在SelectionStrategy.SMART时生效
    var securityWeight: Double = 0.5 // 安全权重 (0.0-1.0)，仅在SelectionStrategy.SMART时生效
}