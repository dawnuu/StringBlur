package com.android.string.plugin.mode

/**
 * @author chancey
 * @date   2025/6/29
 **/
enum class Mode {
    DEFAULT,      // Base64默认加密
    XOR,          // 基础XOR加密
    XOR_SHIFT,    // XOR+位移组合
    FAST_ROT,     // 快速旋转加密
    XOR_CHAIN,    // 链式XOR，使用前一个密文字节
    LCG_STREAM,   // 整条key播种LCG密钥流
}
