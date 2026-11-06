package com.herannet.arabictvkeyboard

import org.junit.Assert.*
import org.junit.Test

class RemoteModeShortcutsTest {
    private val mouse = "2.4G Composite Devic Wireless Devic Mouse"
    private val alt = RemoteModeShortcuts.Action.LOCK_ALT
    private val nums = RemoteModeShortcuts.Action.NUMBERS

    @Test fun exactReportedUnknownButtonsHaveReadyBindings() {
        assertEquals(alt, RemoteModeShortcuts.binding(mouse, 0, 165))
        assertEquals(nums, RemoteModeShortcuts.binding(mouse, 0, 163))
        assertEquals(alt, RemoteModeShortcuts.binding(mouse.lowercase(), 0, 165))
    }
    @Test fun knownMediaKeysAndOtherDevicesKeepTheirOriginalFunction() {
        assertNull(RemoteModeShortcuts.binding(mouse, 87, 163))
        assertNull(RemoteModeShortcuts.binding(mouse, 88, 165))
        assertNull(RemoteModeShortcuts.binding("Other USB Keyboard", 0, 165))
        assertNull(RemoteModeShortcuts.binding(null, 0, 163))
        assertNull(RemoteModeShortcuts.binding(mouse, 0, 31))
    }
    @Test fun lockStaysEnabledAcrossTypingAndSpacesUntilSecond165() {
        var state = RemoteModeShortcuts.apply(RemoteModeShortcuts.State(), alt)
        for (scan in listOf(16, 57, 32, 57, 37, 52, 39)) {
            state = RemoteModeShortcuts.apply(state, RemoteModeShortcuts.binding(mouse, 0, scan))
            assertTrue(state.altLocked)
        }
        assertFalse(RemoteModeShortcuts.apply(state, alt).altLocked)
    }
    @Test fun autoRepeatNeverFlipsAStateAgain() {
        val locked = RemoteModeShortcuts.apply(RemoteModeShortcuts.State(), alt)
        val numbers = RemoteModeShortcuts.apply(locked, nums)
        for (repeat in 1..10) {
            assertEquals(locked, RemoteModeShortcuts.apply(locked, alt, repeat))
            assertEquals(numbers, RemoteModeShortcuts.apply(numbers, nums, repeat))
        }
    }
    @Test fun numberModeTemporarilyOverridesButDoesNotClearAltLock() {
        val locked = RemoteModeShortcuts.apply(RemoteModeShortcuts.State(), alt)
        val numbers = RemoteModeShortcuts.apply(locked, nums)
        assertTrue(numbers.altLocked); assertTrue(numbers.numbers)
        assertEquals(locked, RemoteModeShortcuts.apply(numbers, nums))
    }
    @Test fun activatingAltFromNumbersReturnsToTheArabicLayer() {
        val numbers = RemoteModeShortcuts.State(numbers = true)
        assertEquals(RemoteModeShortcuts.State(altLocked = true), RemoteModeShortcuts.apply(numbers, alt))
    }
    @Test fun topQwertyRowTypesDigitsWithoutPhysicalFn() {
        assertEquals("1234567890", "QWERTYUIOP".map { NumberMode.resolve(it, it.lowercaseChar()) }.joinToString(""))
        assertEquals("1234567890", "QWERTYUIOP".map { NumberMode.resolve(it, it) }.joinToString(""))
    }
    @Test fun dotAndColonWorkWithAndWithoutShiftAndUnicode() {
        assertEquals(".", NumberMode.resolve(null, '.', 52))
        assertEquals(".", NumberMode.resolve(null, '>', 52))
        assertEquals(".", NumberMode.resolve(null, null, 52))
        assertEquals(".", NumberMode.resolve(null, null, 83))
        assertEquals(":", NumberMode.resolve('M', 'm', 50))
        assertEquals(":", NumberMode.resolve('M', 'M', 50))
        assertEquals(":", NumberMode.resolve(null, ':'))
        assertEquals(":", NumberMode.resolve(null, ';', 39))
        assertEquals(":", NumberMode.resolve(null, null, 39))
    }
    @Test fun stickyAltDoesNotTurnNumbersDotOrColonIntoArabic() {
        val state = RemoteModeShortcuts.apply(RemoteModeShortcuts.apply(RemoteModeShortcuts.State(), alt), nums)
        assertTrue(state.altLocked && state.numbers)
        assertEquals("1", NumberMode.resolve('Q', 'Q'))
        assertEquals(".", NumberMode.resolve(null, '>', 52))
        assertEquals(":", NumberMode.resolve('M', 'M'))
    }
    @Test fun literalDigitScansRemainNumericEvenWithShiftHeld() {
        assertEquals("1234567890", (2..11).map { NumberMode.resolve(null, UsbScanCodes.character(it, true), it) }.joinToString(""))
    }
    @Test fun lockedAltDoesNotReuseAPrimaryManualMappingAsSecondary() {
        val secondary = RemoteLayer.calibrated("ق", "ئ", null, true, false)
        assertEquals("ئ", secondary)
        val fallback = RemoteLayer.calibrated("ق", null, null, true, false)
        assertNull(fallback)
        assertEquals("ء", RemoteLayer.resolve('Q', 'q', true, false, false, true, false, true, false, fallback))
        assertEquals("ق", RemoteLayer.calibrated("ق", "ئ", null, false, false))
        assertEquals("ئ", RemoteLayer.calibrated("ئ", null, null, true, true))
    }
    @Test fun networkAddressCanBeTypedWithTheRequestedKeys() {
        val keys = listOf('Q', 'O', 'W', '.', 'Q', 'Y', 'I', '.', 'Q', '.', 'Q', 'M', 'I', 'P', 'I', 'P')
        assertEquals("192.168.1.1:8080", keys.joinToString("") {
            if (it == '.') NumberMode.resolve(null, '>', 52)!! else NumberMode.resolve(it, it.lowercaseChar())!!
        })
    }
}
