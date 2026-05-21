package com.mevastyle.app.ui.theme
import androidx.compose.material3.*; import androidx.compose.runtime.Composable; import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF2563EB); val Cta = Color(0xFF16A34A)
val TextPrimary = Color(0xFF1A1A2E); val TextSecondary = Color(0xFF6B7280)

@Composable fun MevaStyleTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Primary, onPrimary = Color.White,
        background = Color(0xFFF8FAFC), surface = Color.White), content = content)
}
