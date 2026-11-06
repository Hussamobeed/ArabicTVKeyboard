package com.herannet.arabictvkeyboard

/** Shared by typing, calibration and the indicator. Raw modifier identity is never rewritten. */
object RemoteLayer {
    fun active(alt: Boolean, shift: Boolean, shiftIsAlt: Boolean) = alt || (shift && shiftIsAlt)
    fun calibrated(raw: String?, shifted: String?, alt: String?, requested: Boolean, physicalLayer: Boolean): String? =
        if (requested && !physicalLayer) shifted ?: alt else raw
    fun resolve(letter: Char?, character: Char?, armed: Boolean, alt: Boolean, shift: Boolean,
                shiftIsAlt: Boolean, legacyBlue: Boolean, arabic: Boolean, numbers: Boolean,
                custom: String? = null): String? {
        if (!arabic || numbers) return character?.takeUnless { it.isISOControl() }?.toString()
        // Keep the owner's existing calibrated SHIFT+scanCode and ALT+scanCode mappings first.
        if (custom != null) return custom
        val layer = armed || active(alt, shift, shiftIsAlt)
        // Mini remotes can synthesize SHIFT+3 => # for the physical blue D legend.
        // Decode that symbol before interpreting the synthetic physical scan code.
        val blueSource = character?.let { KeyboardLayout.blueSymbols[it] }
        if (blueSource != null && (legacyBlue || (layer && (letter == null || character?.isLetter() != true)))) {
            return KeyboardLayout.secondary[blueSource] ?: KeyboardLayout.primary[blueSource]
        }
        if (letter != null && layer) {
            val key = letter.uppercaseChar()
            // Do not leak the remote's original symbols from an unmapped secondary key.
            return KeyboardLayout.secondary[key] ?: KeyboardLayout.primary[key]
        }
        return KeyboardLayout.resolve(letter, character, false, false, false)
    }
}
