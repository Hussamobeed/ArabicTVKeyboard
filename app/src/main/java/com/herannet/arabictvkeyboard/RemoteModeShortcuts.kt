package com.herannet.arabictvkeyboard

import java.util.Locale

/** Explicit owner-requested overrides for two UNKNOWN buttons on this USB remote only. */
object RemoteModeShortcuts {
    enum class Action { LOCK_ALT, NUMBERS }
    data class State(val altLocked: Boolean = false, val numbers: Boolean = false)
    fun binding(deviceName: String?, keyCode: Int, scanCode: Int): Action? {
        if (keyCode != 0) return null
        val name = deviceName.orEmpty().lowercase(Locale.ROOT)
        if (!name.contains("2.4g") || !name.contains("composite") || !name.contains("wireless") || !name.contains("mouse")) return null
        return when (scanCode) { 165 -> Action.LOCK_ALT; 163 -> Action.NUMBERS; else -> null }
    }
    fun apply(state: State, action: Action?, repeat: Int = 0): State {
        if (repeat != 0) return state
        return when (action) {
            Action.LOCK_ALT -> state.copy(altLocked = !state.altLocked, numbers = if (!state.altLocked) false else state.numbers)
            Action.NUMBERS -> state.copy(numbers = !state.numbers)
            null -> state
        }
    }
}
