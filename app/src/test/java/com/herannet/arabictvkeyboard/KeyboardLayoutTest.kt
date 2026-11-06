package com.herannet.arabictvkeyboard

import org.junit.Assert.*
import org.junit.Test

class KeyboardLayoutTest {
    @Test fun allAlphabetLettersAndVariantsAreReachable() {
        val reachable = (KeyboardLayout.primary.values + KeyboardLayout.secondary.values).toSet()
        "ابتثجحخدذرزسشصضطظعغفقكلمنهويءؤئأإآىة".forEach { assertTrue("Missing $it", it.toString() in reachable) }
        assertEquals(26, KeyboardLayout.primary.size)
    }
    @Test fun numericScreenCoversAllPhysicalKeys() {
        assertEquals(KeyboardLayout.primary.keys, KeyboardLayout.numeric.keys)
        assertEquals("1234567890", KeyboardLayout.rows[0].map { KeyboardLayout.numeric[it] }.joinToString(""))
    }
    @Test fun physicalKeysMatchTheApprovedLayout() {
        assertEquals("د", KeyboardLayout.resolve('D', 'd', false, false, false))
        assertEquals("ك", KeyboardLayout.resolve('K', 'k', false, false, false))
        assertEquals("ث", KeyboardLayout.primary['U'])
        assertEquals("ذ", KeyboardLayout.primary['I'])
        assertEquals("ش", KeyboardLayout.primary['O'])
        assertEquals("ظ", KeyboardLayout.resolve('Z', '+', true, false, false))
        assertEquals("ط", KeyboardLayout.resolve('T', 't', true, false, false))
    }
    @Test fun firmwareSymbolsAndTrueAltReachTheSameLetter() {
        KeyboardLayout.blueSymbols.forEach { (symbol, source) ->
            if (source in KeyboardLayout.secondary) {
                assertEquals(KeyboardLayout.secondary[source], KeyboardLayout.resolve(null, symbol, false, true, false))
                assertEquals(KeyboardLayout.secondary[source], KeyboardLayout.resolve(source, source.lowercaseChar(), true, false, false))
            }
        }
    }
    @Test fun blueDigitsRequireOptInAndNumberModeAlwaysPreservesNumbers() {
        assertEquals("1", KeyboardLayout.resolve(null, '1', false, false, false))
        assertEquals("ء", KeyboardLayout.resolve(null, '1', false, true, false))
        for (digit in '0'..'9') assertEquals(digit.toString(), KeyboardLayout.resolve(null, digit, true, true, true))
    }
    @Test fun unknownControlsDoNotProduceCharacters() {
        assertNull(KeyboardLayout.resolve(null, '\n', false, false, false))
        assertNull(KeyboardLayout.resolve(null, null, false, false, false))
        assertEquals("@", KeyboardLayout.resolve(null, '@', false, false, false))
        assertEquals("r", KeyboardLayout.resolve('R', 'r', true, false, false))
    }
}
