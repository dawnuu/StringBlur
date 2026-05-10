package com.android.string.plugin.report

import java.io.File

object StringBlurReport {
    private val lock = Any()
    private val stats = mutableMapOf<String, Stats>()

    fun init(reportPath: String, variantName: String, mode: String, useBytes: Boolean) {
        synchronized(lock) {
            val file = File(reportPath)
            file.parentFile?.mkdirs()
            stats[reportPath] = Stats()
            file.writeText(
                "StringBlur Report\n" +
                        "Variant: $variantName\n" +
                        "Mode: $mode\n" +
                        "UseBytes: $useBytes\n\n" +
                        "Summary:\n" +
                        "Classes scanned: 0\n" +
                        "Classes skipped: 0\n" +
                        "Strings encrypted: 0\n" +
                        "Strings ignored: 0\n\n" +
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

    fun encrypted(reportPath: String, className: String?, methodName: String?, value: String?) {
        update(reportPath) { it.stringsEncrypted++ }
        append(
            reportPath,
            "ENCRYPT class=${className.orEmpty()} method=${methodName.orEmpty()} value=${sanitize(value)}"
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
            writeSummary(file, stats.getOrPut(reportPath) { Stats() })
        }
    }

    private fun update(reportPath: String, block: (Stats) -> Unit) {
        synchronized(lock) {
            block(stats.getOrPut(reportPath) { Stats() })
        }
    }

    private fun writeSummary(file: File, stats: Stats) {
        val text = file.readText()
        val updated = text.replace(
            Regex(
                "Summary:\\n" +
                        "Classes scanned: \\d+\\n" +
                        "Classes skipped: \\d+\\n" +
                        "Strings encrypted: \\d+\\n" +
                        "Strings ignored: \\d+"
            ),
            "Summary:\n" +
                    "Classes scanned: ${stats.classesScanned}\n" +
                    "Classes skipped: ${stats.classesSkipped}\n" +
                    "Strings encrypted: ${stats.stringsEncrypted}\n" +
                    "Strings ignored: ${stats.stringsIgnored}"
        )
        file.writeText(updated)
    }

    private fun sanitize(value: Any?): String {
        return value?.toString()
            ?.replace("\\", "\\\\")
            ?.replace("\n", "\\n")
            ?.replace("\r", "\\r")
            ?.take(200)
            ?: ""
    }

    private data class Stats(
        var classesScanned: Int = 0,
        var classesSkipped: Int = 0,
        var stringsEncrypted: Int = 0,
        var stringsIgnored: Int = 0
    )
}