package com.android.string.plugin.mode

/**
 * @author chancey
 * @date   2025/6/29
 **/
enum class Mode {
    DEFAULT,      // Base64默认加密
    XOR,          // 基础XOR加密
    REVERSE,      // 字节反转
    SHIFT,        // 位移加密
    XOR_SHIFT,    // XOR+位移组合
    XOR_SIMD,     // SIMD优化的批量XOR（高性能）
    FAST_ROT,     // 快速旋转加密
}