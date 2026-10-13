package com.android.string.plugin.report

import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import com.android.string.plugin.trasform.StringBlurClassVisitor
import com.android.string.plugin.util.KeyDerivation
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.nio.file.Files
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class StringBlurReportTest {
    @Test
    fun visitsOnlyCollectEventsAndPreserveTransformedBytecode() {
        val directory = Files.createTempDirectory("stringblur-report").toFile()
        try {
            val report = StringBlurReport("debug", "XOR,FAST_ROT", "RANDOM")
            val input = ClassWriter(0).apply {
                visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "test/Sample", null, "java/lang/Object", null)
                visitMethod(Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC, "sample", "()V", null, null).apply {
                    visitCode()
                    listOf("", "x", "hello\nworld\\!").forEach {
                        visitLdcInsn(it)
                        visitInsn(Opcodes.POP)
                    }
                    visitInsn(Opcodes.RETURN)
                    visitMaxs(1, 0)
                    visitEnd()
                }
                visitEnd()
            }.toByteArray()

            fun transform(collector: StringBlurReport?): ByteArray {
                val output = ClassWriter(0)
                ClassReader(input).accept(StringBlurClassVisitor(
                    output, "test/StringBlur", "decrypt", KeyDerivation.master("test-key"),
                    BytesMode.RANDOM, listOf(Mode.XOR, Mode.FAST_ROT), collector, 3
                ), 0)
                return output.toByteArray()
            }

            assertArrayEquals(transform(null), transform(report))
            assertTrue("Visits must not create report files", directory.listFiles()!!.isEmpty())

            val file = File(directory, "reports/debug.txt")
            report.writeTo(file)
            val text = file.readText()
            assertTrue(text.contains("Strings Encrypted: 1"))
            assertTrue(text.contains("Strings Ignored: 2"))
            assertTrue(text.contains("reason=emptyString"))
            assertTrue(text.contains("reason=tooShort"))
            assertTrue(text.contains("value=hello\\nworld\\\\!"))
            assertTrue(text.indexOf("ENCRYPT ") < text.indexOf("PERFORMANCE SUMMARY"))
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun concurrentEventsAreCountedExactlyOnce() {
        val directory = Files.createTempDirectory("stringblur-parallel-report").toFile()
        val executor = Executors.newFixedThreadPool(8)
        try {
            val report = StringBlurReport("release", "XOR", "STRING")
            val jobs = (0 until 8).map { worker -> Callable {
                report.scanned("test/Class$worker")
                repeat(100) { index ->
                    report.encrypted("test/Class$worker", "sample", "value-$index", "XOR", "STRING")
                    report.ignored("test/Class$worker", "sample", "x", "tooShort")
                }
            } }
            executor.invokeAll(jobs).forEach { it.get() }
            val file = File(directory, "release.txt")
            assertFalse(file.exists())
            report.writeTo(file)
            val text = file.readText()
            assertEquals(800, text.lineSequence().count { it.startsWith("ENCRYPT ") })
            assertEquals(800, text.lineSequence().count { it.startsWith("IGNORE ") })
            assertTrue(text.contains("Classes Scanned: 8"))
            assertTrue(text.contains("Strings Encrypted: 800"))
            assertTrue(text.contains("Strings Ignored: 800"))
            assertTrue(text.contains("XOR: 800 strings (100%)"))
        } finally {
            executor.shutdownNow()
            directory.deleteRecursively()
        }
    }

    @Test
    fun variantsHaveIndependentEventsAndStatistics() {
        val directory = Files.createTempDirectory("stringblur-variants-report").toFile()
        try {
            val debug = StringBlurReport("debug", "XOR", "STRING")
            val release = StringBlurReport("release", "FAST_ROT", "BYTES")
            debug.encrypted("test/Debug", "sample", "debug-only", "XOR", "STRING")
            release.skipped("test/Release", "whiteList")
            val debugFile = File(directory, "debug.txt")
            val releaseFile = File(directory, "release.txt")
            debug.writeTo(debugFile)
            release.writeTo(releaseFile)
            assertTrue(debugFile.readText().contains("Strings Encrypted: 1"))
            assertTrue(releaseFile.readText().contains("Classes Skipped: 1"))
            assertTrue(releaseFile.readText().contains("Strings Encrypted: 0"))
            assertFalse(releaseFile.readText().contains("debug-only"))
        } finally {
            directory.deleteRecursively()
        }
    }
}
