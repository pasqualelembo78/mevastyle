package com.mevastyle.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
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
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Termini di Servizio", fontWeight = FontWeight.SemiBold) },
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
                Icon(Icons.Default.Description, null, Modifier.size(32.dp), tint = Primary)
                Column {
                    Text("MevaStyle", fontWeight = FontWeight.Bold, color = Primary, fontSize = 16.sp)
                    Text("Ultimo aggiornamento: Maggio 2025", fontSize = 12.sp, color = TextSecondary)
                }
            }

            TermsSection(
                number = "1",
                title = "Accettazione dei Termini",
                body = "Utilizzando l'applicazione MevaStyle, accetti integralmente i presenti Termini di Servizio. Se non accetti questi termini, non è consentito utilizzare il servizio."
            )
            TermsSection(
                number = "2",
                title = "Descrizione del Servizio",
                body = "MevaStyle è un'applicazione mobile che consente agli utenti di:\n\n• Personalizzare graficamente capi d'abbigliamento (magliette, felpe, ecc.)\n• Visualizzare anteprime 2D e 3D dei propri design\n• Salvare bozze localmente sul dispositivo\n• Caricare e salvare creazioni su cloud (previa registrazione)\n• Caricare modelli 3D (.glb) propri"
            )
            TermsSection(
                number = "3",
                title = "Contenuti dell'Utente",
                body = "Sei l'unico responsabile dei contenuti che carichi o crei tramite l'app. È vietato caricare contenuti che:\n\n• Violino diritti di proprietà intellettuale di terzi\n• Siano illegali, offensivi, diffamatori o discriminatori\n• Contengano malware o codice dannoso\n\nMevaStyle si riserva il diritto di rimuovere contenuti in violazione senza preavviso."
            )
            TermsSection(
                number = "4",
                title = "Proprietà Intellettuale",
                body = "Il software, il design e i contenuti originali di MevaStyle sono di proprietà esclusiva di MevaStyle e protetti dalle leggi sul diritto d'autore.\n\nI design creati dall'utente rimangono di proprietà dell'utente stesso."
            )
            TermsSection(
                number = "5",
                title = "Limitazione di Responsabilità",
                body = "MevaStyle non garantisce la disponibilità continuativa del servizio. Non siamo responsabili per eventuali perdite di dati, interruzioni del servizio o danni diretti o indiretti derivanti dall'uso dell'applicazione."
            )
            TermsSection(
                number = "6",
                title = "Modifiche al Servizio",
                body = "Ci riserviamo il diritto di modificare, sospendere o interrompere il servizio in qualsiasi momento, con o senza preavviso.\n\nCi riserviamo altresì il diritto di aggiornare i presenti Termini: l'uso continuato dell'app dopo le modifiche costituisce accettazione dei nuovi termini."
            )
            TermsSection(
                number = "7",
                title = "Legge Applicabile",
                body = "I presenti Termini sono regolati dalla legge italiana. Per qualsiasi controversia è competente il Tribunale del luogo di residenza del consumatore, ai sensi del Codice del Consumo italiano."
            )
            TermsSection(
                number = "8",
                title = "Contatti",
                body = "Per qualsiasi domanda sui presenti Termini:\n\n📧 info@mevastyle.it\n🌐 mevastyle.it"
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TermsSection(number: String, title: String, body: String) {
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
