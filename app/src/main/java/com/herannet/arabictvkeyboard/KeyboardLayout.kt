package com.herannet.arabictvkeyboard

/** Invented mnemonic layout. This is deliberately not the standard Arabic PC layout. */
object KeyboardLayout {
    val rows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")
    val primary = linkedMapOf(
        'Q' to "ق", 'W' to "و", 'E' to "ع", 'R' to "ر", 'T' to "ت",
        'Y' to "ي", 'U' to "ث", 'I' to "ذ", 'O' to "ش", 'P' to "ح",
        'A' to "ا", 'S' to "س", 'D' to "د", 'F' to "ف", 'G' to "غ",
        'H' to "ه", 'J' to "ج", 'K' to "ك", 'L' to "ل", 'Z' to "ز",
        'X' to "خ", 'C' to "ص", 'V' to "ض", 'B' to "ب", 'N' to "ن", 'M' to "م"
    )
    val numeric = rows.joinToString("").zip("1234567890@#$/&*()-+.,:!?_".map { it.toString() }).toMap()
    val secondary = linkedMapOf(
        'Q' to "ء", 'W' to "ؤ", 'E' to "إ", 'T' to "ط", 'Y' to "ى",
        'I' to "ئ", 'O' to "آ", 'A' to "أ", 'Z' to "ظ", 'B' to "ة",
        'F' to "َ", 'D' to "ُ", 'K' to "ِ", 'S' to "ْ", 'J' to "ّ",
        'N' to "ً", 'M' to "ٌ", 'L' to "ٍ", 'X' to "ـ"
    )
    // Symbols printed in blue on the supplied remote. Opt-in: digits conflict with ordinary numbers.
    val blueSymbols = mapOf(
        '1' to 'Q', '2' to 'W', '3' to 'E', '4' to 'R', '5' to 'T',
        '6' to 'Y', '7' to 'U', '8' to 'I', '9' to 'O', '0' to 'P',
        '?' to 'A', '!' to 'S', '#' to 'D', '$' to 'F', '%' to 'G',
        '&' to 'H', '*' to 'J', '(' to 'K', ')' to 'L', '+' to 'Z',
        '-' to 'X', '=' to 'C', '/' to 'V', '_' to 'B', '"' to 'N', ':' to 'M'
    )
    fun resolve(letter: Char?, unicode: Char?, alt: Boolean, legacyBlue: Boolean, numbers: Boolean): String? {
        if (numbers) return unicode?.takeIf { !it.isISOControl() }?.toString()
        if (legacyBlue && unicode != null && blueSymbols.containsKey(unicode)) {
            val source = blueSymbols.getValue(unicode)
            return secondary[source] ?: primary[source]
        }
        if (letter != null) {
            val key = letter.uppercaseChar()
            if (alt) return secondary[key] ?: unicode?.takeIf { !it.isISOControl() }?.toString()
            return primary[key]
        }
        return unicode?.takeIf { !it.isISOControl() }?.toString()
    }
    fun signature(keyCode: Int, unicode: Int): String = "$keyCode:$unicode"
}
