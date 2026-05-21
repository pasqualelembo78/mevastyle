package com.mevastyle.app.screens
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DraftsScreen(onBack: () -> Unit, onOpen: (SavedProject) -> Unit) {
    val ctx = LocalContext.current; val scope = rememberCoroutineScope()
    var projects by remember { mutableStateOf<List<SavedProject>>(emptyList()) }
    var delId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { projects = ProjectDatabase.get(ctx).projectDao().getAll() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("< Indietro", color = Primary) }
        Text("Le mie bozze", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("${projects.size} design salvati", fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.height(16.dp))
        if (projects.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nessuna bozza salvata", fontSize = 16.sp, color = TextSecondary) }
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(projects, key = { it.id }) { p ->
                val c = TSHIRT_COLORS.find { it.name == p.shirtColor } ?: TSHIRT_COLORS[0]
                val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                Card(Modifier.fillMaxWidth().clickable { onOpen(p) }, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(c.color).border(1.dp, if (c.hex == 0xFFFFFFFF) Color.LightGray else Color.Transparent, RoundedCornerShape(8.dp)))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("${c.label} - ${df.format(Date(p.updatedAt))}", fontSize = 12.sp, color = TextSecondary)
                        }
                        IconButton(onClick = { delId = p.id }) { Icon(Icons.Default.Delete, "Elimina", tint = Color(0xFFDC2626)) }
                    }
                }
            }
        }
    }
    delId?.let { id -> AlertDialog(onDismissRequest = { delId = null }, title = { Text("Elimina bozza?") }, text = { Text("Questa azione non puo' essere annullata.") },
        confirmButton = { TextButton(onClick = { scope.launch { ProjectDatabase.get(ctx).projectDao().delete(id); CanvasSerializer.deleteProjectFiles(ctx, id); projects = ProjectDatabase.get(ctx).projectDao().getAll(); delId = null } }) { Text("Elimina", color = Color(0xFFDC2626)) } },
        dismissButton = { TextButton(onClick = { delId = null }) { Text("Annulla") } })
    }
}
