package com.mevastyle.app.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import com.mevastyle.app.data.GarmentManager
import com.mevastyle.app.ui.theme.*

/**
 * Schermata per caricare un nuovo capo d'abbigliamento:
 * - Modello 3D (.glb)
 * - Oppure immagini flat (fronte/retro)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadGarmentScreen(onBack: () -> Unit, onGarmentAdded: (String) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("custom") }
    var mode by remember { mutableStateOf("model3d") } // "model3d" o "images"
    var glbUri by remember { mutableStateOf<Uri?>(null) }
    var frontUri by remember { mutableStateOf<Uri?>(null) }
    var backUri by remember { mutableStateOf<Uri?>(null) }

    val glbPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> glbUri = uri }
    val frontPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> frontUri = uri }
    val backPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> backUri = uri }

    val typeOptions = listOf("tshirt" to "Maglietta", "hoodie" to "Felpa", "jeans" to "Jeans",
        "hat" to "Cappello", "polo" to "Polo", "tank" to "Canottiera", "custom" to "Altro")

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("< Indietro", color = Primary) }
        Text("Carica nuovo capo", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("Modello 3D (.glb) o immagini flat", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(20.dp))

        // Nome
        OutlinedTextField(name, { name = it }, label = { Text("Nome del capo") }, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Es: Maglietta personalizzata") })
        Spacer(Modifier.height(12.dp))

        // Tipo
        Text("Tipo:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            typeOptions.take(4).forEach { (id, label) ->
                FilterChip(type == id, { type = id }, label = { Text(label, fontSize = 11.sp) })
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            typeOptions.drop(4).forEach { (id, label) ->
                FilterChip(type == id, { type = id }, label = { Text(label, fontSize = 11.sp) })
            }
        }
        Spacer(Modifier.height(16.dp))

        // Modalita'
        Text("Formato:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(mode == "model3d", { mode = "model3d" }, label = { Text("Modello 3D (.glb)") },
                leadingIcon = { Icon(Icons.Default.ViewInAr, null, Modifier.size(16.dp)) })
            FilterChip(mode == "images", { mode = "images" }, label = { Text("Immagini (PNG/JPG)") },
                leadingIcon = { Icon(Icons.Default.Image, null, Modifier.size(16.dp)) })
        }
        Spacer(Modifier.height(16.dp))

        if (mode == "model3d") {
            // Upload .glb
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Modello 3D", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Carica un file .glb o .gltf", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { glbPicker.launch("*/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = if (glbUri != null) Color(0xFF16A34A) else Primary),
                        modifier = Modifier.fillMaxWidth()) {
                        Icon(if (glbUri != null) Icons.Default.CheckCircle else Icons.Default.Upload, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (glbUri != null) "File selezionato!" else "Seleziona file .glb")
                    }
                }
            }
        } else {
            // Upload immagini
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Immagini del capo", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("Carica fronte e retro (opzionale)", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { frontPicker.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = if (frontUri != null) Color(0xFF16A34A) else Primary),
                            modifier = Modifier.weight(1f)) {
                            Text(if (frontUri != null) "Fronte OK" else "Fronte", fontSize = 13.sp) }
                        Button(onClick = { backPicker.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = if (backUri != null) Color(0xFF16A34A) else Primary),
                            modifier = Modifier.weight(1f)) {
                            Text(if (backUri != null) "Retro OK" else "Retro", fontSize = 13.sp) }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Conferma
        val canSave = name.isNotBlank() && ((mode == "model3d" && glbUri != null) || (mode == "images" && frontUri != null))
        Button(
            onClick = {
                if (mode == "model3d" && glbUri != null) {
                    val garment = GarmentManager.addUserModel(context, glbUri!!, name, type)
                    if (garment != null) {
                        Toast.makeText(context, "Modello aggiunto!", Toast.LENGTH_SHORT).show()
                        onGarmentAdded(garment.id)
                    } else Toast.makeText(context, "Errore nel salvataggio", Toast.LENGTH_SHORT).show()
                } else if (mode == "images") {
                    val garment = GarmentManager.addUserImages(context, frontUri, backUri, name, type)
                    if (garment != null) {
                        Toast.makeText(context, "Immagini aggiunte!", Toast.LENGTH_SHORT).show()
                        onGarmentAdded(garment.id)
                    } else Toast.makeText(context, "Errore nel salvataggio", Toast.LENGTH_SHORT).show()
                }
            },
            enabled = canSave,
            colors = ButtonDefaults.buttonColors(containerColor = Cta),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.Add, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
            Text("Aggiungi capo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
