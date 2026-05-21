package com.mevastyle.app.screens
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mevastyle.app.data.*
import com.mevastyle.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current; val scope = rememberCoroutineScope()
    var types by remember { mutableStateOf<List<ClothingType>>(emptyList()) }
    var colors by remember { mutableStateOf<List<TShirtColor>>(emptyList()) }
    var selType by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showAddColor by remember { mutableStateOf(false) }
    var showAddType by remember { mutableStateOf(false) }
    var ncName by remember { mutableStateOf("") }; var ncLabel by remember { mutableStateOf("") }; var ncHex by remember { mutableStateOf("FF000000") }
    var ntName by remember { mutableStateOf("") }; var ntLabel by remember { mutableStateOf("") }; var ntSides by remember { mutableStateOf("front,back,sleeve_left,sleeve_right") }

    LaunchedEffect(Unit) {
        types = FirebaseManager.getClothingTypes().ifEmpty { DEFAULT_CLOTHING_TYPES }
        if (types.isNotEmpty()) { selType = types.first().id; colors = FirebaseManager.getColors(types.first().id).ifEmpty { TSHIRT_COLORS } }
        loading = false
    }
    fun refresh() { scope.launch { selType?.let { colors = FirebaseManager.getColors(it).ifEmpty { TSHIRT_COLORS } } } }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("< Indietro", color = Primary) }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.AdminPanelSettings, null, tint = Primary, modifier = Modifier.size(24.dp))
        }
        Text("Admin Panel", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Gestisci abbigliamento e colori", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(16.dp))

        if (loading) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
        else {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Tipi Abbigliamento", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                IconButton(onClick = { showAddType = true }) { Icon(Icons.Default.Add, "Aggiungi", tint = Primary) }
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { t -> FilterChip(selType == t.id, { selType = t.id; refresh() }, label = { Text(t.label) }) }
            }
            Spacer(Modifier.height(16.dp)); Divider(color = Color(0xFF333333)); Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Colori", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                IconButton(onClick = { showAddColor = true }) { Icon(Icons.Default.Add, "Aggiungi", tint = Primary) }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(colors, key = { it.name }) { c ->
                    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(CircleShape).background(c.color).border(1.dp, if (c.hex == 0xFFFFFFFF) Color.LightGray else Color.Transparent, CircleShape))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) { Text(c.label, fontWeight = FontWeight.SemiBold, color = TextPrimary); Text("${c.name} | #${String.format("%08X", c.hex)}", fontSize = 12.sp, color = TextSecondary) }
                            IconButton(onClick = { scope.launch { if (FirebaseManager.removeColor(selType ?: "tshirt", c.name)) { Toast.makeText(ctx, "Rimosso!", Toast.LENGTH_SHORT).show(); refresh() } } }) {
                                Icon(Icons.Default.Delete, "Rimuovi", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddColor) AlertDialog(onDismissRequest = { showAddColor = false }, title = { Text("Nuovo Colore") },
        text = { Column {
            OutlinedTextField(ncName, { ncName = it.lowercase().replace(" ", "_") }, label = { Text("Nome (es: sky_blue)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp)); OutlinedTextField(ncLabel, { ncLabel = it }, label = { Text("Etichetta (es: Azzurro)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp)); OutlinedTextField(ncHex, { ncHex = it }, label = { Text("Hex con alpha (es: FF87CEEB)") }, modifier = Modifier.fillMaxWidth())
            val ph = try { ncHex.toLong(16) } catch (_: Exception) { 0xFF000000L }
            Spacer(Modifier.height(8.dp)); Row(verticalAlignment = Alignment.CenterVertically) { Text("Anteprima: ", fontSize = 12.sp); Box(Modifier.size(32.dp).clip(CircleShape).background(Color(ph)).border(1.dp, Color.Gray, CircleShape)) }
        }},
        confirmButton = { TextButton(onClick = { if (ncName.isNotBlank() && ncLabel.isNotBlank()) {
            val h = try { ncHex.toLong(16) } catch (_: Exception) { 0xFF000000L }
            scope.launch { if (FirebaseManager.addColor(selType ?: "tshirt", TShirtColor(ncName, h, ncLabel))) { Toast.makeText(ctx, "Aggiunto!", Toast.LENGTH_SHORT).show(); refresh(); ncName = ""; ncLabel = "" } }
            showAddColor = false } }) { Text("Aggiungi") } },
        dismissButton = { TextButton(onClick = { showAddColor = false }) { Text("Annulla") } })

    if (showAddType) AlertDialog(onDismissRequest = { showAddType = false }, title = { Text("Nuovo Tipo Abbigliamento") },
        text = { Column {
            OutlinedTextField(ntName, { ntName = it.lowercase().replace(" ", "_") }, label = { Text("ID (es: hoodie)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp)); OutlinedTextField(ntLabel, { ntLabel = it }, label = { Text("Etichetta (es: Felpa)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp)); OutlinedTextField(ntSides, { ntSides = it }, label = { Text("Lati (separati da virgola)") }, modifier = Modifier.fillMaxWidth())
            Text("Es: front,back,sleeve_left,sleeve_right", fontSize = 11.sp, color = TextSecondary)
        }},
        confirmButton = { TextButton(onClick = { if (ntName.isNotBlank() && ntLabel.isNotBlank()) {
            scope.launch { if (FirebaseManager.addClothingType(ClothingType(ntName, ntName, ntLabel, ntSides.split(",").map { it.trim() }, types.size))) {
                Toast.makeText(ctx, "Aggiunto!", Toast.LENGTH_SHORT).show(); types = FirebaseManager.getClothingTypes().ifEmpty { DEFAULT_CLOTHING_TYPES } } }
            showAddType = false } }) { Text("Aggiungi") } },
        dismissButton = { TextButton(onClick = { showAddType = false }) { Text("Annulla") } })
}
