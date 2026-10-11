package com.jpweytjens.barberfish.extension

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class SparklineTapReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION = "com.jpweytjens.barberfish.SPARKLINE_TAP"
        const val EXTRA_SURFACE = "surface"
        const val SURFACE_HUD = "hud"
        const val SURFACE_FIELD = "field"
        // HUDId name of the tapped HUD.
        const val EXTRA_HUD = "hud"
        // The last HUD strip tap; only the HUD strips listen (transition overlay).
        // Timestamp is always unique (ms), preventing MutableStateFlow deduplication.
        val tapSignal = MutableStateFlow(HUDTap.NONE)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isField = intent.getStringExtra(EXTRA_SURFACE) == SURFACE_FIELD
        val result = goAsync()
        val job = Job()
        CoroutineScope(Dispatchers.IO + job).launch {
            try {
                withTimeout(5_000L) {
                    if (isField) {
                        val cfg = context.streamFieldSparklineConfig().first()
                        context.saveFieldSparklineConfig(
                            cfg.copy(lookaheadKm = nextLookahead(cfg.lookaheadKm))
                        )
                    } else {
                        val hud = HUDId.fromName(intent.getStringExtra(EXTRA_HUD))
                        context.updateHUDConfig(hud) { config ->
                            val next = nextLookahead(config.strip.lookaheadKm)
                            tapSignal.value = HUDTap(System.currentTimeMillis(), hud, next)
                            config.copy(strip = config.strip.copy(lookaheadKm = next))
                        }
                    }
                }
            } finally {
                result.finish()
                job.cancel()
            }
        }
    }
}

/** The last strip tap: which HUD, and the lookahead it moved to. [NONE] before any tap. */
data class HUDTap(val timestampMs: Long, val hud: HUDId, val lookaheadKm: Int) {
    companion object {
        val NONE = HUDTap(0L, HUDId.ONE, 10)
    }
}

/** The lookahead to flash on [hud], or null when this tap belongs to another HUD or none. */
fun HUDTap.lookaheadFor(hud: HUDId): Int? = lookaheadKm.takeIf {
    timestampMs != 0L && this.hud == hud
}

private fun nextLookahead(current: Int): Int =
    when (current) {
        5 -> 10
        10 -> 20
        else -> 5
    }
