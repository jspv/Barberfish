package com.jpweytjens.barberfish

import com.jpweytjens.barberfish.extension.HUDId
import com.jpweytjens.barberfish.extension.HUDTap
import com.jpweytjens.barberfish.extension.lookaheadFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HUDTapTest {

    @Test
    fun `a tap shows its lookahead on the HUD that was tapped`() =
        assertEquals(20, HUDTap(1L, HUDId.TWO, 20).lookaheadFor(HUDId.TWO))

    @Test
    fun `a tap is ignored by the other HUDs`() =
        assertNull(HUDTap(1L, HUDId.TWO, 20).lookaheadFor(HUDId.ONE))

    @Test fun `no tap shows nothing`() = assertNull(HUDTap.NONE.lookaheadFor(HUDId.ONE))
}
