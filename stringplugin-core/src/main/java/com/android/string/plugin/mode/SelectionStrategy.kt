package com.android.string.plugin.mode

/**
 * 加密算法选择策略
 *
 * @author chancey
 * @date 2026/6/19
 **/
enum class SelectionStrategy {
    /**
     * 完全随机选择加密算法
     */
    RANDOM,
    
    /**
     * 智能选择最佳算法（考虑性能、安全性、字符串特征）
     */
    SMART,
    
    /**
     * 性能优先策略（选择最快的算法）
     */
    PERFORMANCE,
    
    /**
     * 安全性优先策略（选择最安全的算法）
     */
    SECURITY
}