package com.android.string.plugin.util

import com.android.string.plugin.mode.BytesMode
import com.android.string.plugin.mode.Mode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SeedBuilderTest {

    private fun seed(userSeed: String, variant: String = "release") =
        SeedBuilder.build("1.0.3", variant, "com.example", listOf(Mode.XOR, Mode.DEFAULT), BytesMode.STRING, userSeed)

    @Test
    fun sameConfigGivesSameOutputs() {
        val a = seed("s1")
        val b = seed("s1")
        assertEquals(a, b)
        assertEquals(EntryNames.derive(a), EntryNames.derive(b))
        assertEquals(KeyDerivation.master(a).toList(), KeyDerivation.master(b).toList())
    }

    @Test
    fun differentUserSeedChangesEverything() {
        val a = seed("s1")
        val b = seed("s2")
        val empty = seed("")
        assertNotEquals(a, b)
        assertNotEquals(EntryNames.derive(a), EntryNames.derive(b))
        assertNotEquals(KeyDerivation.master(a).toList(), KeyDerivation.master(b).toList())
        assertNotEquals(KeyDerivation.derive(KeyDerivation.master(a), 7), KeyDerivation.derive(KeyDerivation.master(b), 7))
        assertNotEquals(a, empty)
    }

    @Test
    fun variantStillAffectsSeed() {
        assertNotEquals(seed("s1", "debug"), seed("s1", "release"))
    }

    @Test
    fun entryNameDoesNotLeakMasterMaterial() {
        val s = seed("s1")
        val (cls, _) = EntryNames.derive(s)
        val masterHex = KeyDerivation.master(s).joinToString("") { "%08x".format(it) }
        assertFalse(masterHex.contains(cls.removePrefix("Sb")))
    }
}
