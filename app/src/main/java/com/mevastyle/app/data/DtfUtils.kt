package com.mevastyle.app.data
import android.graphics.ColorMatrix
object DtfUtils {
    fun getDtfColorMatrix(): ColorMatrix = ColorMatrix(floatArrayOf(
        0.85f,0.05f,0.05f,0f,0f, 0.05f,0.85f,0.05f,0f,0f, 0.05f,0.05f,0.85f,0f,0f, 0f,0f,0f,1f,0f))
}
