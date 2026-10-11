package com.jpweytjens.barberfish.extension

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * One of the four HUD fields. Each has its own Karoo type ID and its own saved layout; ONE keeps
 * the IDs and keys the single HUD always used, so existing pages and settings carry over.
 */
enum class HUDId(
    val typeId: String,
    val label: String,
    slotsKeyName: String,
    stripKeyName: String,
) {
    // "three_column_config" dates from the pre-4-column era; the HUDSlots blob covers both
    // layouts now.
    ONE("three-column", "HUD", "three_column_config", "sparkline_config"),
    TWO("hud-2", "HUD 2", "three_column_config_2", "sparkline_config_2"),
    THREE("hud-3", "HUD 3", "three_column_config_3", "sparkline_config_3"),
    FOUR("hud-4", "HUD 4", "three_column_config_4", "sparkline_config_4");

    internal val slotsKey: Preferences.Key<String> = stringPreferencesKey(slotsKeyName)
    internal val stripKey: Preferences.Key<String> = stringPreferencesKey(stripKeyName)

    companion object {
        /** The HUD named by [name]; ONE when it is missing or unknown. */
        fun fromName(name: String?): HUDId = entries.firstOrNull { it.name == name } ?: ONE
    }
}
