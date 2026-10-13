package com.android.string.plugin.report

import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue

/** Visits only enqueue events; the build-service owner writes the report after instrumentation. */
class StringBlurReport(
    private val variantName: String,
    private val mode: String,
    private val bytesMode: String
) {
    private val startTime = System.currentTimeMillis()
    private val events = ConcurrentLinkedQueue<Event>()

    fun scanned(className: String) {
        events.add(Event(Kind.SCANNED, "SCAN class=$className"))
    }

    fun skipped(className: String, reason: String) {
        events.add(Event(Kind.SKIPPED, "SKIP class=$className reason=$reason"))
    }

    fun encrypted(className: String?, methodName: String?, value: String?, mode: String, bytesMode: String) {
        val length = value?.length ?: 0
        events.add(Event(
            Kind.ENCRYPTED,
            "ENCRYPT class=${className.orEmpty()} method=${methodName.orEmpty()} mode=$mode bytesMode=$bytesMode length=$length value=${sanitize(value)}",
            mode,
            length
        ))
    }

    fun ignored(className: String?, methodName: String?, value: Any?, reason: String) {
        events.add(Event(
            Kind.IGNORED,
            "IGNORE class=${className.orEmpty()} method=${methodName.orEmpty()} reason=$reason value=${sanitize(value)}"
        ))
    }

    /** Called once after all visits have completed. */
    fun writeTo(file: File) {
        val stats = Stats()
        val duration = System.currentTimeMillis() - startTime
        file.parentFile?.mkdirs()
        file.bufferedWriter().use { writer ->
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())
            writer.write(
                "StringBlur Performance Report\n" +
                "Generated: $timestamp\n" +
                "Variant: $variantName\n" +
                "Config: $mode ($bytesMode mode)\n" +
                "========================================\n\n" +
                "Events:\n"
            )
            while (true) {
                val event = events.poll() ?: break
                writer.write(event.line + "\n")
                when (event.kind) {
                    Kind.SCANNED -> stats.classesScanned++
                    Kind.SKIPPED -> stats.classesSkipped++
                    Kind.IGNORED -> stats.stringsIgnored++
                    Kind.ENCRYPTED -> {
                        stats.stringsEncrypted++
                        stats.algorithmUsage.merge(event.mode, 1, Int::plus)
                        val category = when {
                            event.length == 0 -> "empty"
                            event.length <= 8 -> "1-8"
                            event.length <= 20 -> "9-20"
                            event.length <= 50 -> "21-50"
                            event.length <= 100 -> "51-100"
                            event.length <= 200 -> "101-200"
                            else -> "200+"
                        }
                        stats.lengthDistribution.merge(category, 1, Int::plus)
                    }
                }
            }
            writer.write(generateSummary(stats, duration))
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

    private fun generateSummary(stats: Stats, duration: Long): String = buildString {
        appendLine()
        appendLine("========================================")
        appendLine("PERFORMANCE SUMMARY")
        appendLine("========================================")

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

    private enum class Kind { SCANNED, SKIPPED, ENCRYPTED, IGNORED }

    private data class Event(
        val kind: Kind,
        val line: String,
        val mode: String = "",
        val length: Int = 0
    )

    private data class Stats(
        var classesScanned: Int = 0,
        var classesSkipped: Int = 0,
        var stringsEncrypted: Int = 0,
        var stringsIgnored: Int = 0,
        val algorithmUsage: MutableMap<String, Int> = mutableMapOf(),
        val lengthDistribution: MutableMap<String, Int> = mutableMapOf()
    )
}
