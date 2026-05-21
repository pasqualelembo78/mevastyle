package com.mevastyle.app.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mevastyle.app.ui.theme.*

@Composable
fun LoginScreen(onLoginSuccess: (Boolean) -> Unit, onSkip: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Person, null, Modifier.size(80.dp), tint = Primary)
        Spacer(Modifier.height(16.dp))
        Text("Benvenuto in MevaStyle", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("Crea la tua maglietta personalizzata", fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        // Google login coming soon
        OutlinedButton(
            onClick = { /* Coming soon */ },
            enabled = false,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("G", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
            Spacer(Modifier.width(12.dp))
            Text("Accedi con Google", fontSize = 16.sp, color = Color.Gray)
        }
        Spacer(Modifier.height(4.dp))
        Text("Accesso Google prossimamente", fontSize = 12.sp, color = TextSecondary)

        Spacer(Modifier.height(24.dp))
        Button(onClick = onSkip, colors = ButtonDefaults.buttonColors(containerColor = Primary),
            modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Icon(Icons.Default.ArrowForward, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
            Text("Continua senza account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
