package com.herannet.arabictvkeyboard

import android.view.KeyEvent

object RemoteKeys {
    fun device(event: KeyEvent): String = event.device?.descriptor ?: "device:${event.deviceId}"
    fun identity(event: KeyEvent, shift: Boolean, alt: Boolean): String =
        UsbScanCodes.identity(device(event), event.keyCode, event.scanCode, event.unicodeChar, shift, alt)
    fun pressedIdentity(event: KeyEvent) = UsbScanCodes.pressedIdentity(event.deviceId, event.keyCode, event.scanCode)
    fun actionKeyCode(event: KeyEvent): Int {
        if (event.keyCode != KeyEvent.KEYCODE_UNKNOWN) return event.keyCode
        return when (UsbScanCodes.action(event.scanCode)) {
            UsbScanCodes.Action.SHIFT -> KeyEvent.KEYCODE_SHIFT_LEFT
            UsbScanCodes.Action.ALT -> KeyEvent.KEYCODE_ALT_LEFT
            UsbScanCodes.Action.CTRL -> KeyEvent.KEYCODE_CTRL_LEFT
            UsbScanCodes.Action.META -> KeyEvent.KEYCODE_META_LEFT
            UsbScanCodes.Action.CAPS -> KeyEvent.KEYCODE_CAPS_LOCK
            UsbScanCodes.Action.BACKSPACE -> KeyEvent.KEYCODE_DEL
            UsbScanCodes.Action.ENTER -> KeyEvent.KEYCODE_ENTER
            UsbScanCodes.Action.SPACE -> KeyEvent.KEYCODE_SPACE
            UsbScanCodes.Action.ALT_LAYER -> KeyEvent.KEYCODE_F2
            UsbScanCodes.Action.NUMBER_LAYER -> KeyEvent.KEYCODE_F3
            UsbScanCodes.Action.HIDE -> KeyEvent.KEYCODE_ESCAPE
            else -> event.keyCode
        }
    }
    fun letter(event: KeyEvent): Char? = when {
        event.keyCode in KeyEvent.KEYCODE_A..KeyEvent.KEYCODE_Z -> ('A'.code + event.keyCode - KeyEvent.KEYCODE_A).toChar()
        event.keyCode == KeyEvent.KEYCODE_UNKNOWN -> UsbScanCodes.letters[event.scanCode]
        else -> null
    }
    fun character(event: KeyEvent, shift: Boolean, caps: Boolean): Char? {
        val unicode = event.unicodeChar
        if (unicode in 32..0xFFFF) return unicode.toChar()
        if (event.keyCode == KeyEvent.KEYCODE_UNKNOWN) return UsbScanCodes.character(event.scanCode, shift, caps)
        if (event.keyCode in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9) return ('0'.code + event.keyCode - KeyEvent.KEYCODE_0).toChar()
        if (event.keyCode in KeyEvent.KEYCODE_NUMPAD_0..KeyEvent.KEYCODE_NUMPAD_9) return ('0'.code + event.keyCode - KeyEvent.KEYCODE_NUMPAD_0).toChar()
        return letter(event)?.let { if (shift xor caps) it else it.lowercaseChar() }
    }
}
