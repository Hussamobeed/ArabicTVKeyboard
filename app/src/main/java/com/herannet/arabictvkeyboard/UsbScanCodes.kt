package com.herannet.arabictvkeyboard

/** Linux evdev scan codes, as used by Android's AOSP Generic.kl for USB keyboards.
 * Only use automatic recovery for KEYCODE_UNKNOWN. Never reinterpret a known remote action.
 * Reference: https://android.googlesource.com/platform/frameworks/base/+/master/data/keyboards/Generic.kl
 */
object UsbScanCodes {
    val letters: Map<Int, Char> = (16..25).zip("QWERTYUIOP".toList()).toMap() +
        (30..38).zip("ASDFGHJKL".toList()).toMap() + (44..50).zip("ZXCVBNM".toList()).toMap()
    private val punctuation = mapOf(
        12 to ('-' to '_'), 13 to ('=' to '+'), 26 to ('[' to '{'), 27 to (']' to '}'),
        39 to (';' to ':'), 40 to ('\'' to '"'), 41 to ('`' to '~'), 43 to ('\\' to '|'),
        51 to (',' to '<'), 52 to ('.' to '>'), 53 to ('/' to '?')
    )
    private val keypad = mapOf(71 to '7', 72 to '8', 73 to '9', 75 to '4', 76 to '5',
        77 to '6', 79 to '1', 80 to '2', 81 to '3', 82 to '0', 83 to '.', 55 to '*', 74 to '-', 78 to '+', 98 to '/')
    enum class Action { SHIFT, ALT, CTRL, META, CAPS, BACKSPACE, ENTER, SPACE, ALT_LAYER, NUMBER_LAYER, HIDE }
    fun action(scan: Int): Action? = when (scan) {
        42, 54 -> Action.SHIFT
        56, 100 -> Action.ALT
        29, 97 -> Action.CTRL
        125, 126 -> Action.META
        58 -> Action.CAPS
        14 -> Action.BACKSPACE
        28, 96 -> Action.ENTER
        57 -> Action.SPACE
        60 -> Action.ALT_LAYER
        61 -> Action.NUMBER_LAYER
        1 -> Action.HIDE
        else -> null
    }
    fun character(scan: Int, shift: Boolean = false, caps: Boolean = false): Char? {
        letters[scan]?.let { return if (shift xor caps) it else it.lowercaseChar() }
        if (scan in 2..11) return (if (shift) "!@#$%^&*()" else "1234567890")[scan - 2]
        punctuation[scan]?.let { return if (shift) it.second else it.first }
        return keypad[scan]
    }
    fun canAssign(keyCode: Int, scanCode: Int, unicode: Int): Boolean =
        (keyCode != 0 || scanCode > 0 || unicode >= 32) &&
            action(scanCode) == null && scanCode !in setOf(15, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 113, 114, 115, 116, 158, 172)
    fun identity(device: String, keyCode: Int, scanCode: Int, unicode: Int, shift: Boolean, alt: Boolean): String {
        val devicePart = "${device.length}:$device"
        return if (scanCode > 0) "$devicePart:scan:$scanCode:$shift:$alt"
        else "$devicePart:key:$keyCode:$unicode:$shift:$alt"
    }
    fun pressedIdentity(deviceId: Int, keyCode: Int, scanCode: Int) = "$deviceId:$keyCode:$scanCode"
}

/** Holds only physical modifier state, per USB input device, never text. */
class RawModifiers {
    private val held = mutableSetOf<Pair<Int, Int>>()
    private val caps = mutableSetOf<Int>()
    fun update(device: Int, scan: Int, down: Boolean, repeat: Int = 0) {
        when (UsbScanCodes.action(scan)) {
            UsbScanCodes.Action.SHIFT, UsbScanCodes.Action.ALT, UsbScanCodes.Action.CTRL, UsbScanCodes.Action.META ->
                if (down) held.add(device to scan) else held.remove(device to scan)
            UsbScanCodes.Action.CAPS -> if (down && repeat == 0) { if (!caps.add(device)) caps.remove(device) }
            else -> Unit
        }
    }
    fun active(device: Int, action: UsbScanCodes.Action) = held.any { it.first == device && UsbScanCodes.action(it.second) == action }
    fun caps(device: Int) = device in caps
    fun clear() { held.clear(); caps.clear() }
}
