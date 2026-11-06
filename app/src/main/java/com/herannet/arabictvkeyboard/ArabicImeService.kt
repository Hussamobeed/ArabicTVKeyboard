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
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class ArabicImeService : InputMethodService() {
    private lateinit var prefs: Prefs
    private var panel: LinearLayout? = null
    private var status: TextView? = null
    private var legend: TextView? = null
    private var altKey: GuideKey? = null
    private var numbersKey: GuideKey? = null
    private var physicalAltVisible = false
    private var keyGlyphSize = 22f
    private var builtGuideSize = 0
    private var altArmed = false
    private var numbers = false
    private var numericEditor = false
    private var hasEditor = false
    private var sessionHidden = false
    private var navigateGuide = false
    private val consumed = mutableSetOf<String>()
    private val rawModifiers = RawModifiers()
    private val labels = mutableMapOf<Char, GuideKey>()
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
        altArmed = false; numbers = numericEditor || prefs.remoteNumbers; consumed.clear(); rawModifiers.clear(); physicalAltVisible = false; sessionHidden = false; navigateGuide = false
        if (panel != null && builtGuideSize != prefs.guideSizePercent) setInputView(onCreateInputView())
        renderState()
        // Vendor boxes often suppress the IME when a USB keyboard is connected.
        // Keep this view in the IME window, with no global overlay/accessibility permissions.
        if (hasEditor && prefs.showGuide) handler.post {
            if (hasEditor && !sessionHidden) try { showWindow(true) } catch (_: RuntimeException) { /* OEM refused the window. Hardware mapping still works. */ }
        }
    }
    override fun onFinishInput() {
        handler.removeCallbacksAndMessages(null)
        hasEditor = false; altArmed = false; consumed.clear(); rawModifiers.clear(); physicalAltVisible = false
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
    private fun altVisible() = prefs.arabic && !numbers && (prefs.remoteAltLocked || altArmed || physicalAltVisible)
    override fun onCreateInputView(): View {
        window.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        builtGuideSize = prefs.guideSizePercent
        val scale = builtGuideSize / 100f
        val baseRowHeight = (resources.configuration.screenHeightDp * 0.115f).toInt().coerceIn(42, 66)
        val rowHeight = (baseRowHeight * scale).toInt().coerceAtLeast(27)
        val headerHeight = (32 * scale).toInt().coerceAtLeast(20)
        keyGlyphSize = ((if (baseRowHeight < 50) 25f else 31f) * scale).coerceAtLeast(18f)
        val root = Ui.column(this).apply { setPadding(Ui.dp(this@ArabicImeService, 7), 4, Ui.dp(this@ArabicImeService, 7), 4) }
        panel = root; labels.clear()
        val header = FrameLayout(this)
        val title = Ui.text(this, "دليل الحروف", 22f * scale).apply { gravity = Gravity.CENTER; setPadding(0, 0, 0, 0) }
        header.addView(title, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, headerHeight)))
        legend = Ui.text(this, "الأزرق مع ALT", (15f * scale).coerceAtLeast(11f)).apply { setTextColor(Ui.cyan); gravity = Gravity.CENTER; setPadding(4, 0, 4, 0) }
        header.addView(legend, FrameLayout.LayoutParams(Ui.dp(this, (165 * scale).toInt()), Ui.dp(this, headerHeight), Gravity.LEFT))
        numbersKey = GuideKey(this, scale) { toggleNumbers() }
        header.addView(numbersKey, FrameLayout.LayoutParams(Ui.dp(this, (76 * scale).toInt().coerceAtLeast(48)), Ui.dp(this, headerHeight), Gravity.RIGHT))
        root.addView(header)
        fun addKey(row: LinearLayout, key: GuideKey, weight: Float = 1f, height: Int = rowHeight) {
            row.addView(key, LinearLayout.LayoutParams(0, Ui.dp(this, height), weight).apply {
                setMargins(Ui.dp(this@ArabicImeService, 2), Ui.dp(this@ArabicImeService, 2), Ui.dp(this@ArabicImeService, 2), Ui.dp(this@ArabicImeService, 2))
            })
        }
        KeyboardLayout.rows.forEachIndexed { index, letters ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            letters.forEach { letter ->
                val key = GuideKey(this, scale) { typeLetter(letter) }
                labels[letter] = key
                addKey(row, key)
            }
            if (index == 1) addKey(row, GuideKey(this, scale) { delete() }.apply { action("⌫\nBACKSPACE", size = 13f) }, 1.35f)
            if (index == 2) {
                addKey(row, GuideKey(this, scale) {
                    commit(if (numbers) "." else if (altVisible()) "،" else "."); altArmed = false; renderState()
                }.apply {
                    action(".  ،", size = 25f)
                    contentDescription = "نقطة، مع ALT فاصلة عربية، ضغط مطول علامة استفهام"
                    setOnLongClickListener { commit(if (numbers) ":" else "؟"); true }
                })
                addKey(row, GuideKey(this, scale) { enter() }.apply { action("↵\nENTER", size = 17f) }, 1.6f)
            }
            root.addView(row)
        }
        val controls = LinearLayout(this)
        altKey = GuideKey(this, scale) { if (!prefs.remoteAltLocked) altArmed = !altArmed; renderState() }
        addKey(controls, altKey!!, 1.5f, rowHeight - 9)
        addKey(controls, GuideKey(this, scale) { commit(" ") }.apply { action("SPACE", size = 20f) }, 4f, rowHeight - 9)
        addKey(controls, GuideKey(this, scale) { toggleLanguage() }.apply { action("العربية / EN", size = 19f) }, 1.9f, rowHeight - 9)
        addKey(controls, GuideKey(this, scale) { sessionHidden = true; navigateGuide = false; requestHideSelf(0) }.apply { action("إخفاء", size = 20f) }, 1.5f, rowHeight - 9)
        root.addView(controls)
        status = Ui.text(this, "", 10f).apply { gravity = Gravity.CENTER; setPadding(0, 2, 0, 0) }
        root.addView(status)
        renderState()
        return FrameLayout(this).apply {
            val width = (resources.configuration.screenWidthDp * (0.55f + 0.45f * scale)).toInt()
            addView(root, FrameLayout.LayoutParams(Ui.dp(this@ArabicImeService, width), FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL))
        }
    }
    private fun renderState() {
        val active = altVisible()
        legend?.text = when {
            numbers && prefs.remoteAltLocked -> "123 · ALT مثبت"
            active && prefs.remoteAltLocked -> "ALT مثبت"
            active -> "● ALT مفعّل"
            else -> "الأزرق مع ALT"
        }
        legend?.setTextColor(if (active) Color.WHITE else Ui.cyan)
        legend?.background = if (active) Ui.background(Color.rgb(15, 107, 121), Ui.cyan) else null
        altKey?.action(if (prefs.remoteAltLocked) "ALT ثابت" else if (active) "ALT  ●" else "ALT", active, 22f)
        numbersKey?.action(if (numbers) "ABC" else "123", numbers, 14f)
        status?.text = when {
            numbers -> "123 — Q…P: 1…0 • M: : • زر النقطة: . • زر 163 للعودة"
            !prefs.arabic -> "English • CAPS: العربية • OK: تنقّل • Esc: إخفاء"
            active && prefs.remoteAltLocked -> "ALT مثبت — زر 165 للإلغاء • زر 163 للأرقام"
            active -> "الحروف السماوية مفعّلة • اختر الحرف أو اضغط ALT للإلغاء"
            else -> "CAPS: EN • ALT / F2: الحروف السماوية • F3: 123 • OK: تنقّل • Esc: إخفاء"
        }
        panel?.background = Ui.background(Color.argb(prefs.panelOpacity * 255 / 100, 17, 24, 37),
            if (active) Ui.cyan else Color.argb(110, 111, 126, 148))
        labels.forEach { (letter, key) ->
            when {
                numbers -> key.letter(letter, KeyboardLayout.numeric.getValue(letter), null, false, keyGlyphSize)
                !prefs.arabic -> key.letter(letter, letter.toString(), null, false, keyGlyphSize)
                else -> key.letter(letter, KeyboardLayout.primary.getValue(letter), KeyboardLayout.secondary[letter], active, keyGlyphSize)
            }
        }
    }
    private fun commit(text: String) { currentInputConnection?.commitText(text, 1) }
    private fun typeLetter(letter: Char) {
        if (numbers) commit(KeyboardLayout.numeric.getValue(letter))
        else if (!prefs.arabic) commit(letter.toString().lowercase(Locale.ROOT))
        else commit(if (altVisible()) KeyboardLayout.secondary[letter] ?: KeyboardLayout.primary.getValue(letter) else KeyboardLayout.primary.getValue(letter))
        altArmed = false; renderState()
    }
    private fun toggleLanguage() { prefs.arabic = !prefs.arabic; altArmed = false; renderState() }
    private fun toggleNumbers() {
        prefs.remoteNumbers = !prefs.remoteNumbers
        numbers = numericEditor || prefs.remoteNumbers
        altArmed = false; renderState()
    }
    private fun remoteShortcut(event: KeyEvent): Boolean {
        val action = RemoteModeShortcuts.binding(event.device?.name, event.keyCode, event.scanCode) ?: return false
        if (event.repeatCount == 0) {
            val state = RemoteModeShortcuts.State(prefs.remoteAltLocked, prefs.remoteNumbers)
            val next = RemoteModeShortcuts.apply(state, action)
            prefs.remoteAltLocked = next.altLocked
            prefs.remoteNumbers = next.numbers
            if (action == RemoteModeShortcuts.Action.LOCK_ALT && next.altLocked) prefs.arabic = true
            numbers = numericEditor || next.numbers
            altArmed = false; renderState()
        }
        return handled(event)
    }
    private fun delete() {
        val ic = currentInputConnection ?: return
        if (!ic.getSelectedText(0).isNullOrEmpty()) ic.commitText("", 1) else ic.deleteSurroundingText(1, 0)
    }
    private fun enter() { sendDefaultEditorAction(true).also { if (!it) sendKeyChar('\n') } }
    private fun handled(event: KeyEvent): Boolean { consumed.add(RemoteKeys.pressedIdentity(event)); return true }

    override fun onKeyDown(rawKeyCode: Int, event: KeyEvent): Boolean {
        val keyCode = RemoteKeys.actionKeyCode(event)
        if (!hasEditor || currentInputConnection == null) return super.onKeyDown(rawKeyCode, event)
        rawModifiers.update(event.deviceId, event.scanCode, true, event.repeatCount)
        val shift = event.isShiftPressed || rawModifiers.active(event.deviceId, UsbScanCodes.Action.SHIFT)
        val physicalAlt = event.isAltPressed || rawModifiers.active(event.deviceId, UsbScanCodes.Action.ALT)
        physicalAltVisible = RemoteLayer.active(physicalAlt, shift, prefs.shiftIsAlt)
        if (event.isCtrlPressed || event.isMetaPressed || rawModifiers.active(event.deviceId, UsbScanCodes.Action.CTRL) || rawModifiers.active(event.deviceId, UsbScanCodes.Action.META)) return super.onKeyDown(rawKeyCode, event)
        if (remoteShortcut(event)) return true
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && isInputViewShown) {
            if (event.repeatCount == 0) {
                if (navigateGuide) panel?.findFocus()?.performClick()
                else { navigateGuide = true; labels['Q']?.requestFocus() }
            }
            return handled(event)
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
                return handled(event)
            }
        }
        when (keyCode) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                if (prefs.arabic && !numbers && prefs.shiftIsAlt) { renderState(); return handled(event) }
            }
            KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT, KeyEvent.KEYCODE_F2 -> {
                if (event.repeatCount == 0) { altArmed = !altArmed; renderState() }
                return handled(event)
            }
            KeyEvent.KEYCODE_CAPS_LOCK, KeyEvent.KEYCODE_LANGUAGE_SWITCH -> {
                if (event.repeatCount == 0) toggleLanguage()
                return handled(event)
            }
            KeyEvent.KEYCODE_F3 -> {
                if (event.repeatCount == 0) { toggleNumbers() }
                return handled(event)
            }
            KeyEvent.KEYCODE_ESCAPE -> { sessionHidden = true; navigateGuide = false; requestHideSelf(0); return handled(event) }
            KeyEvent.KEYCODE_DEL -> { delete(); return handled(event) }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> { if (navigateGuide && isInputViewShown) panel?.findFocus()?.performClick() else enter(); return handled(event) }
            KeyEvent.KEYCODE_SPACE -> { commit(" "); altArmed = false; renderState(); return handled(event) }
        }
        // Leave arrows, HOME, BACK, volume, TAB and app shortcuts to Android/the editor.
        val unicode = event.unicodeChar
        if (unicode and KeyCharacterMap.COMBINING_ACCENT != 0) return super.onKeyDown(rawKeyCode, event)
        val character = RemoteKeys.character(event, shift, rawModifiers.caps(event.deviceId))
        val letter = RemoteKeys.letter(event)
        val requestedLayer = altArmed || prefs.remoteAltLocked
        val custom = RemoteLayer.calibrated(
            prefs.mapped(RemoteKeys.identity(event, shift, physicalAlt), event.keyCode, unicode),
            prefs.mapped(RemoteKeys.identity(event, true, false), 0, 0),
            prefs.mapped(RemoteKeys.identity(event, false, true), 0, 0),
            requestedLayer, RemoteLayer.active(physicalAlt, shift, prefs.shiftIsAlt))
        val numericScan = when (event.keyCode) {
            KeyEvent.KEYCODE_PERIOD, KeyEvent.KEYCODE_NUMPAD_DOT -> 52
            KeyEvent.KEYCODE_SEMICOLON -> 39
            else -> event.scanCode
        }
        val text = if (numbers) NumberMode.resolve(letter, character, numericScan)
            else RemoteLayer.resolve(letter, character, requestedLayer, physicalAlt, shift,
            prefs.shiftIsAlt, prefs.blueSymbols, prefs.arabic, numericEditor || numbers, custom)
        if (text == null) return super.onKeyDown(rawKeyCode, event)
        commit(text); altArmed = false; renderState()
        return handled(event)
    }
    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        rawModifiers.update(event.deviceId, event.scanCode, false)
        val released = RemoteKeys.actionKeyCode(event)
        val physicalAlt = rawModifiers.active(event.deviceId, UsbScanCodes.Action.ALT) ||
            (released !in setOf(KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT) && event.isAltPressed)
        val shift = rawModifiers.active(event.deviceId, UsbScanCodes.Action.SHIFT) ||
            (released !in setOf(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT) && event.isShiftPressed)
        physicalAltVisible = RemoteLayer.active(physicalAlt, shift, prefs.shiftIsAlt)
        renderState()
        if (consumed.remove(RemoteKeys.pressedIdentity(event))) return true
        return super.onKeyUp(keyCode, event)
    }
}
