package com.mevastyle.app.editor
import android.graphics.Bitmap
import com.mevastyle.app.data.TShirtSide

sealed class CanvasElement {
    abstract var x: Float; abstract var y: Float; abstract var scale: Float; abstract var rotation: Float
    abstract val id: String; abstract var side: TShirtSide

    data class ImageElement(override var x: Float = 50f, override var y: Float = 50f,
        override var scale: Float = 1f, override var rotation: Float = 0f,
        override val id: String = "img_" + System.currentTimeMillis(),
        override var side: TShirtSide = TShirtSide.FRONT,
        var bitmap: Bitmap? = null, var hasTransparency: Boolean = false) : CanvasElement()

    data class TextElement(override var x: Float = 50f, override var y: Float = 50f,
        override var scale: Float = 1f, override var rotation: Float = 0f,
        override val id: String = "txt_" + System.currentTimeMillis(),
        override var side: TShirtSide = TShirtSide.FRONT,
        var text: String = "", var fontFamily: String = "sans-serif",
        var color: Int = 0xFF000000.toInt(), var fontSize: Float = 48f) : CanvasElement()

    fun getWidth(): Float = when (this) {
        is ImageElement -> (bitmap?.width?.toFloat() ?: 100f) * scale
        is TextElement -> text.length * fontSize * 0.55f * scale
    }
    fun getHeight(): Float = when (this) {
        is ImageElement -> (bitmap?.height?.toFloat() ?: 100f) * scale
        is TextElement -> fontSize * 1.4f * scale
    }
    fun containsRotated(px: Float, py: Float): Boolean {
        val cx = x + getWidth()/2; val cy = y + getHeight()/2
        val rad = Math.toRadians(-rotation.toDouble()); val cos = Math.cos(rad).toFloat(); val sin = Math.sin(rad).toFloat()
        val dx = px - cx; val dy = py - cy; val rx = dx*cos - dy*sin + cx; val ry = dx*sin + dy*cos + cy
        return rx in x..(x+getWidth()) && ry in y..(y+getHeight())
    }
}
