package com.herannet.arabictvkeyboard

import org.junit.Assert.*
import org.junit.Test

class RemoteLayerTest {
    private fun resolve(letter: Char?, char: Char?, armed: Boolean = false, alt: Boolean = false,
                        shift: Boolean = false, compatibility: Boolean = true, legacy: Boolean = false,
                        arabic: Boolean = true, numbers: Boolean = false, custom: String? = null) =
        RemoteLayer.resolve(letter, char, armed, alt, shift, compatibility, legacy, arabic, numbers, custom)

    @Test fun shiftReportedByUsbRemoteActivatesBothLayerAndOutput() {
        assertTrue(RemoteLayer.active(false, true, true))
        assertEquals("أ", resolve('A', 'A', shift = true))
        assertEquals("ط", resolve('T', 'T', shift = true))
        assertEquals("ظ", resolve('Z', 'Z', shift = true))
    }
    @Test fun shiftedSymbolsDecodeTheOriginalBlueLegendInsteadOfLeakingSymbols() {
        // Firmware synthesizes US Shift+3 for the blue # printed on the D key.
        assertEquals("ُ", resolve(null, '#', shift = true))
        assertEquals("أ", resolve(null, '?', shift = true))
        assertEquals("ة", resolve(null, '_', shift = true))
        assertEquals("ظ", resolve(null, '+', shift = true))
    }
    @Test fun knownAndroidLetterWithBlueUnicodeAlsoDecodesCorrectly() {
        assertEquals("أ", resolve('A', '?', shift = true))
        assertEquals("ُ", resolve('D', '#', shift = true))
        assertEquals("ة", resolve('B', '_', shift = true))
    }
    @Test fun existingUserCalibrationsWinAndTheirRawIdentityDoesNotChange() {
        val identity = UsbScanCodes.identity("usb", 0, 4, 0, true, false)
        val saved = mapOf(identity to "ئ")
        assertEquals("ئ", resolve(null, '#', shift = true, custom = saved[identity]))
        assertEquals(identity, UsbScanCodes.identity("usb", 0, 4, 0, true, false))
        assertNotEquals(identity, UsbScanCodes.identity("usb", 0, 4, 0, false, true))
    }
    @Test fun releasedShiftDoesNotLeaveTheIndicatorOrLayerActive() {
        val modifiers = RawModifiers()
        modifiers.update(1, 42, true)
        assertTrue(RemoteLayer.active(false, modifiers.active(1, UsbScanCodes.Action.SHIFT), true))
        modifiers.update(1, 42, false)
        assertFalse(RemoteLayer.active(false, modifiers.active(1, UsbScanCodes.Action.SHIFT), true))
        assertEquals("ت", resolve('T', 't'))
    }
    @Test fun actualAltAndOnScreenAltStillWork() {
        assertEquals("ء", resolve('Q', 'q', alt = true))
        assertEquals("ء", resolve('Q', 'q', armed = true))
        assertEquals("غ", resolve('G', '%', alt = true))
    }
    @Test fun englishAndNumberModesPreserveShiftedSymbolsAndDigits() {
        assertEquals("#", resolve(null, '#', shift = true, arabic = false))
        assertEquals("A", resolve('A', 'A', shift = true, arabic = false))
        assertEquals("1", resolve(null, '1', alt = true, legacy = true, numbers = true))
        assertEquals("#", resolve(null, '#', shift = true, numbers = true, custom = "ض"))
    }
    @Test fun ordinaryDigitsAreNotMistakenForTheBlueLayer() {
        for (digit in '0'..'9') assertEquals(digit.toString(), resolve(null, digit))
        assertEquals("ء", resolve(null, '1', legacy = true))
    }
    @Test fun compatibilityCanBeDisabledWithoutDisablingActualAlt() {
        assertFalse(RemoteLayer.active(false, true, false))
        assertEquals("ا", resolve('A', 'A', shift = true, compatibility = false))
        assertEquals("أ", resolve('A', 'A', alt = true, compatibility = false))
    }
    @Test fun rawUnknownScanCodesWorkWithShiftReportedAsAlt() {
        assertEquals("أ", resolve(UsbScanCodes.letters[30], UsbScanCodes.character(30, true), shift = true))
        assertEquals("ُ", resolve(null, UsbScanCodes.character(4, true), shift = true))
    }
}
