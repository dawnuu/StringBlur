package com.android.string.plugin.report

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.LongAdder

/**
 * Visits only update counters, so memory stays constant regardless of project size.
 * Only a bounded sample of skip/ignore diagnostics is kept, and string values are never recorded.
 */
class StringBlurReport(
    private val variantName: String,
    private val mode: String,
    private val bytesMode: String
) {
    private val startTime = System.currentTimeMillis()
    private val classesScanned = LongAdder()
    private val classesSkipped = LongAdder()
    private val stringsEncrypted = LongAdder()
    private val stringsIgnored = LongAdder()
    private val algorithmUsage = ConcurrentHashMap<String, LongAdder>()
    private val lengthDistribution = ConcurrentHashMap<String, LongAdder>()
    private val ignoredReasons = ConcurrentHashMap<String, LongAdder>()
    private val samples = ConcurrentLinkedQueue<String>()
    private val sampleCount = AtomicInteger()

    fun scanned(className: String) {
        classesScanned.increment()
    }

    fun skipped(className: String, reason: String) {
        classesSkipped.increment()
        sample("SKIP class=$className reason=$reason")
    }

    fun encrypted(className: String?, methodName: String?, value: String?, mode: String, bytesMode: String) {
        stringsEncrypted.increment()
        algorithmUsage.computeIfAbsent(mode) { LongAdder() }.increment()
        lengthDistribution.computeIfAbsent(lengthCategory(value?.length ?: 0)) { LongAdder() }.increment()
    }

    fun ignored(className: String?, methodName: String?, value: Any?, reason: String) {
        stringsIgnored.increment()
        ignoredReasons.computeIfAbsent(reason) { LongAdder() }.increment()
        sample("IGNORE class=${className.orEmpty()} method=${methodName.orEmpty()} reason=$reason")
    }

    private fun sample(line: String) {
        if (sampleCount.getAndIncrement() < MAX_SAMPLES) samples.add(line)
    }

    /** Called once after all visits have completed. */
    fun writeTo(file: File) {
        val duration = System.currentTimeMillis() - startTime
        file.parentFile?.mkdirs()
        file.bufferedWriter().use { writer ->
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())
            writer.write(
                "StringBlur Performance Report\n" +
                "Generated: $timestamp\n" +
                "Variant: $variantName\n" +
                "Config: $mode ($bytesMode mode)\n" +
                "========================================\n\n"
            )
            val total = sampleCount.get()
            if (total > 0) {
                writer.write("Skip/Ignore Samples (showing ${minOf(total, MAX_SAMPLES)} of $total):\n")
                samples.forEach { writer.write(it + "\n") }
            }
            writer.write(generateSummary(duration))
        }
    }

    private fun lengthCategory(length: Int) = when {
        length == 0 -> "empty"
        length <= 8 -> "1-8"
        length <= 20 -> "9-20"
        length <= 50 -> "21-50"
        length <= 100 -> "51-100"
        length <= 200 -> "101-200"
        else -> "200+"
    }

    private fun generateSummary(duration: Long): String = buildString {
        val encrypted = stringsEncrypted.sum()
        appendLine()
        appendLine("========================================")
        appendLine("PERFORMANCE SUMMARY")
        appendLine("========================================")

        appendLine("Execution Time: ${duration}ms")
        appendLine()

        // 总体统计
        appendLine("Overall Statistics:")
        appendLine("  Classes Scanned: ${classesScanned.sum()}")
        appendLine("  Classes Skipped: ${classesSkipped.sum()}")
        appendLine("  Strings Encrypted: $encrypted")
        appendLine("  Strings Ignored: ${stringsIgnored.sum()}")
        appendLine()

        // 忽略原因分布
        if (ignoredReasons.isNotEmpty()) {
            appendLine("Ignored Reasons:")
            ignoredReasons.mapValues { it.value.sum() }.toList().sortedByDescending { it.second }
                .forEach { (reason, count) -> appendLine("  $reason: $count") }
            appendLine()
        }

        // 算法使用分布
        if (algorithmUsage.isNotEmpty()) {
            appendLine("Algorithm Distribution:")
            val sortedAlgorithms = algorithmUsage.mapValues { it.value.sum() }.toList().sortedByDescending { it.second }
            sortedAlgorithms.forEach { (mode, count) ->
                val percentage = (count * 100.0 / encrypted).toInt()
                appendLine("  $mode: $count strings ($percentage%)")
            }
            appendLine()
        }

        // 字符串长度分布
        if (lengthDistribution.isNotEmpty()) {
            appendLine("String Length Distribution:")
            val sortedLengths = lengthDistribution.mapValues { it.value.sum() }.toList().sortedBy {
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

        val encryptRate = if (duration > 0) (encrypted * 1000.0 / duration) else 0.0
        appendLine("  Encryption Rate: ${encryptRate.toInt()} strings/sec")
    }

    private companion object {
        const val MAX_SAMPLES = 200
    }
}
