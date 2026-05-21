package com.mevastyle.app.data
import androidx.compose.ui.graphics.Color

data class ClothingType(val id: String = "", val name: String = "", val label: String = "",
    val sides: List<String> = listOf("front","back","sleeve_left","sleeve_right"), val order: Int = 0)
data class TShirtColor(val name: String, val hex: Long, val label: String, val clothingTypeId: String = "tshirt") {
    val color: Color get() = Color(hex)
}
val TSHIRT_COLORS = mutableListOf(
    TShirtColor("white",0xFFFFFFFF,"Bianco"), TShirtColor("black",0xFF1A1A1A,"Nero"),
    TShirtColor("red",0xFFDC2626,"Rosso"), TShirtColor("blue",0xFF2563EB,"Blu"),
    TShirtColor("green",0xFF16A34A,"Verde"), TShirtColor("yellow",0xFFEAB308,"Giallo"),
    TShirtColor("gray",0xFF6B7280,"Grigio"), TShirtColor("navy",0xFF1E3A5F,"Blu Navy"),
    TShirtColor("pink",0xFFEC4899,"Rosa"))
val DEFAULT_CLOTHING_TYPES = listOf(ClothingType("tshirt","tshirt","Maglietta",listOf("front","back","sleeve_left","sleeve_right"),0))
object PrintArea {
    const val WIDTH_CM=30; const val HEIGHT_CM=40; const val CANVAS_WIDTH=600f; const val CANVAS_HEIGHT=800f
    fun pixelsToCm(px:Float,totalPx:Float,totalCm:Int):Float=(px/totalPx*totalCm*10).toInt()/10f
}
