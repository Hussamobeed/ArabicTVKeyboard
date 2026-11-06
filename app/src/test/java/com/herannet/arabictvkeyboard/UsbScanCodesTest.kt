package com.herannet.arabictvkeyboard

import org.junit.Assert.*
import org.junit.Test

class UsbScanCodesTest {
    @Test fun screenshotUnknownScan31RecoversSAndArabicSeen() {
        assertEquals('S', UsbScanCodes.letters[31])
        assertEquals('s', UsbScanCodes.character(31))
        assertEquals("س", KeyboardLayout.resolve(UsbScanCodes.letters[31], UsbScanCodes.character(31), false, false, false))
    }
    @Test fun unknownPrintableKeysCanBeCalibratedWithoutUnicode() {
        assertTrue(UsbScanCodes.canAssign(0, 31, 0))
        assertTrue(UsbScanCodes.canAssign(0, 32, 0))
        assertFalse(UsbScanCodes.canAssign(0, 0, 0))
        assertFalse(UsbScanCodes.canAssign(0, 56, 0))
        assertFalse(UsbScanCodes.canAssign(0, 103, 0))
        assertFalse(UsbScanCodes.canAssign(0, 115, 0))
    }
    @Test fun everyQwertyLetterHasADistinctScanCode() {
        assertEquals(26, UsbScanCodes.letters.size)
        assertEquals(('A'..'Z').toSet(), UsbScanCodes.letters.values.toSet())
        assertEquals("د", KeyboardLayout.resolve(UsbScanCodes.letters[32], UsbScanCodes.character(32), false, false, false))
        assertEquals("ك", KeyboardLayout.resolve(UsbScanCodes.letters[37], UsbScanCodes.character(37), false, false, false))
    }
    @Test fun rawNumbersWorkInBothTextAndNumberMode() {
        (2..11).forEachIndexed { index, scan ->
            val expected = "1234567890"[index].toString()
            val character = UsbScanCodes.character(scan)
            assertEquals(expected, KeyboardLayout.resolve(null, character, false, false, false))
            assertEquals(expected, KeyboardLayout.resolve(null, character, false, true, true))
        }
        assertEquals('1', UsbScanCodes.character(79))
        assertEquals('0', UsbScanCodes.character(82))
    }
    @Test fun manualMappingsAndReleasesDoNotCollapseAllUnknownKeysToZero() {
        val s = UsbScanCodes.identity("usb-a", 0, 31, 0, false, false)
        val d = UsbScanCodes.identity("usb-a", 0, 32, 0, false, false)
        assertNotEquals(s, d)
        assertNotEquals(s, UsbScanCodes.identity("usb-b", 0, 31, 0, false, false))
        assertNotEquals(s, UsbScanCodes.identity("usb-a", 0, 31, 0, false, true))
        assertNotEquals(UsbScanCodes.pressedIdentity(1, 0, 31), UsbScanCodes.pressedIdentity(1, 0, 32))
        val mappings = mapOf(s to "س", d to "د")
        assertEquals("س", mappings[s]); assertEquals("د", mappings[d])
    }
    @Test fun manualScanMappingSurvivesUnicodeChangesBetweenTestAndIme() {
        assertEquals(UsbScanCodes.identity("usb-a", 0, 31, 0, false, false),
            UsbScanCodes.identity("usb-a", 47, 31, 's'.code, false, false))
    }
    @Test fun modifiersSupportUnknownKeyCodesAndStayPerDevice() {
        val modifiers = RawModifiers()
        modifiers.update(1, 42, true)
        assertTrue(modifiers.active(1, UsbScanCodes.Action.SHIFT))
        assertFalse(modifiers.active(2, UsbScanCodes.Action.SHIFT))
        assertEquals('S', UsbScanCodes.character(31, modifiers.active(1, UsbScanCodes.Action.SHIFT)))
        modifiers.update(1, 42, false)
        assertFalse(modifiers.active(1, UsbScanCodes.Action.SHIFT))
        modifiers.update(1, 56, true)
        assertEquals("ط", KeyboardLayout.resolve(UsbScanCodes.letters[20], UsbScanCodes.character(20), modifiers.active(1, UsbScanCodes.Action.ALT), false, false))
        modifiers.clear()
        assertFalse(modifiers.active(1, UsbScanCodes.Action.ALT))
    }
    @Test fun scanFallbackLeavesNavigationAndMediaUntranslated() {
        for (scan in listOf(102, 103, 105, 106, 108, 113, 114, 115, 116, 158, 172, 999)) {
            assertNull(UsbScanCodes.character(scan))
            assertNull(UsbScanCodes.letters[scan])
            assertNull(UsbScanCodes.action(scan))
        }
    }
    @Test fun symbolsAndCapsHaveAsciiFallbacks() {
        assertEquals('!', UsbScanCodes.character(2, true))
        assertEquals('S', UsbScanCodes.character(31, false, true))
        assertEquals('s', UsbScanCodes.character(31, true, true))
        assertEquals('?', UsbScanCodes.character(53, true))
    }
}
