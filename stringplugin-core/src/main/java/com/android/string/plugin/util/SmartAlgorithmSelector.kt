package com.android.string.plugin.util

import com.android.string.plugin.mode.Mode
import com.android.string.plugin.mode.SecurityLevel
import com.android.string.plugin.mode.SelectionStrategy
import kotlin.math.min

/**
 * 智能算法选择器
 * 基于字符串特征自动选择最佳加密算法
 *
 * @author chancey
 * @date 2026/6/19
 **/
class SmartAlgorithmSelector {
    
    /**
     * 选择最佳加密算法
     */
    fun selectBestAlgorithm(
        content: String,
        modes: List<Mode>,
        strategy: SelectionStrategy = SelectionStrategy.RANDOM,
        performanceWeight: Double = 0.5,
        securityWeight: Double = 0.5
    ): Mode {
        if (modes.size == 1) return modes.first()
        
        return when (strategy) {
            SelectionStrategy.RANDOM -> modes.random() // 保持现有行为：完全随机
            SelectionStrategy.PERFORMANCE -> selectPerformanceMode(modes)
            SelectionStrategy.SECURITY -> selectSecurityMode(modes)
            SelectionStrategy.SMART -> selectSmartMode(content, modes, performanceWeight, securityWeight)
        }
    }
    
    /**
     * 性能优先模式：选择最快的算法
     */
    private fun selectPerformanceMode(modes: List<Mode>): Mode {
        // 按性能排序（最快的在前）
        val performanceOrder = listOf(Mode.REVERSE, Mode.FAST_ROT, Mode.XOR, Mode.XOR_SIMD, Mode.SHIFT, Mode.XOR_SHIFT, Mode.DEFAULT)
        
        for (mode in performanceOrder) {
            if (mode in modes) return mode
        }
        return modes.first()
    }
    
    /**
     * 安全优先模式：选择最安全的算法
     */
    private fun selectSecurityMode(modes: List<Mode>): Mode {
        // 按安全强度排序（最安全的在前）
        val securityOrder = listOf(Mode.XOR_SHIFT, Mode.DEFAULT, Mode.XOR_SIMD, Mode.SHIFT, Mode.XOR, Mode.FAST_ROT, Mode.REVERSE)
        
        for (mode in securityOrder) {
            if (mode in modes) return mode
        }
        return modes.first()
    }
    
    /**
     * 智能模式：综合考虑多个因素
     */
    private fun selectSmartMode(
        content: String,
        modes: List<Mode>,
        performanceWeight: Double,
        securityWeight: Double
    ): Mode {
        val scoreMap = mutableMapOf<Mode, Double>()
        
        // 对每个可用算法打分
        modes.forEach { mode ->
            var score = 0.0
            
            // 1. 基于字符串长度打分
            score += getLengthScore(content.length, mode) * 0.3
            
            // 2. 基于安全级别打分 
            val securityLevel = SecurityLevel.analyze(content)
            score += getSecurityScore(securityLevel, mode) * securityWeight
            
            // 3. 基于内容特征打分
            score += getContentScore(content, mode) * 0.2
            
            // 4. 性能权重
            score += getPerformanceScore(mode) * performanceWeight
            
            scoreMap[mode] = score
        }
        
        // 返回得分最高的算法
        return scoreMap.maxByOrNull { it.value }?.key ?: modes.first()
    }
    
    /**
     * 基于字符串长度的评分
     */
    private fun getLengthScore(length: Int, mode: Mode): Double {
        return when (length) {
            in 0..8 -> when (mode) {
                Mode.FAST_ROT, Mode.REVERSE -> 1.0
                Mode.XOR -> 0.8
                else -> 0.6
            }
            in 9..50 -> when (mode) {
                Mode.XOR_SIMD -> 1.0
                Mode.XOR, Mode.SHIFT -> 0.9
                else -> 0.7
            }
            in 51..200 -> when (mode) {
                Mode.XOR_SHIFT -> 1.0
                Mode.XOR_SIMD -> 0.9
                else -> 0.8
            }
            else -> when (mode) { // > 200
                Mode.REVERSE -> 1.0
                Mode.XOR_SIMD -> 0.9
                else -> 0.7
            }
        }
    }
    
    /**
     * 基于安全级别的评分
     */
    private fun getSecurityScore(securityLevel: SecurityLevel, mode: Mode): Double {
        val baseSecurity = when (mode) {
            Mode.XOR_SHIFT, Mode.DEFAULT -> 1.0
            Mode.XOR_SIMD, Mode.SHIFT -> 0.8
            Mode.XOR -> 0.6
            Mode.FAST_ROT, Mode.REVERSE -> 0.4
        }
        
        return when (securityLevel) {
            SecurityLevel.HIGH -> baseSecurity
            SecurityLevel.MEDIUM -> baseSecurity * 0.8
            SecurityLevel.LOW -> baseSecurity * 0.6
        }
    }
    
    /**
     * 基于内容特征的评分
     */
    private fun getContentScore(content: String, mode: Mode): Double {
        val isBinaryLike = content.any { it.code < 32 || it.code > 126 }
        val isNumeric = content.all { it.isDigit() }
        val isAlphaNum = content.all { it.isLetterOrDigit() }
        
        return when {
            isBinaryLike -> when (mode) {
                Mode.XOR_SIMD -> 1.0
                Mode.XOR -> 0.9
                else -> 0.6
            }
            isNumeric -> when (mode) {
                Mode.FAST_ROT -> 1.0
                Mode.SHIFT -> 0.9
                else -> 0.8
            }
            isAlphaNum -> when (mode) {
                Mode.REVERSE -> 1.0
                Mode.FAST_ROT -> 0.9
                else -> 0.8
            }
            else -> 0.8 // 复杂文本，平衡选择
        }
    }
    
    /**
     * 基于性能的评分
     */
    private fun getPerformanceScore(mode: Mode): Double {
        return when (mode) {
            Mode.REVERSE -> 1.0      // 最快
            Mode.FAST_ROT -> 0.95    // 非常快
            Mode.XOR -> 0.85         // 快
            Mode.XOR_SIMD -> 0.8     // 快（长字符串优秀）
            Mode.SHIFT -> 0.7        // 中等
            Mode.XOR_SHIFT -> 0.6    // 较慢
            Mode.DEFAULT -> 0.5      // 最慢
        }
    }
    
    companion object {
        /**
         * 获取算法性能描述
         */
        fun getModeDescription(mode: Mode): String {
            return when (mode) {
                Mode.REVERSE -> "字节反转 - 超快速，适合所有长度"
                Mode.FAST_ROT -> "位旋转 - 超快速，适合短字符串"
                Mode.XOR -> "基础异或 - 快速通用"
                Mode.XOR_SIMD -> "SIMD异或 - 批量优化，适合中长字符串"
                Mode.SHIFT -> "位移变换 - 中等速度，安全性好"
                Mode.XOR_SHIFT -> "异或位移组合 - 较慢，安全性最高"
                Mode.DEFAULT -> "Base64变种 - 最慢，兼容性最好"
            }
        }
    }
}