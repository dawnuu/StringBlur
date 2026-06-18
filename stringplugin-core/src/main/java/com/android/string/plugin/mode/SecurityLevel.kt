package com.android.string.plugin.mode

/**
 * 字符串安全级别
 *
 * @author chancey
 * @date 2026/6/19
 **/
enum class SecurityLevel {
    /**
     * 低安全要求 - 开发调试相关
     */
    LOW,
    
    /**
     * 中等安全要求 - 普通业务数据
     */
    MEDIUM,
    
    /**
     * 高安全要求 - 敏感信息
     */
    HIGH;
    
    companion object {
        /**
         * 分析字符串的安全级别
         */
        fun analyze(content: String): SecurityLevel {
            val lower = content.lowercase()
            
            // 高安全风险关键词
            val highRiskKeywords = listOf(
                "password", "passwd", "pwd", "secret",
                "token", "api_key", "apikey", "access_key",
                "private_key", "privatekey", "credential",
                "auth", "authorization", "signature"
            )
            
            // 低安全风险关键词
            val lowRiskKeywords = listOf(
                "debug", "test", "mock", "example",
                "sample", "placeholder", "dummy"
            )
            
            return when {
                highRiskKeywords.any { lower.contains(it) } -> HIGH
                lowRiskKeywords.any { lower.contains(it) } -> LOW
                else -> MEDIUM
            }
        }
    }
}