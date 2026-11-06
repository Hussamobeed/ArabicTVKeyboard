package com.herannet.arabictvkeyboard

import android.content.Context

class Prefs(context: Context) {
    private val prefs = context.getSharedPreferences("keyboard", Context.MODE_PRIVATE)
    var arabic: Boolean
        get() = prefs.getBoolean("arabic", true)
        set(value) { prefs.edit().putBoolean("arabic", value).apply() }
    var showGuide: Boolean
        get() = prefs.getBoolean("guide", true)
        set(value) { prefs.edit().putBoolean("guide", value).apply() }
    var overlayGuide: Boolean
        get() = prefs.getBoolean("overlay", true)
        set(value) { prefs.edit().putBoolean("overlay", value).apply() }
    var blueSymbols: Boolean
        get() = prefs.getBoolean("blue", false)
        set(value) { prefs.edit().putBoolean("blue", value).apply() }
    var panelOpacity: Int
        get() = prefs.getInt("opacity", 80)
        set(value) { prefs.edit().putInt("opacity", value.coerceIn(35, 100)).apply() }
    fun mapped(keyCode: Int, unicode: Int): String? = prefs.getString("map:${KeyboardLayout.signature(keyCode, unicode)}", null)
    fun map(keyCode: Int, unicode: Int, text: String) { prefs.edit().putString("map:${KeyboardLayout.signature(keyCode, unicode)}", text).apply() }
    fun clearMappings() {
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith("map:") }.forEach { editor.remove(it) }
        editor.apply()
    }
    fun mappingCount(): Int = prefs.all.keys.count { it.startsWith("map:") }
}
