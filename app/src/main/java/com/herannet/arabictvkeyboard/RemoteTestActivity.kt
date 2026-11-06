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
    private var pending: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); prefs = Prefs(this)
        val root = Ui.column(this); setContentView(root)
        root.addView(Ui.text(this, "اختبار الريموت / Remote test", 25f))
        root.addView(Ui.text(this, "اضغط ALT ثم D أو Q. سترى ما يرسله الريموت.\nلتخصيص زر، اختر الحرف المطلوب ثم اضغط الزر أو ALT مع الزر.\nلا تعيّن زرًا عاديًا للأرقام إلا إذا كنت تقصد تحويله لحرف. F3 يعيد كتابة الأرقام.", 17f))
        report = Ui.text(this, "لم يُضغط زر بعد", 19f)
        root.addView(report)
        root.addView(Ui.button(this, "تعيين زر إلى حرف عربي") {
            val letters = (KeyboardLayout.primary.values + KeyboardLayout.secondary.values).distinct()
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
        if (event.action == KeyEvent.ACTION_DOWN) {
            val code = event.keyCode
            if (code == KeyEvent.KEYCODE_BACK && pending != null) { pending = null; report.text = "أُلغي التعيين"; return true }
            report.text = "${KeyEvent.keyCodeToString(code)}\nkeyCode=$code  scanCode=${event.scanCode}\nunicode=${event.unicodeChar}  text=${event.unicodeChar.takeIf { it in 32..65535 }?.toChar() ?: '—'}\nALT=${event.isAltPressed}  SHIFT=${event.isShiftPressed}  repeat=${event.repeatCount}"
            val modifiers = setOf(KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT, KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT, KeyEvent.KEYCODE_CAPS_LOCK)
            if (pending != null && code !in modifiers && event.unicodeChar >= 32) {
                prefs.map(code, event.unicodeChar, pending!!)
                report.append("\nحُفظ التعيين إلى ${pending}"); pending = null; return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
