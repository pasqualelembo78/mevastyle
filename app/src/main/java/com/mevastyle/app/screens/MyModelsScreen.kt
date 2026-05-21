package com.mevastyle.app.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mevastyle.app.data.Garment
import com.mevastyle.app.data.GarmentManager
import com.mevastyle.app.ui.theme.*

@Composable
fun MyModelsScreen(onBack: () -> Unit, onOpenViewer: (Garment) -> Unit) {
    val context = LocalContext.current
    var garments by remember { mutableStateOf(GarmentManager.getAll()) }
    val userGarments = garments.filter { it.isUserUploaded }
    val defaultGarments = garments.filter { !it.isUserUploaded }

    fun refresh() { garments = GarmentManager.getAll() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("< Indietro", color = Primary) }
        Text("I miei modelli", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("${userGarments.size} caricati dall'utente, ${defaultGarments.size} predefiniti", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(16.dp))

        if (garments.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ViewInAr, null, Modifier.size(64.dp), tint = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    Text("Nessun modello", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Text("Carica un file .glb o immagini dalla schermata precedente", fontSize = 13.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                // User uploaded models first
                if (userGarments.isNotEmpty()) {
                    item {
                        Text("Caricati da te", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Primary)
                        Spacer(Modifier.height(4.dp))
                    }
                    items(userGarments, key = { it.id }) { garment ->
                        GarmentCard(garment = garment,
                            onOpen = { onOpenViewer(garment) },
                            onDelete = {
                                GarmentManager.removeGarment(context, garment.id)
                                Toast.makeText(context, "Eliminato!", Toast.LENGTH_SHORT).show()
                                refresh()
                            }
                        )
                    }
                }
                // Default models
                if (defaultGarments.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        Text("Predefiniti", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                    }
                    items(defaultGarments, key = { it.id }) { garment ->
                        GarmentCard(garment = garment, onOpen = { onOpenViewer(garment) }, onDelete = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun GarmentCard(garment: Garment, onOpen: () -> Unit, onDelete: (() -> Unit)?) {
    Card(Modifier.fillMaxWidth().clickable { onOpen() }, shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Icon based on mode
            Icon(
                if (garment.mode == "model3d") Icons.Default.ViewInAr else Icons.Default.Image,
                null, Modifier.size(40.dp),
                tint = if (garment.mode == "model3d") Primary else Color(0xFF16A34A)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(garment.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (garment.mode == "model3d") "3D (.glb)" else "Immagini",
                        fontSize = 12.sp, color = TextSecondary
                    )
                    Text("|", fontSize = 12.sp, color = TextSecondary)
                    Text(garment.type, fontSize = 12.sp, color = TextSecondary)
                }
                if (garment.isUserUploaded) {
                    Text("Caricato da te", fontSize = 11.sp, color = Primary)
                }
            }
            // Delete button (only for user uploaded)
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Elimina", tint = Color(0xFFEF4444))
                }
            }
        }
    }
}
