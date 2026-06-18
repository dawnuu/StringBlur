package com.android.string.plugin.report

import java.io.File

object StringBlurReport {
    private val lock = Any()
    private val stats = mutableMapOf<String, Stats>()

    fun init(reportPath: String, variantName: String, mode: String, bytesMode: String) {
        synchronized(lock) {
            val file = File(reportPath)
            file.parentFile?.mkdirs()
            stats[reportPath] = Stats().apply {
                startTime = System.currentTimeMillis()
            }
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())
            file.writeText(
                "StringBlur Performance Report\n" +
                "Generated: $timestamp\n" +
                "Variant: $variantName\n" +
                "Config: $mode ($bytesMode mode)\n" +
                "========================================\n\n" +
                "Events:\n"
            )
        }
    }

    fun scanned(reportPath: String, className: String) {
        update(reportPath) { it.classesScanned++ }
        append(reportPath, "SCAN class=$className")
    }

    fun skipped(reportPath: String, className: String, reason: String) {
        update(reportPath) { it.classesSkipped++ }
        append(reportPath, "SKIP class=$className reason=$reason")
    }

    fun encrypted(reportPath: String, className: String?, methodName: String?, value: String?, mode: String, bytesMode: String) {
        update(reportPath) { stats ->
            stats.stringsEncrypted++
            
            // 统计算法使用
            stats.algorithmUsage[mode] = stats.algorithmUsage.getOrDefault(mode, 0) + 1
            
            // 统计字节模式使用
            stats.bytesModeUsage[bytesMode] = stats.bytesModeUsage.getOrDefault(bytesMode, 0) + 1
            
            // 统计字符串长度分布
            val length = value?.length ?: 0
            val lengthCategory = when {
                length == 0 -> "empty"
                length <= 8 -> "1-8"
                length <= 20 -> "9-20" 
                length <= 50 -> "21-50"
                length <= 100 -> "51-100"
                length <= 200 -> "101-200"
                else -> "200+"
            }
            stats.lengthDistribution[lengthCategory] = stats.lengthDistribution.getOrDefault(lengthCategory, 0) + 1
        }
        
        append(
            reportPath,
            "ENCRYPT class=${className.orEmpty()} method=${methodName.orEmpty()} mode=$mode bytesMode=$bytesMode length=${value?.length ?: 0} value=${sanitize(value)}"
        )
    }

    fun ignored(reportPath: String, className: String?, methodName: String?, value: Any?, reason: String) {
        update(reportPath) { it.stringsIgnored++ }
        append(
            reportPath,
            "IGNORE class=${className.orEmpty()} method=${methodName.orEmpty()} reason=$reason value=${sanitize(value)}"
        )
    }

    private fun append(reportPath: String, line: String) {
        synchronized(lock) {
            val file = File(reportPath)
            file.parentFile?.mkdirs()
            file.appendText(line + "\n")
        }
    }

    private fun update(reportPath: String, block: (Stats) -> Unit) {
        synchronized(lock) {
            block(stats.getOrPut(reportPath) { Stats() })
        }
    }

    private fun sanitize(value: Any?): String {
        return value?.toString()
            ?.replace("\\", "\\\\")
            ?.replace("\n", "\\n")
            ?.replace("\r", "\\r")
            ?.take(200)
            ?: ""
    }

    /**
     * 生成总结报告
     */
    fun generateSummary(reportPath: String) {
        synchronized(lock) {
            val stats = stats[reportPath] ?: return
            stats.endTime = System.currentTimeMillis()
            
            val summary = buildString {
                appendLine()
                appendLine("========================================")
                appendLine("PERFORMANCE SUMMARY")
                appendLine("========================================")
                
                val duration = stats.endTime - stats.startTime
                appendLine("Execution Time: ${duration}ms")
                appendLine()
                
                // 总体统计
                appendLine("Overall Statistics:")
                appendLine("  Classes Scanned: ${stats.classesScanned}")
                appendLine("  Classes Skipped: ${stats.classesSkipped}")
                appendLine("  Strings Encrypted: ${stats.stringsEncrypted}")
                appendLine("  Strings Ignored: ${stats.stringsIgnored}")
                appendLine()
                
                // 算法使用分布
                if (stats.algorithmUsage.isNotEmpty()) {
                    appendLine("Algorithm Distribution:")
                    val sortedAlgorithms = stats.algorithmUsage.toList().sortedByDescending { it.second }
                    sortedAlgorithms.forEach { (mode, count) ->
                        val percentage = (count * 100.0 / stats.stringsEncrypted).toInt()
                        appendLine("  $mode: $count strings ($percentage%)")
                    }
                    appendLine()
                }
                
                // 字符串长度分布
                if (stats.lengthDistribution.isNotEmpty()) {
                    appendLine("String Length Distribution:")
                    val sortedLengths = stats.lengthDistribution.toList().sortedBy {
                        when (it.first) {
                            "empty" -> 0
                            "1-8" -> 1
                            "9-20" -> 2
                            "21-50" -> 3
                            "51-100" -> 4
                            "101-200" -> 5
                            "200+" -> 6
                            else -> 999
                        }
                    }
                    sortedLengths.forEach { (range, count) ->
                        appendLine("  $range chars: $count strings")
                    }
                    appendLine()
                }
                
                // 性能评估
                appendLine("Performance Assessment:")
                when {
                    duration < 1000 -> appendLine("  ⭐⭐⭐⭐⭐ Very Fast (<1s)")
                    duration < 3000 -> appendLine("  ⭐⭐⭐⭐ Fast (1-3s)")
                    duration < 10000 -> appendLine("  ⭐⭐⭐ Moderate (3-10s)")
                    else -> appendLine("  ⭐⭐ Slow (>10s)")
                }
                
                val encryptRate = if (duration > 0) (stats.stringsEncrypted * 1000.0 / duration) else 0.0
                appendLine("  Encryption Rate: ${encryptRate.toInt()} strings/sec")
            }
            
            append(reportPath, summary)
        }
    }

    private data class Stats(
        var classesScanned: Int = 0,
        var classesSkipped: Int = 0,
        var stringsEncrypted: Int = 0,
        var stringsIgnored: Int = 0,
        
        // 性能统计
        var startTime: Long = System.currentTimeMillis(),
        var endTime: Long = 0,
        
        // 算法使用统计
        val algorithmUsage: MutableMap<String, Int> = mutableMapOf(),
        val bytesModeUsage: MutableMap<String, Int> = mutableMapOf(),
        
        // 字符串长度分布
        val lengthDistribution: MutableMap<String, Int> = mutableMapOf(),
        
        // 加密耗时统计（简化版）
        var totalEncryptTime: Long = 0
    )
}