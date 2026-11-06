package com.herannet.arabictvkeyboard

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView

/** Focusable TV key with independent corner legends and a large centered glyph. */
class GuideKey(context: Context, private val scale: Float = 1f, action: () -> Unit) : FrameLayout(context) {
    private val latin = label((13f * scale).coerceAtLeast(10f), Color.WHITE, Gravity.TOP or Gravity.LEFT)
    private val main = label(30f, Color.WHITE, Gravity.CENTER)
    private val alternate = label((17f * scale).coerceAtLeast(12f), Ui.cyan, Gravity.TOP or Gravity.RIGHT)

    init {
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        isFocusable = true; isClickable = true
        setPadding(Ui.dp(context, (8 * scale).toInt()), Ui.dp(context, 2), Ui.dp(context, (8 * scale).toInt()), Ui.dp(context, 2))
        addView(main, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(latin, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.LEFT))
        addView(alternate, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.RIGHT))
        setOnClickListener { action() }
        paintBackground(false)
    }
    private fun label(size: Float, color: Int, alignment: Int) = TextView(context).apply {
        textSize = size; setTextColor(color); gravity = alignment; typeface = Typeface.DEFAULT_BOLD
        includeFontPadding = false; isFocusable = false; isClickable = false
    }
    private fun fill(active: Boolean, focused: Boolean): GradientDrawable = GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        if (active) intArrayOf(Color.argb(225, 17, 93, 110), Color.argb(220, 18, 57, 78))
        else if (focused) intArrayOf(Color.argb(240, 45, 90, 120), Color.argb(230, 32, 64, 93))
        else intArrayOf(Color.argb(115, 67, 75, 91), Color.argb(110, 32, 40, 56))
    ).apply {
        cornerRadius = Ui.dp(context, 7).toFloat()
        setStroke(Ui.dp(context, if (active || focused) 2 else 1),
            if (focused) Color.WHITE else if (active) Ui.cyan else Color.argb(155, 151, 162, 179))
    }
    private fun paintBackground(active: Boolean) {
        background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), fill(active, true))
            addState(intArrayOf(android.R.attr.state_pressed), fill(active, true))
            addState(intArrayOf(), fill(active, false))
        }
    }
    fun letter(key: Char, primary: String, secondary: String?, alt: Boolean, size: Float) {
        latin.text = key.toString()
        main.text = if (alt && secondary != null) secondary else primary
        main.textSize = size
        main.setTextColor(if (alt && secondary != null) Ui.cyan else Color.WHITE)
        alternate.text = if (alt && secondary != null) primary else secondary.orEmpty()
        alternate.setTextColor(if (alt) Color.argb(210, 208, 218, 233) else Ui.cyan)
        contentDescription = "$key $primary${if (secondary == null) "" else ", ALT $secondary"}"
        paintBackground(alt && secondary != null)
    }
    fun action(title: String, active: Boolean = false, size: Float = 18f) {
        latin.text = ""; alternate.text = ""
        main.text = title; main.textSize = (size * scale).coerceAtLeast(10f)
        main.setTextColor(if (active) Ui.cyan else Color.WHITE)
        contentDescription = title
        paintBackground(active)
    }
}
