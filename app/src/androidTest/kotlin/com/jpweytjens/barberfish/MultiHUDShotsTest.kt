package com.jpweytjens.barberfish

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.jpweytjens.barberfish.extension.HUDConfig
import com.jpweytjens.barberfish.extension.HUDId
import com.jpweytjens.barberfish.extension.HUDSlotConfig
import com.jpweytjens.barberfish.extension.HUDSlotField
import com.jpweytjens.barberfish.extension.HUDSlots
import com.jpweytjens.barberfish.extension.SparklineConfig
import com.jpweytjens.barberfish.extension.SparklineMode
import com.jpweytjens.barberfish.extension.streamHUDConfig
import com.jpweytjens.barberfish.extension.updateHUDConfig
import com.jpweytjens.barberfish.screens.MainActivity
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Fork-only check of the HUD picker on the real config screen: gives each HUD a different layout,
 * then picks each in turn and captures the HUD section to multi_hud_shots/ in the app's external
 * files dir.
 */
class MultiHUDShotsTest {

    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    private fun slots(columns: Int, vararg fields: HUDSlotField): HUDSlots {
        val cfg = fields.map { HUDSlotConfig(field = it) }
        return HUDSlots(
            columns = columns,
            leftSlot = cfg[0],
            middleSlot = cfg[1],
            rightSlot = cfg[2],
            fourthSlot = cfg.getOrElse(3) { HUDSlotConfig(field = HUDSlotField.Grade) },
        )
    }

    private val layouts =
        mapOf(
            HUDId.ONE to slots(3, HUDSlotField.Speed, HUDSlotField.HR, HUDSlotField.Power),
            HUDId.TWO to
                slots(
                    4,
                    HUDSlotField.Power,
                    HUDSlotField.HR,
                    HUDSlotField.Cadence,
                    HUDSlotField.NP,
                ),
            HUDId.THREE to slots(3, HUDSlotField.Speed, HUDSlotField.Grade, HUDSlotField.Distance),
            HUDId.FOUR to
                slots(
                    4,
                    HUDSlotField.Speed,
                    HUDSlotField.HRZone,
                    HUDSlotField.PowerZone,
                    HUDSlotField.Grade,
                ),
        )

    @Test
    fun eachHUDShowsItsOwnLayout() {
        val context = composeRule.activity
        runBlocking {
            layouts.forEach { (hud, slots) ->
                context.updateHUDConfig(hud) {
                    HUDConfig(slots, SparklineConfig(mode = SparklineMode.OFF))
                }
            }
        }
        // Each HUD reads back its own layout.
        runBlocking {
            layouts.forEach { (hud, slots) ->
                assertEquals(slots, context.streamHUDConfig(hud).first().slots)
            }
        }
        composeRule.waitForIdle()
        // Expand the HUD section (its header title is the only "HUD" text while collapsed).
        composeRule.onAllNodesWithText("HUD")[0].performClick()
        composeRule.waitForIdle()
        for (hud in HUDId.entries) {
            composeRule
                .onNode(hasText(hud.label) and hasAnyAncestor(hasTestTag("bf:hud:picker")))
                .performClick()
            composeRule.waitForIdle()
            Thread.sleep(1500)
            val bitmap =
                composeRule.onNodeWithTag("bf:section:hud").captureToImage().asAndroidBitmap()
            write("hud_${hud.ordinal + 1}", bitmap)
        }
    }

    private fun write(name: String, bitmap: Bitmap) {
        val dir = File(composeRule.activity.getExternalFilesDir(null), "multi_hud_shots")
        dir.mkdirs()
        FileOutputStream(File(dir, "$name.png")).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
