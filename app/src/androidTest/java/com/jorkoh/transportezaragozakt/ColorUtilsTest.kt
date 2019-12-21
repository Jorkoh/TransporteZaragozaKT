package com.jorkoh.transportezaragozakt

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.jorkoh.transportezaragozakt.destinations.utils.toColorFromHex
import com.jorkoh.transportezaragozakt.destinations.utils.toHexFromColor
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ColorUtilsTest{

    @Test
    fun colorUtils_transparentToHex(){
        assertThat(Color.TRANSPARENT.toHexFromColor(), `is`(""))
    }

    @Test
    fun colorUtils_colorsToHex(){
        assertThat(Color.BLACK.toHexFromColor(), `is`("#000000"))
        assertThat(Color.RED.toHexFromColor(), `is`("#FF0000"))
        assertThat(Color.GREEN.toHexFromColor(), `is`("#00FF00"))
        assertThat(Color.BLUE.toHexFromColor(), `is`("#0000FF"))
        assertThat(Color.WHITE.toHexFromColor(), `is`("#FFFFFF"))
    }

    @Test
    fun colorUtils_transparentFromHex(){
        assertThat("".toColorFromHex(), `is`(Color.TRANSPARENT))
    }

    @Test
    fun colorUtils_colorsFromHex(){
        assertThat("#000000".toColorFromHex(), `is`(Color.BLACK))
        assertThat("#FF0000".toColorFromHex(), `is`(Color.RED))
        assertThat("#00FF00".toColorFromHex(), `is`(Color.GREEN))
        assertThat("#0000FF".toColorFromHex(), `is`(Color.BLUE))
        assertThat("#FFFFFF".toColorFromHex(), `is`(Color.WHITE))
    }
}