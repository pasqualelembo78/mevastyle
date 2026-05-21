package com.mevastyle.app.ui
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image; import androidx.compose.runtime.Composable; import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier; import androidx.compose.ui.graphics.asImageBitmap; import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

@Composable
fun TShirtImage(colorName: String, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val bmp = remember(colorName) { try { ctx.assets.open("tshirts/tshirt_$colorName.png").use { BitmapFactory.decodeStream(it) } }
        catch (_: Exception) { ctx.assets.open("tshirt_white.png").use { BitmapFactory.decodeStream(it) } } }
    Image(bitmap = bmp.asImageBitmap(), contentDescription = "T-shirt $colorName", modifier = modifier, contentScale = ContentScale.Fit)
}
