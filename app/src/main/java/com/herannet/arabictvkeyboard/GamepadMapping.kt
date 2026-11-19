package com.herannet.arabictvkeyboard

import android.view.KeyEvent

/** Logical controls that can be assigned to buttons on the user's air-mouse remote. */
enum class GamepadButton(val label: String, val fallbackKeyCode: Int) {
    DPAD_UP("D-pad Up", KeyEvent.KEYCODE_DPAD_UP),
    DPAD_DOWN("D-pad Down", KeyEvent.KEYCODE_DPAD_DOWN),
    DPAD_LEFT("D-pad Left", KeyEvent.KEYCODE_DPAD_LEFT),
    DPAD_RIGHT("D-pad Right", KeyEvent.KEYCODE_DPAD_RIGHT),
    A("A", KeyEvent.KEYCODE_Z),
    B("B", KeyEvent.KEYCODE_X),
    X("X", KeyEvent.KEYCODE_A),
    Y("Y", KeyEvent.KEYCODE_S),
    START("Start", KeyEvent.KEYCODE_ENTER),
    SELECT("Select", KeyEvent.KEYCODE_SPACE),
    L1("L1", KeyEvent.KEYCODE_Q),
    R1("R1", KeyEvent.KEYCODE_W),
    L2("L2", KeyEvent.KEYCODE_E),
    R2("R2", KeyEvent.KEYCODE_R)
}

data class GamepadBinding(
    val deviceDescriptor: String,
    val keyCode: Int,
    val scanCode: Int
) {
    fun matches(event: KeyEvent): Boolean =
        (deviceDescriptor == "*" || deviceDescriptor == (event.device?.descriptor ?: "device:${event.deviceId}")) &&
            keyCode == event.keyCode && (scanCode == 0 || scanCode == event.scanCode)
}

/** Standard keyboard defaults. Remote-specific bindings override these after calibration. */
object GamepadDefaults {
    fun bindings(): Map<GamepadButton, GamepadBinding> = mapOf(
        GamepadButton.DPAD_UP to binding(KeyEvent.KEYCODE_DPAD_UP),
        GamepadButton.DPAD_DOWN to binding(KeyEvent.KEYCODE_DPAD_DOWN),
        GamepadButton.DPAD_LEFT to binding(KeyEvent.KEYCODE_DPAD_LEFT),
        GamepadButton.DPAD_RIGHT to binding(KeyEvent.KEYCODE_DPAD_RIGHT),
        GamepadButton.A to binding(KeyEvent.KEYCODE_Z),
        GamepadButton.B to binding(KeyEvent.KEYCODE_X),
        GamepadButton.X to binding(KeyEvent.KEYCODE_A),
        GamepadButton.Y to binding(KeyEvent.KEYCODE_S),
        GamepadButton.START to binding(KeyEvent.KEYCODE_ENTER),
        GamepadButton.SELECT to binding(KeyEvent.KEYCODE_SPACE),
        GamepadButton.L1 to binding(KeyEvent.KEYCODE_Q),
        GamepadButton.R1 to binding(KeyEvent.KEYCODE_W),
        GamepadButton.L2 to binding(KeyEvent.KEYCODE_E),
        GamepadButton.R2 to binding(KeyEvent.KEYCODE_R)
    )

    private fun binding(keyCode: Int) = GamepadBinding("*", keyCode, 0)
}

/** Stable, delimiter-safe persistence for the experimental profile. */
object GamepadBindings {
    fun encode(bindings: Map<GamepadButton, GamepadBinding>): String = bindings.entries.joinToString(";") { (button, binding) ->
        listOf(
            button.name,
            binding.deviceDescriptor.toByteArray(Charsets.UTF_8).joinToString("") { "%02x".format(it.toInt() and 0xff) },
            binding.keyCode.toString(),
            binding.scanCode.toString()
        ).joinToString(",")
    }

    fun decode(value: String): Map<GamepadButton, GamepadBinding> = buildMap {
        value.split(';').forEach { row ->
            val fields = row.split(',')
            if (fields.size != 4) return@forEach
            val button = runCatching { GamepadButton.valueOf(fields[0]) }.getOrNull() ?: return@forEach
            val descriptor = runCatching {
                String(fields[1].chunked(2).map { it.toInt(16).toByte() }.toByteArray(), Charsets.UTF_8)
            }.getOrNull() ?: return@forEach
            val keyCode = fields[2].toIntOrNull() ?: return@forEach
            val scanCode = fields[3].toIntOrNull() ?: return@forEach
            put(button, GamepadBinding(descriptor, keyCode, scanCode))
        }
    }

    fun find(event: KeyEvent, bindings: Map<GamepadButton, GamepadBinding>): GamepadButton? =
        bindings.entries.firstOrNull { it.value.matches(event) }?.key
}

/** System controls remain outside the experimental Gamepad profile. */
object GamepadSafety {
    val blockedKeyCodes = setOf(
        KeyEvent.KEYCODE_HOME,
        KeyEvent.KEYCODE_BACK,
        KeyEvent.KEYCODE_POWER,
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_MUTE
    )
}
