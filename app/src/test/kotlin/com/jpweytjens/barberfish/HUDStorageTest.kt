package com.jpweytjens.barberfish

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.jpweytjens.barberfish.extension.HUDConfig
import com.jpweytjens.barberfish.extension.HUDId
import com.jpweytjens.barberfish.extension.HUDSlots
import com.jpweytjens.barberfish.extension.SparklineConfig
import com.jpweytjens.barberfish.extension.allHUDConfigs
import com.jpweytjens.barberfish.extension.hudConfig
import com.jpweytjens.barberfish.extension.writeHUDConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HUDStorageTest {

    private val fourColumns = HUDConfig(slots = HUDSlots(columns = 4))

    @Test
    fun `HUD one keeps the original keys and type id`() {
        assertEquals("three-column", HUDId.ONE.typeId)
        val prefs = mutablePreferencesOf()
        prefs.writeHUDConfig(HUDId.ONE, fourColumns)
        assertTrue(prefs.contains(stringPreferencesKey("three_column_config")))
        assertTrue(prefs.contains(stringPreferencesKey("sparkline_config")))
    }

    @Test
    fun `other HUDs use numbered keys and type ids`() {
        assertEquals(listOf("hud-2", "hud-3", "hud-4"), HUDId.entries.drop(1).map { it.typeId })
        val prefs = mutablePreferencesOf()
        prefs.writeHUDConfig(HUDId.THREE, fourColumns)
        assertTrue(prefs.contains(stringPreferencesKey("three_column_config_3")))
        assertTrue(prefs.contains(stringPreferencesKey("sparkline_config_3")))
        assertNull(prefs[stringPreferencesKey("three_column_config")])
    }

    @Test
    fun `a HUD reads back what was written`() {
        val prefs = mutablePreferencesOf()
        prefs.writeHUDConfig(HUDId.TWO, fourColumns)
        assertEquals(fourColumns, prefs.hudConfig(HUDId.TWO))
    }

    @Test
    fun `writing one HUD leaves the others on defaults`() {
        val prefs = mutablePreferencesOf()
        prefs.writeHUDConfig(HUDId.TWO, fourColumns)
        HUDId.entries
            .filter { it != HUDId.TWO }
            .forEach { assertEquals(HUDConfig(), prefs.hudConfig(it)) }
    }

    @Test
    fun `absent keys give the default HUD`() =
        assertEquals(HUDConfig(), emptyPreferences().hudConfig(HUDId.FOUR))

    @Test
    fun `HUD one's strip reads back from its original key`() {
        val strip = SparklineConfig(lookaheadKm = 20)
        val prefs = mutablePreferencesOf()
        prefs.writeHUDConfig(HUDId.ONE, HUDConfig(HUDSlots(columns = 4), strip))
        assertEquals(strip, prefs.hudConfig(HUDId.ONE).strip)
    }

    @Test
    fun `fromName maps names and falls back to HUD one`() {
        assertEquals(HUDId.THREE, HUDId.fromName("THREE"))
        assertEquals(HUDId.ONE, HUDId.fromName(null))
        assertEquals(HUDId.ONE, HUDId.fromName("nope"))
    }

    @Test
    fun `the snapshot holds every HUD`() {
        val prefs = mutablePreferencesOf()
        prefs.writeHUDConfig(HUDId.FOUR, fourColumns)
        val all = prefs.allHUDConfigs()
        assertEquals(HUDId.entries.toSet(), all.keys)
        assertEquals(fourColumns, all.getValue(HUDId.FOUR))
    }
}
