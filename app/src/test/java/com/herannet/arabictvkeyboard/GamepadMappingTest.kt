package com.herannet.arabictvkeyboard

import android.view.KeyEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GamepadMappingTest {
    @Test
    fun bindings_roundTripWithDeviceDescriptor() {
        val original = mapOf(
            GamepadButton.A to GamepadBinding("usb:receiver,1", KeyEvent.KEYCODE_BUTTON_A, 42),
            GamepadButton.DPAD_LEFT to GamepadBinding("usb:receiver,1", KeyEvent.KEYCODE_DPAD_LEFT, 105)
        )

        assertEquals(original, GamepadBindings.decode(GamepadBindings.encode(original)))
    }

    @Test
    fun systemControlsAreProtected() {
        assertTrue(KeyEvent.KEYCODE_HOME in GamepadSafety.blockedKeyCodes)
        assertTrue(KeyEvent.KEYCODE_BACK in GamepadSafety.blockedKeyCodes)
        assertTrue(KeyEvent.KEYCODE_POWER in GamepadSafety.blockedKeyCodes)
        assertTrue(KeyEvent.KEYCODE_VOLUME_UP in GamepadSafety.blockedKeyCodes)
    }

    @Test
    fun defaultsProvideEditableKeyboardLayout() {
        assertEquals(KeyEvent.KEYCODE_Z, GamepadButton.A.fallbackKeyCode)
        assertEquals(KeyEvent.KEYCODE_DPAD_UP, GamepadDefaults.bindings().getValue(GamepadButton.DPAD_UP).keyCode)
        assertEquals("*", GamepadDefaults.bindings().getValue(GamepadButton.A).deviceDescriptor)
    }
}
