package org.apps.flexmed.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class FlexMedLogo @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    override fun onDraw(canvas: Canvas) {
        val text = text.toString()
        val width = paint.measureText(text)

        val gradient = LinearGradient(
            0f, 0f, width, 0f,
            intArrayOf(
                0xFFB0E5FF.toInt(), // Light blue
                0xFFB9FFF2.toInt(), // Light greenish
                0xFF1B9FFF.toInt()  // Blue
            ),
            null,
            Shader.TileMode.CLAMP
        )

        paint.shader = gradient

        super.onDraw(canvas)
    }
}