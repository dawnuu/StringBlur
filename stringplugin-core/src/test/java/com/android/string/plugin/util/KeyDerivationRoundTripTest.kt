package com.android.string.plugin.util

import com.android.string.plugin.mode.Mode
import com.android.string.plugin.task.build.StringBlurFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import javax.tools.ToolProvider

class KeyDerivationRoundTripTest {

    private val master = KeyDerivation.master("test-seed|debug|com.example|XOR")
    private val samples = listOf("a", "https://example.com/api", "中文字符串 with symbols !@#%", "x".repeat(300))
    private val tags = listOf(0, 1, -1, 42, 12345, Int.MAX_VALUE, Int.MIN_VALUE, 0x7fff0001, -0x1234567)

    @Test
    fun derivedKeysAreAsciiAndDifferPerTag() {
        val keys = tags.map { KeyDerivation.derive(master, it) }
        keys.forEach {
            assertEquals(KeyDerivation.KEY_LENGTH, it.length)
            assertTrue(it.all { c -> c in KeyDerivation.ALPHABET })
        }
        assertEquals(keys.size, keys.toSet().size)
        assertNotEquals(KeyDerivation.derive(master, 1), KeyDerivation.derive(KeyDerivation.master("other"), 1))
    }

    @Test
    fun generatedRuntimeClassMatchesBuildTimeEncryption() {
        val modes = listOf(Mode.DEFAULT, Mode.XOR, Mode.XOR_SHIFT, Mode.XOR_SIMD, Mode.FAST_ROT)
        val appId = "test.app"
        val dir = Files.createTempDirectory("stringblur-roundtrip").toFile()
        try {
            val srcDir = File(dir, "src/test/app/stringblur").apply { mkdirs() }
            modes.forEach { ModeUtils.getEncodeImplFile(it).create(srcDir, appId) }
            StringBlurFile(master).create(srcDir, appId, modes, "SbTest", "dTest")

            val outDir = File(dir, "out").apply { mkdirs() }
            val compiler = ToolProvider.getSystemJavaCompiler()
            val sources = srcDir.listFiles { f -> f.name.endsWith(".java") }!!.map { it.absolutePath }
            val args = listOf("-d", outDir.absolutePath, "-cp", System.getProperty("java.class.path")) + sources
            val errors = java.io.ByteArrayOutputStream()
            val status = compiler.run(null, null, errors, *args.toTypedArray())
            assertEquals("generated sources failed to compile:\n$errors", 0, status)

            val loader = URLClassLoader(arrayOf(outDir.toURI().toURL()), javaClass.classLoader)
            val wrapper = loader.loadClass("test.app.stringblur.SbTest")
            val decryptString = wrapper.getMethod("dTest", String::class.java, Int::class.java, Int::class.java)
            val decryptBytes = wrapper.getMethod("dTest", ByteArray::class.java, Int::class.java, Int::class.java)

            modes.forEachIndexed { modeIndex, mode ->
                val impl = ModeUtils.getEncodeImpl(mode)
                for (plain in samples) {
                    for (tag in tags) {
                        val key = KeyDerivation.derive(master, tag)
                        assertEquals(plain, decryptString.invoke(null, impl.encryptString(plain, key), tag, modeIndex))
                        assertEquals(plain, decryptBytes.invoke(null, impl.encryptBytes(plain, key), tag, modeIndex))
                    }
                }
            }
        } finally {
            dir.deleteRecursively()
        }
    }
}
