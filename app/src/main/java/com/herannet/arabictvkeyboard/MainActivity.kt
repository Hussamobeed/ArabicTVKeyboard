package com.herannet.arabictvkeyboard

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        val content = Ui.column(this)
        val scroll = ScrollView(this).apply { addView(content) }
        setContentView(scroll)
        content.addView(Ui.text(this, "Arabic TV Keyboard", 28f))
        content.addView(Ui.text(this, "لوحة عربية للريموت الإنجليزي • Android TV Box", 20f))
        content.addView(Ui.text(this, "١. فعّل اللوحة، ثم اخترها.\n٢. اكتب بأزرار الريموت. CAPS يبدّل اللغة، F2 للحروف الإضافية، F3 للأرقام.\n٣. إذا كان ALT يرسل الرموز الزرقاء، جرّب وضع الرموز أو عيّن الأزرار يدويًا.", 17f))
        content.addView(Ui.button(this, "١ — تفعيل لوحة الإدخال / Enable keyboard") {
            try { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
            catch (_: ActivityNotFoundException) { startActivity(Intent(Settings.ACTION_SETTINGS)) }
        })
        content.addView(Ui.button(this, "٢ — اختيار اللوحة / Select keyboard") {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        })
        content.addView(Ui.button(this, "اختبار الريموت وتعيين الأزرار") { startActivity(Intent(this, RemoteTestActivity::class.java)) })
        fun check(title: String, value: Boolean, change: (Boolean) -> Unit) {
            content.addView(CheckBox(this).apply {
                text = title; textSize = 17f; isChecked = value
                setOnCheckedChangeListener { _, checked -> change(checked) }
            })
        }
        check("إظهار دليل الحروف عند الكتابة", prefs.showGuide) { prefs.showGuide = it }
        check("تحويل الرموز الزرقاء حسب الريموت في الصورة", prefs.blueSymbols) { prefs.blueSymbols = it }
        content.addView(Ui.text(this, "وضع الرموز يحوّل 1 إلى ء و2 إلى ؤ… استخدم F3 أو زر 123 لكتابة الأرقام. لا تحتاجه إذا كان ALT يصل كزر مستقل.", 15f))
        content.addView(Ui.text(this, "عتامة خلفية الدليل / Guide opacity"))
        content.addView(SeekBar(this).apply {
            max = 65; progress = prefs.panelOpacity - 35
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, value: Int, user: Boolean) { if (user) prefs.panelOpacity = value + 35 }
                override fun onStartTrackingTouch(bar: SeekBar?) {}
                override fun onStopTrackingTouch(bar: SeekBar?) {}
            })
        })
        content.addView(Ui.text(this, "جرّب الكتابة هنا / Test typing"))
        content.addView(EditText(this).apply {
            hint = "D = د     K = ك"; textSize = 23f; minLines = 2
            setSingleLine(false)
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 100)))
        content.addView(Ui.text(this, "بدون إنترنت أو إعلانات. لا نحفظ النصوص المكتوبة. تعيينات الأزرار تبقى على جهازك.\nتعمل في صناديق الكتابة التي تدعم لوحة إدخال Android. بعض تطبيقات التلفزيون تفرض لوحة خاصة بها.", 15f))
        content.addView(Ui.button(this, "عرض حالة اللوحة") {
            val selected = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD).orEmpty().contains(packageName)
            Toast.makeText(this, if (selected) "اللوحة محددة الآن" else "اختر Arabic TV Keyboard من قائمة لوحات الإدخال", Toast.LENGTH_LONG).show()
        })
    }
}
