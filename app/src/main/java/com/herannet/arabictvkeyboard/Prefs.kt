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
    var shiftIsAlt: Boolean
        get() = prefs.getBoolean("shiftIsAlt", true)
        set(value) { prefs.edit().putBoolean("shiftIsAlt", value).apply() }
    var guideSizePercent: Int
        get() = prefs.getInt("guideSize", 65).coerceIn(45, 100)
        set(value) { prefs.edit().putInt("guideSize", value.coerceIn(45, 100)).apply() }
    var panelOpacity: Int
        get() = prefs.getInt("opacity", 80)
        set(value) { prefs.edit().putInt("opacity", value.coerceIn(35, 100)).apply() }
    fun mapped(identity: String, keyCode: Int, unicode: Int): String? =
        prefs.getString("map2:$identity", null)
            ?: if (keyCode != 0 && unicode >= 32) prefs.getString("map:${KeyboardLayout.signature(keyCode, unicode)}", null) else null
    fun map(identity: String, text: String) { prefs.edit().putString("map2:$identity", text).apply() }
    fun clearMappings() {
        val editor = prefs.edit()
        prefs.all.keys.filter { (it.startsWith("map:") || it.startsWith("map2:")) }.forEach { editor.remove(it) }
        editor.apply()
    }
    fun mappingCount(): Int = prefs.all.keys.count { (it.startsWith("map:") || it.startsWith("map2:")) }
}
