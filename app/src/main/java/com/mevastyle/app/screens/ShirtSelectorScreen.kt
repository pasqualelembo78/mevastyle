package com.mevastyle.app.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mevastyle.app.data.*
import com.mevastyle.app.ui.TShirtImage
import com.mevastyle.app.ui.theme.*

@Composable
fun ShirtSelectorScreen(sel: TShirtColor, onSel: (TShirtColor) -> Unit, onNext: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("< Indietro", color = Primary) }
        Text("Scegli la tua maglietta", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Seleziona il colore", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) { TShirtImage(sel.name, Modifier.height(200.dp)) }
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
            items(TSHIRT_COLORS) { c ->
                Column(Modifier.clip(RoundedCornerShape(8.dp)).border(2.dp, if (sel.name == c.name) Primary else Color.Transparent, RoundedCornerShape(8.dp)).background(Color.White).clickable { onSel(c) }.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(c.color).border(1.dp, if (c.hex == 0xFFFFFFFF) Color.LightGray else Color.Transparent, CircleShape))
                    Spacer(Modifier.height(6.dp)); Text(c.label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = Cta), modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Personalizza >", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
    }
}
