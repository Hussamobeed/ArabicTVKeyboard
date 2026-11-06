package com.herannet.arabictvkeyboard

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.Gravity
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class ArabicImeService : InputMethodService() {
    private lateinit var prefs: Prefs
    private var panel: LinearLayout? = null
    private var status: TextView? = null
    private var altArmed = false
    private var numbers = false
    private var numericEditor = false
    private var hasEditor = false
    private var sessionHidden = false
    private var navigateGuide = false
    private val consumed = mutableSetOf<Int>()
    private val labels = mutableMapOf<Char, TextView>()
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() { super.onCreate(); prefs = Prefs(this) }
    override fun onEvaluateFullscreenMode() = false
    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        // Also show for a connected hardware keyboard, unless the owner hides the guide.
        return hasEditor && prefs.showGuide && !sessionHidden
    }
    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean = onEvaluateInputViewShown()

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        handler.removeCallbacksAndMessages(null)
        hasEditor = attribute != null && attribute.inputType != InputType.TYPE_NULL
        numericEditor = when (attribute?.inputType?.and(InputType.TYPE_MASK_CLASS)) {
            InputType.TYPE_CLASS_NUMBER, InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_DATETIME -> true
            else -> false
        }
        altArmed = false; numbers = numericEditor; consumed.clear(); sessionHidden = false; navigateGuide = false
        renderState()
        // Vendor boxes often suppress the IME when a USB keyboard is connected.
        // Keep this view in the IME window, with no global overlay/accessibility permissions.
        if (hasEditor && prefs.showGuide) handler.post {
            if (hasEditor && !sessionHidden) try { showWindow(true) } catch (_: RuntimeException) { /* OEM refused the window. Hardware mapping still works. */ }
        }
    }
    override fun onFinishInput() {
        handler.removeCallbacksAndMessages(null)
        hasEditor = false; altArmed = false; consumed.clear()
        super.onFinishInput()
    }
    override fun onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy() }
    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        val guide = panel ?: return
        val decor = window.window?.decorView ?: return
        if (prefs.overlayGuide && isInputViewShown) {
            // Report no reserved editor area so the translucent guide sits over the app.
            outInsets.contentTopInsets = decor.height
            outInsets.visibleTopInsets = decor.height
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_REGION
            val location = IntArray(2)
            guide.getLocationInWindow(location)
            outInsets.touchableRegion.set(location[0], location[1], location[0] + guide.width, location[1] + guide.height)
        }
    }
    override fun onCreateInputView(): View {
        window.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val root = Ui.column(this).apply {
            setPadding(12, 6, 12, 6)
            background = Ui.background(Color.argb(prefs.panelOpacity * 255 / 100, 17, 27, 43))
        }
        panel = root; labels.clear()
        status = Ui.text(this, "", 14f).apply { gravity = Gravity.CENTER }
        root.addView(status)
        KeyboardLayout.rows.forEachIndexed { index, letters ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            letters.forEach { letter ->
                val key = Ui.button(this, "") { typeLetter(letter) }.apply {
                    textSize = 19f
                    contentDescription = "$letter ${KeyboardLayout.primary[letter]} ALT ${KeyboardLayout.secondary[letter].orEmpty()}"
                }
                labels[letter] = key
                row.addView(key, LinearLayout.LayoutParams(0, Ui.dp(this, 49), 1f).apply { setMargins(2, 2, 2, 2) })
            }
            if (index == 1) row.addView(Ui.button(this, "⌫") { delete() }, LinearLayout.LayoutParams(0, Ui.dp(this, 49), 1f))
            if (index == 2) row.addView(Ui.button(this, "↵") { enter() }, LinearLayout.LayoutParams(0, Ui.dp(this, 49), 2f))
            root.addView(row)
        }
        val controls = LinearLayout(this)
        fun control(title: String, weight: Float = 1f, action: () -> Unit) {
            controls.addView(Ui.button(this, title, action), LinearLayout.LayoutParams(0, Ui.dp(this, 43), weight).apply { setMargins(2, 3, 2, 0) })
        }
        control("ALT / F2") { altArmed = !altArmed; renderState() }
        control("العربية / EN") { toggleLanguage() }
        control("123 / F3") { numbers = numericEditor || !numbers; altArmed = false; renderState() }
        control("مسافة", 2f) { commit(" ") }
        control("،") { commit("،") }
        control(".") { commit(".") }
        control("؟") { commit("؟") }
        control("إخفاء") { sessionHidden = true; navigateGuide = false; requestHideSelf(0) }
        root.addView(controls)
        renderState()
        return root
    }
    private fun renderState() {
        status?.text = when {
            numbers -> "123 — الأرقام تمر كما هي • F3 للعودة"
            !prefs.arabic -> "English • CAPS: العربية • F2: ALT • Esc: إخفاء"
            else -> "العربية • CAPS: EN • ${if (altArmed) "ALT مفعّل" else "F2: ALT"} • F3: 123 • OK: تنقّل • Esc: إخفاء"
        }
        panel?.background = Ui.background(Color.argb(prefs.panelOpacity * 255 / 100, 17, 27, 43))
        labels.forEach { (letter, view) ->
            val secondary = KeyboardLayout.secondary[letter]
            view.text = when {
                numbers -> KeyboardLayout.numeric.getValue(letter)
                !prefs.arabic -> letter.toString()
                altArmed -> "$letter  ${secondary ?: "—"}"
                else -> "$letter  ${KeyboardLayout.primary[letter]}${if (secondary == null) "" else "  ·$secondary"}"
            }
        }
    }
    private fun commit(text: String) { currentInputConnection?.commitText(text, 1) }
    private fun typeLetter(letter: Char) {
        if (numbers) commit(KeyboardLayout.numeric.getValue(letter))
        else if (!prefs.arabic) commit(letter.toString().lowercase(Locale.ROOT))
        else commit(if (altArmed) KeyboardLayout.secondary[letter] ?: KeyboardLayout.primary.getValue(letter) else KeyboardLayout.primary.getValue(letter))
        altArmed = false; renderState()
    }
    private fun toggleLanguage() { prefs.arabic = !prefs.arabic; altArmed = false; renderState() }
    private fun delete() {
        val ic = currentInputConnection ?: return
        if (!ic.getSelectedText(0).isNullOrEmpty()) ic.commitText("", 1) else ic.deleteSurroundingText(1, 0)
    }
    private fun enter() { sendDefaultEditorAction(true).also { if (!it) sendKeyChar('\n') } }
    private fun handled(keyCode: Int): Boolean { consumed.add(keyCode); return true }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (!hasEditor || currentInputConnection == null) return super.onKeyDown(keyCode, event)
        if (event.isCtrlPressed || event.isMetaPressed) return super.onKeyDown(keyCode, event)
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && isInputViewShown) {
            if (event.repeatCount == 0) {
                if (navigateGuide) panel?.findFocus()?.performClick()
                else { navigateGuide = true; labels['Q']?.requestFocus() }
            }
            return handled(keyCode)
        }
        if (navigateGuide && isInputViewShown) {
            val direction = when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> View.FOCUS_UP
                KeyEvent.KEYCODE_DPAD_DOWN -> View.FOCUS_DOWN
                KeyEvent.KEYCODE_DPAD_LEFT -> View.FOCUS_LEFT
                KeyEvent.KEYCODE_DPAD_RIGHT -> View.FOCUS_RIGHT
                else -> null
            }
            if (direction != null) {
                panel?.findFocus()?.focusSearch(direction)?.requestFocus()
                return handled(keyCode)
            }
        }
        when (keyCode) {
            KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT, KeyEvent.KEYCODE_F2 -> {
                if (event.repeatCount == 0) { altArmed = !altArmed; renderState() }
                return handled(keyCode)
            }
            KeyEvent.KEYCODE_CAPS_LOCK, KeyEvent.KEYCODE_LANGUAGE_SWITCH -> {
                if (event.repeatCount == 0) toggleLanguage()
                return handled(keyCode)
            }
            KeyEvent.KEYCODE_F3 -> {
                if (event.repeatCount == 0) { numbers = numericEditor || !numbers; altArmed = false; renderState() }
                return handled(keyCode)
            }
            KeyEvent.KEYCODE_ESCAPE -> { sessionHidden = true; navigateGuide = false; requestHideSelf(0); return handled(keyCode) }
            KeyEvent.KEYCODE_DEL -> { delete(); return handled(keyCode) }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> { if (navigateGuide && isInputViewShown) panel?.findFocus()?.performClick() else enter(); return handled(keyCode) }
            KeyEvent.KEYCODE_SPACE -> { commit(" "); altArmed = false; renderState(); return handled(keyCode) }
        }
        // Leave arrows, HOME, BACK, volume, TAB and app shortcuts to Android/the editor.
        val unicode = event.unicodeChar
        if (unicode and KeyCharacterMap.COMBINING_ACCENT != 0) return super.onKeyDown(keyCode, event)
        val character = unicode.takeIf { it in 32..0xFFFF }?.toChar()
        val letter = if (keyCode in KeyEvent.KEYCODE_A..KeyEvent.KEYCODE_Z) ('A'.code + keyCode - KeyEvent.KEYCODE_A).toChar() else null
        if (!prefs.arabic || numericEditor || numbers) return super.onKeyDown(keyCode, event)
        val custom = prefs.mapped(keyCode, unicode)
        val text = custom ?: KeyboardLayout.resolve(letter, character, altArmed || event.isAltPressed, prefs.blueSymbols, false)
        if (text == null) return super.onKeyDown(keyCode, event)
        commit(text); altArmed = false; renderState()
        return handled(keyCode)
    }
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (consumed.remove(keyCode)) return true
        return super.onKeyUp(keyCode, event)
    }
}
