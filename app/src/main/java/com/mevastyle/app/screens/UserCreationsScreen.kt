package com.mevastyle.app.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mevastyle.app.data.*
import com.mevastyle.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCreationsScreen(onBack: () -> Unit) {
    var creations by remember { mutableStateOf<List<UserCreation>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var selected by remember { mutableStateOf<UserCreation?>(null) }
    var selSide by remember { mutableStateOf("front") }

    LaunchedEffect(Unit) { creations = FirebaseManager.getUserCreations(); loading = false }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("< Indietro", color = Primary) }
        Text("Le mie creazioni", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Salvate su cloud", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(16.dp))

        if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
        else if (creations.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CloudOff, null, Modifier.size(48.dp), tint = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text("Nessuna creazione salvata", fontSize = 16.sp, color = TextSecondary)
                Text("Le creazioni vengono salvate quando confermi l'ordine", fontSize = 12.sp, color = TextSecondary)
            }
        }
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(creations, key = { it.id }) { c ->
                val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                Card(Modifier.fillMaxWidth().clickable { selected = c; selSide = "front" }, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (c.mockupUrl.isNotBlank()) AsyncImage(c.mockupUrl, "Mockup", Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        else Box(Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray), contentAlignment = Alignment.Center) { Icon(Icons.Default.Image, null, tint = Color.White) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${c.colorLabel} - ${c.clothingType}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(df.format(Date(c.createdAt)), fontSize = 12.sp, color = TextSecondary)
                            Text("${c.sides.size} lati salvati", fontSize = 11.sp, color = TextSecondary)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = TextSecondary)
                    }
                }
            }
        }
    }

    selected?.let { c ->
        AlertDialog(onDismissRequest = { selected = null }, title = { Text("${c.colorLabel}") },
            text = {
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        c.sides.keys.forEach { s ->
                            FilterChip(selSide == s, { selSide = s }, label = {
                                Text(when (s) { "front" -> "Fronte"; "back" -> "Retro"; "sleeve_left" -> "Man SX"; "sleeve_right" -> "Man DX"; else -> s }, fontSize = 11.sp)
                            })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    val url = c.sides[selSide] ?: c.mockupUrl
                    if (url.isNotBlank()) AsyncImage(url, selSide, Modifier.fillMaxWidth().aspectRatio(0.75f).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit)
                }
            },
            confirmButton = { TextButton(onClick = { selected = null }) { Text("Chiudi") } })
    }
}
