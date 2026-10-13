package com.android.string.plugin.util

import com.android.string.plugin.mode.Mode
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamEncodeImplTest {
    private val modes = listOf(Mode.XOR_CHAIN, Mode.LCG_STREAM)

    @Test
    fun xorChainMatchesKnownCiphertextAndDecryptsInPlace() {
        val impl = ModeUtils.getEncodeImpl(Mode.XOR_CHAIN)
        val plain = byteArrayOf(0, 1, 2, 3, 0x80.toByte(), 0xff.toByte())
        val cipher = byteArrayOf(107, 15, 116, 28, 249.toByte(), 127)
        assertArrayEquals(cipher, impl.encrypt(plain.copyOf(), "key"))
        val input = cipher.copyOf()
        assertSame(input, impl.decrypt(input, "key".toByteArray()))
        assertArrayEquals(plain, input)
    }

    @Test
    fun xorChainPropagatesPlaintextChangeToCiphertextSuffix() {
        val impl = ModeUtils.getEncodeImpl(Mode.XOR_CHAIN)
        val plain = ByteArray(64) { it.toByte() }
        val changed = plain.copyOf().apply { this[17] = (this[17].toInt() xor 1).toByte() }
        val first = impl.encrypt(plain, "key")
        val second = impl.encrypt(changed, "key")
        assertArrayEquals(first.copyOfRange(0, 17), second.copyOfRange(0, 17))
        for (i in 17 until first.size) {
            assertTrue("ciphertext byte $i must change", first[i] != second[i])
        }
    }

    @Test
    fun lcgStreamMatchesKnownCiphertext() {
        val impl = ModeUtils.getEncodeImpl(Mode.LCG_STREAM)
        val plain = ByteArray(16) { it.toByte() }
        // Independently computed using unsigned 32-bit FNV-1a and LCG arithmetic.
        val cipher = intArrayOf(197, 92, 57, 205, 98, 225, 85, 15, 59, 170, 176, 211, 88, 144, 242, 160)
            .map { it.toByte() }.toByteArray()
        assertArrayEquals(cipher, impl.encrypt(plain.copyOf(), "key"))
        assertArrayEquals(plain, impl.decrypt(cipher.copyOf(), "key".toByteArray()))
    }

    @Test
    fun lcgStreamUsesKeySuffixEvenForOneBytePayload() {
        val impl = ModeUtils.getEncodeImpl(Mode.LCG_STREAM)
        val first = impl.encrypt(byteArrayOf(0), "same-prefix-A")
        val second = impl.encrypt(byteArrayOf(0), "same-prefix-B")
        assertFalse(first.contentEquals(second))
    }

    @Test
    fun roundTripsAllByteValuesWithUtf8KeysAndResetsStatePerCall() {
        val plain = ByteArray(8192) { it.toByte() }
        for (mode in modes) {
            val impl = ModeUtils.getEncodeImpl(mode)
            for (key in listOf("k", "key", "中文密钥🔑", "z".repeat(300))) {
                val input = plain.copyOf()
                val cipher = impl.encrypt(input, key)
                assertSame(input, cipher)
                assertFalse("$mode must transform the payload", plain.contentEquals(cipher))
                impl.encrypt(byteArrayOf(1, 2, 3), "other-key")
                assertArrayEquals(cipher, impl.encrypt(plain.copyOf(), key))
                assertArrayEquals(plain, impl.decrypt(cipher, key.toByteArray(Charsets.UTF_8)))
            }
        }
    }

    @Test
    fun emptyOrNullDataAndKeysAreNoOps() {
        for (mode in modes) {
            val impl = ModeUtils.getEncodeImpl(mode)
            assertNull(impl.encrypt(null, "key"))
            assertNull(impl.decrypt(null, "key".toByteArray()))
            val empty = byteArrayOf()
            assertSame(empty, impl.encrypt(empty, "key"))
            assertSame(empty, impl.decrypt(empty, "key".toByteArray()))
            val plain = byteArrayOf(0, 1, -1)
            assertSame(plain, impl.encrypt(plain, ""))
            assertSame(plain, impl.encrypt(plain, null))
            assertSame(plain, impl.decrypt(plain, byteArrayOf()))
            assertSame(plain, impl.decrypt(plain, null))
            assertArrayEquals(byteArrayOf(0, 1, -1), plain)
        }
    }
}
