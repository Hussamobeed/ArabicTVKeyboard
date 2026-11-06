package com.herannet.arabictvkeyboard

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.KeyEvent
import android.widget.TextView
import android.widget.Toast

class RemoteTestActivity : Activity() {
    private lateinit var prefs: Prefs
    private lateinit var report: TextView
    private lateinit var layerStatus: TextView
    private var pending: String? = null
    private val modifiers = RawModifiers()
    private val captured = mutableSetOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); prefs = Prefs(this)
        val root = Ui.column(this); setContentView(root)
        root.addView(Ui.text(this, "اختبار الريموت / Remote test", 25f))
        root.addView(Ui.text(this, "اضغط ALT ثم D أو Q. سترى ما يرسله الريموت.\nلتخصيص زر، اختر الحرف المطلوب ثم اضغط الزر أو ALT مع الزر.\nيدعم KEYCODE_UNKNOWN باستخدام scanCode. لا تحتاج تعيين كل الحروف إذا ظهر التفسير الصحيح. F3 للأرقام.", 17f))
        layerStatus = Ui.text(this, "ALT غير مفعّل", 19f)
        root.addView(layerStatus)
        report = Ui.text(this, "لم يُضغط زر بعد", 17f)
        root.addView(report)
        root.addView(Ui.button(this, "تعيين زر إلى حرف عربي") {
            val letters = (KeyboardLayout.primary.values + KeyboardLayout.secondary.values + ('0'..'9').map { it.toString() } + listOf("،", ".", "؟", "@", "/")).distinct()
            AlertDialog.Builder(this).setTitle("اختر الحرف ثم اضغط زر الريموت")
                .setItems(letters.toTypedArray()) { _, which ->
                    pending = letters[which]; report.text = "اضغط الزر الذي سيكتب ${pending} • BACK للإلغاء"
                }.show()
        })
        root.addView(Ui.button(this, "مسح تعييناتي (${prefs.mappingCount()})") {
            AlertDialog.Builder(this).setMessage("مسح التعيينات اليدوية والعودة للتوزيع الأصلي؟")
                .setPositiveButton("مسح") { _, _ -> prefs.clearMappings(); Toast.makeText(this, "تم مسح التعيينات", Toast.LENGTH_SHORT).show() }
                .setNegativeButton("إلغاء", null).show()
        })
        root.addView(Ui.button(this, "رجوع") { finish() })
    }
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val pressed = RemoteKeys.pressedIdentity(event)
        if (event.action == KeyEvent.ACTION_UP) {
            modifiers.update(event.deviceId, event.scanCode, false)
            val released = RemoteKeys.actionKeyCode(event)
            val shift = modifiers.active(event.deviceId, UsbScanCodes.Action.SHIFT) ||
                (released !in setOf(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT) && event.isShiftPressed)
            val alt = modifiers.active(event.deviceId, UsbScanCodes.Action.ALT) ||
                (released !in setOf(KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT) && event.isAltPressed)
            showLayer(alt, shift)
            if (captured.remove(pressed)) return true
        }
        if (event.action == KeyEvent.ACTION_DOWN) {
            modifiers.update(event.deviceId, event.scanCode, true, event.repeatCount)
            val code = event.keyCode
            if (code == KeyEvent.KEYCODE_BACK && pending != null) { pending = null; report.text = "أُلغي التعيين"; captured.add(pressed); return true }
            val shift = event.isShiftPressed || modifiers.active(event.deviceId, UsbScanCodes.Action.SHIFT)
            val alt = event.isAltPressed || modifiers.active(event.deviceId, UsbScanCodes.Action.ALT)
            showLayer(alt, shift)
            val character = RemoteKeys.character(event, shift, modifiers.caps(event.deviceId))
            val letter = RemoteKeys.letter(event)
            val physicalLayer = RemoteLayer.active(alt, shift, prefs.shiftIsAlt)
            val custom = RemoteLayer.calibrated(
                prefs.mapped(RemoteKeys.identity(event, shift, alt), code, event.unicodeChar),
                prefs.mapped(RemoteKeys.identity(event, true, false), 0, 0),
                prefs.mapped(RemoteKeys.identity(event, false, true), 0, 0), prefs.remoteAltLocked, physicalLayer)
            val resolved = if (prefs.remoteNumbers) NumberMode.resolve(letter, character, event.scanCode)
                else RemoteLayer.resolve(letter, character, prefs.remoteAltLocked, alt, shift, prefs.shiftIsAlt,
                prefs.blueSymbols, true, false, custom)
            report.text = "${KeyEvent.keyCodeToString(code)}\nkeyCode=$code  scanCode=${event.scanCode}\nunicode=${event.unicodeChar}  ASCII=${character ?: '—'}  عربي=${resolved ?: "—"}\nALT=$alt  SHIFT=$shift  repeat=${event.repeatCount}\nUSB: ${event.device?.name ?: "—"}"
            val shortcut = RemoteModeShortcuts.binding(event.device?.name, code, event.scanCode)
            if (shortcut != null) {
                report.append(if (shortcut == RemoteModeShortcuts.Action.LOCK_ALT) "\nزر ALT المثبت جاهز — يعمل داخل صندوق الكتابة" else "\nزر وضع الأرقام جاهز — يعمل داخل صندوق الكتابة")
                captured.add(pressed); return true
            }
            val blocked = setOf(KeyEvent.KEYCODE_HOME, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_VOLUME_UP,
                KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_MUTE, KeyEvent.KEYCODE_POWER,
                KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_TAB,
                KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT, KeyEvent.KEYCODE_ALT_LEFT,
                KeyEvent.KEYCODE_ALT_RIGHT, KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_CTRL_RIGHT,
                KeyEvent.KEYCODE_META_LEFT, KeyEvent.KEYCODE_META_RIGHT, KeyEvent.KEYCODE_CAPS_LOCK)
            if (pending != null && RemoteKeys.actionKeyCode(event) in setOf(KeyEvent.KEYCODE_SHIFT_LEFT,
                    KeyEvent.KEYCODE_SHIFT_RIGHT, KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT)) {
                report.append("\nالحرف المختار: ${pending} — اضغط الحرف مع ALT الآن")
                captured.add(pressed); return true
            }
            if (pending != null && code !in blocked && UsbScanCodes.canAssign(code, event.scanCode, event.unicodeChar)) {
                prefs.map(if (prefs.remoteAltLocked && !physicalLayer) RemoteKeys.identity(event, true, false)
                    else RemoteKeys.identity(event, shift, alt), pending!!)
                report.append("\nحُفظ التعيين إلى ${pending} باستخدام scanCode=${event.scanCode}")
                pending = null; captured.add(pressed); return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
    private fun showLayer(alt: Boolean, shift: Boolean) {
        val active = prefs.remoteAltLocked || RemoteLayer.active(alt, shift, prefs.shiftIsAlt)
        layerStatus.text = if (active) "ALT / الحروف الإضافية مفعّل${if (shift && !alt) " — يصل من الريموت باسم SHIFT" else ""}" else "ALT غير مفعّل"
        layerStatus.setTextColor(if (active) Ui.cyan else android.graphics.Color.WHITE)
    }
    override fun onPause() { modifiers.clear(); captured.clear(); super.onPause() }
}
