package com.herannet.arabictvkeyboard

import android.view.KeyEvent

/** Logical controls that can be assigned to buttons on the user's air-mouse remote. */
enum class GamepadButton(val label: String) {
    DPAD_UP("D-pad Up"),
    DPAD_DOWN("D-pad Down"),
    DPAD_LEFT("D-pad Left"),
    DPAD_RIGHT("D-pad Right"),
    A("A"),
    B("B"),
    X("X"),
    Y("Y"),
    START("Start"),
    SELECT("Select"),
    L1("L1"),
    R1("R1"),
    L2("L2"),
    R2("R2")
}

data class GamepadBinding(
    val deviceDescriptor: String,
    val keyCode: Int,
    val scanCode: Int
) {
    fun matches(event: KeyEvent): Boolean =
        deviceDescriptor == (event.device?.descriptor ?: "device:${event.deviceId}") &&
            keyCode == event.keyCode && scanCode == event.scanCode
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
