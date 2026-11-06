package com.herannet.arabictvkeyboard

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

object Ui {
    val cyan = Color.rgb(84, 221, 231)
    fun dp(context: Context, value: Int) = (value * context.resources.displayMetrics.density).toInt()
    fun background(color: Int, border: Int = Color.TRANSPARENT): GradientDrawable = GradientDrawable().apply {
        setColor(color); cornerRadius = 10f; setStroke(2, border)
    }
    fun text(context: Context, text: String, size: Float = 18f) = TextView(context).apply {
        this.text = text; textSize = size; setTextColor(Color.WHITE); setPadding(12, 8, 12, 8)
    }
    fun button(context: Context, title: String, action: () -> Unit) = Button(context).apply {
        text = title; textSize = 16f; isAllCaps = false
        setTextColor(ColorStateList(arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf()), intArrayOf(Color.BLACK, Color.WHITE)))
        typeface = Typeface.DEFAULT_BOLD
        background = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), background(cyan, Color.WHITE))
            addState(intArrayOf(android.R.attr.state_pressed), background(cyan, Color.WHITE))
            addState(intArrayOf(), background(Color.rgb(37, 53, 73), Color.rgb(67, 83, 102)))
        }
        setPadding(12, 4, 12, 4); minimumHeight = dp(context, 44)
        setOnClickListener { action() }
    }
    fun column(context: Context) = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL; setPadding(20, 16, 20, 16)
        layoutDirection = View.LAYOUT_DIRECTION_LTR
    }
}
