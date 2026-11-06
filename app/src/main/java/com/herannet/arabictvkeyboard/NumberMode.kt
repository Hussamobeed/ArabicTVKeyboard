package com.herannet.arabictvkeyboard

/** Match the blue numeric/symbol legends of the owner's mini QWERTY remote without FN. */
object NumberMode {
    val legends = linkedMapOf(
        'Q' to "1", 'W' to "2", 'E' to "3", 'R' to "4", 'T' to "5",
        'Y' to "6", 'U' to "7", 'I' to "8", 'O' to "9", 'P' to "0",
        'A' to "?", 'S' to "!", 'D' to "#", 'F' to "$", 'G' to "%",
        'H' to "&", 'J' to "*", 'K' to "(", 'L' to ")", 'Z' to "+",
        'X' to "-", 'C' to "=", 'V' to "/", 'B' to "_", 'N' to "\"", 'M' to ":"
    )
    fun resolve(letter: Char?, character: Char?, scanCode: Int = 0): String? {
        // Dot must remain a dot even when firmware leaves SHIFT/FN on. The colon key is M,
        // or the real semicolon/colon scan code used by the firmware's synthetic combo.
        if (scanCode in 2..11) return "1234567890"[scanCode - 2].toString()
        if (scanCode == 52 || scanCode == 83) return "."
        if (scanCode == 39 || character == ';') return ":"
        if (character == '.' || character == ':' || character?.isDigit() == true) return character.toString()
        if (character != null && !character.isLetter() && !character.isISOControl()) return character.toString()
        if (letter != null) return legends[letter.uppercaseChar()]
        return null
    }
}
