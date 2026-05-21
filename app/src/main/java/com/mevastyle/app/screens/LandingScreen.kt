package com.mevastyle.app.screens
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mevastyle.app.data.AuthManager
import com.mevastyle.app.ui.theme.*

@Composable
fun LandingScreen(onStart: () -> Unit, onDrafts: () -> Unit, onCreations: () -> Unit,
    onAdmin: () -> Unit, onLogin: () -> Unit, onLogout: () -> Unit, onUpload: () -> Unit = {}, onModels: () -> Unit = {}, isAdmin: Boolean) {
    val ctx = LocalContext.current
    val logo = remember { ctx.assets.open("mevastyle_logo.png").use { BitmapFactory.decodeStream(it) } }
    val user = AuthManager.currentUser

    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(logo.asImageBitmap(), "MevaStyle", Modifier.fillMaxWidth(0.85f).padding(bottom = 8.dp), contentScale = ContentScale.FillWidth)
        Spacer(Modifier.height(4.dp))
        if (user != null) {
            Text("Ciao, ${user.displayName ?: user.email ?: "Utente"}!", fontSize = 14.sp, color = Primary, fontWeight = FontWeight.SemiBold)
            if (isAdmin) Text("⭐ Account Admin", fontSize = 12.sp, color = Color(0xFFFBBF24))
            Spacer(Modifier.height(4.dp))
        }
        Text("Crea la tua maglietta personalizzata", fontSize = 16.sp, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = Primary), modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Icon(Icons.Default.Add, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
            Text("Crea nuova maglietta", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onDrafts, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Icon(Icons.Default.Folder, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
            Text("Le mie bozze", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        if (user != null) {
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onCreations, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Default.Cloud, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                Text("Le mie creazioni", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onUpload, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Icon(Icons.Default.Upload, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
            Text("Carica nuovo capo", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onModels, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Icon(Icons.Default.ViewInAr, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
            Text("I miei modelli", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        if (isAdmin) {
            Spacer(Modifier.height(10.dp))
            Button(onClick = onAdmin, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Default.AdminPanelSettings, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                Text("Admin Panel", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(24.dp))
        if (user != null) TextButton(onClick = onLogout) {
            Icon(Icons.Default.Logout, null, Modifier.size(16.dp), tint = TextSecondary); Spacer(Modifier.width(4.dp))
            Text("Disconnetti (${user.email})", color = TextSecondary, fontSize = 12.sp)
        } else TextButton(onClick = onLogin) {
            Icon(Icons.Default.Login, null, Modifier.size(16.dp), tint = Primary); Spacer(Modifier.width(4.dp))
            Text("Accedi con Google", color = Primary, fontSize = 14.sp)
        }
    }
}
