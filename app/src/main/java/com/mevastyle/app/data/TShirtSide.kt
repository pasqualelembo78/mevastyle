package com.mevastyle.app.data

enum class TShirtSide(val label: String, val fileSuffix: String, val angle: Float) {
    FRONT("Fronte", "front", 0f),
    SLEEVE_RIGHT("Manica DX", "sleeve_right", 90f),
    BACK("Retro", "back", 180f),
    SLEEVE_LEFT("Manica SX", "sleeve_left", 270f);
    companion object {
        val all = values().toList()
        fun fromAngle(angle: Float): TShirtSide {
            val n = ((angle % 360f) + 360f) % 360f
            return when {
                n < 45f || n >= 315f -> FRONT
                n < 135f -> SLEEVE_RIGHT
                n < 225f -> BACK
                else -> SLEEVE_LEFT
            }
        }
    }
}
object SidePrintArea {
    fun getArea(side: TShirtSide): Pair<Float, Float> = when (side) {
        TShirtSide.FRONT, TShirtSide.BACK -> 600f to 800f
        TShirtSide.SLEEVE_LEFT, TShirtSide.SLEEVE_RIGHT -> 300f to 400f
    }
    fun getCmArea(side: TShirtSide): Pair<Int, Int> = when (side) {
        TShirtSide.FRONT, TShirtSide.BACK -> 30 to 40
        TShirtSide.SLEEVE_LEFT, TShirtSide.SLEEVE_RIGHT -> 15 to 20
    }
}
