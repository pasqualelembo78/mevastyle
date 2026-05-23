package com.mevastyle.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mevastyle.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Shield, null, Modifier.size(32.dp), tint = Primary)
                Column {
                    Text("MevaStyle", fontWeight = FontWeight.Bold, color = Primary, fontSize = 16.sp)
                    Text("Ultimo aggiornamento: Maggio 2025", fontSize = 12.sp, color = TextSecondary)
                }
            }

            PolicySection(
                number = "1",
                title = "Titolare del Trattamento",
                body = "Il titolare del trattamento dei dati personali è MevaStyle.\n\nEmail di contatto: privacy@mevastyle.it\nSito web: mevastyle.it"
            )
            PolicySection(
                number = "2",
                title = "Dati Raccolti",
                body = "L'applicazione MevaStyle può raccogliere e trattare le seguenti categorie di dati:\n\n• Dati account: indirizzo email e nome visualizzato, forniti tramite accesso Google (Google Sign-In), se scelto dall'utente.\n\n• Contenuti creati dall'utente: immagini delle personalizzazioni (design, mockup) salvate su Firebase Storage.\n\n• Dati di utilizzo: informazioni sulle creazioni salvate (colore, tipo di capo, timestamp) su Firebase Firestore.\n\n• File caricati: immagini o modelli 3D (.glb) caricati volontariamente dall'utente.\n\nL'utilizzo dell'app è possibile anche senza registrazione: in modalità anonima non vengono raccolti dati personali identificativi."
            )
            PolicySection(
                number = "3",
                title = "Finalità e Base Giuridica",
                body = "• Fornitura del servizio: salvataggio e recupero delle creazioni dell'utente (base giuridica: esecuzione del contratto, art. 6.1.b GDPR).\n\n• Autenticazione: identificazione sicura tramite Google Sign-In (base giuridica: consenso, art. 6.1.a GDPR).\n\n• Sicurezza: protezione dell'integrità del servizio (base giuridica: legittimo interesse, art. 6.1.f GDPR)."
            )
            PolicySection(
                number = "4",
                title = "Servizi di Terze Parti",
                body = "L'app utilizza i seguenti servizi di terze parti:\n\n• Firebase (Google LLC): autenticazione, database cloud, archiviazione file.\n  → firebase.google.com/support/privacy\n\n• Google Sign-In (Google LLC): accesso con account Google.\n  → policies.google.com/privacy"
            )
            PolicySection(
                number = "5",
                title = "Conservazione dei Dati",
                body = "I dati vengono conservati fino a:\n\n• Cancellazione esplicita da parte dell'utente tramite le funzionalità dell'app, oppure\n\n• Richiesta di cancellazione inviata a privacy@mevastyle.it\n\nI dati in modalità anonima (bozze locali) sono conservati solo sul dispositivo dell'utente e non vengono mai trasmessi ai nostri server."
            )
            PolicySection(
                number = "6",
                title = "Diritti dell'Utente (GDPR)",
                body = "In conformità al Regolamento UE 2016/679 (GDPR), hai diritto a:\n\n• Accedere ai tuoi dati personali\n• Richiedere la rettifica o la cancellazione\n• Opporti al trattamento o richiederne la limitazione\n• Richiedere la portabilità dei dati\n• Revocare il consenso in qualsiasi momento\n\nPer esercitare questi diritti: privacy@mevastyle.it"
            )
            PolicySection(
                number = "7",
                title = "Sicurezza dei Dati",
                body = "Adottiamo misure tecniche e organizzative adeguate per proteggere i dati personali da accessi non autorizzati, perdita o distruzione. Le comunicazioni avvengono tramite HTTPS e i dati su Firebase sono protetti dalle regole di sicurezza di Firebase."
            )
            PolicySection(
                number = "8",
                title = "Minori",
                body = "Il servizio è destinato a utenti di età pari o superiore a 13 anni. Non raccogliamo consapevolmente dati personali di minori di 13 anni."
            )
            PolicySection(
                number = "9",
                title = "Modifiche alla Privacy Policy",
                body = "Ci riserviamo il diritto di aggiornare la presente informativa. In caso di modifiche sostanziali, ne daremo avviso tramite l'applicazione. La data di \"ultimo aggiornamento\" riflette sempre la versione corrente."
            )
            PolicySection(
                number = "10",
                title = "Contatti",
                body = "Per qualsiasi domanda relativa al trattamento dei dati personali:\n\n📧 privacy@mevastyle.it\n🌐 mevastyle.it"
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PolicySection(number: String, title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .size(28.dp)
                        .background(Primary, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(number, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
            }
            Text(body, fontSize = 14.sp, color = TextSecondary, lineHeight = 22.sp)
        }
    }
}
