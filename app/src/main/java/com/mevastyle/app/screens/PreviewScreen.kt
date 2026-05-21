package com.mevastyle.app.screens

import android.content.ContentValues; import android.content.Context; import android.content.Intent
import android.graphics.Bitmap; import android.os.Build; import android.os.Environment
import android.provider.MediaStore; import android.widget.Toast
import androidx.compose.foundation.*; import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons; import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*; import androidx.compose.runtime.*
import androidx.compose.ui.Alignment; import androidx.compose.ui.Modifier; import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color; import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext; import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp; import androidx.compose.ui.unit.sp; import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.mevastyle.app.data.*; import com.mevastyle.app.editor.TShirt3DView; import com.mevastyle.app.ui.theme.*
import kotlinx.coroutines.Dispatchers; import kotlinx.coroutines.launch; import kotlinx.coroutines.withContext
import java.io.File; import java.io.FileOutputStream

@Composable
fun PreviewScreen(sideBitmaps: Map<TShirtSide, Bitmap>, mockupBitmaps: Map<TShirtSide, Bitmap>,
    shirtColor: TShirtColor, onBack: () -> Unit) {
    val ctx = LocalContext.current; val scope = rememberCoroutineScope()
    var currentSide by remember { mutableStateOf(TShirtSide.FRONT) }
    var showEmail by remember { mutableStateOf(false) }; var email by remember { mutableStateOf("") }; var notes by remember { mutableStateOf("") }
    var uploading by remember { mutableStateOf(false) }; var uploadSuccess by remember { mutableStateOf<Boolean?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween) {
            TextButton(onClick=onBack) { Icon(Icons.Default.Edit,null,Modifier.size(18.dp),tint=Primary); Spacer(Modifier.width(4.dp)); Text("Modifica",color=Primary,fontWeight=FontWeight.SemiBold) }
        }
        Spacer(Modifier.height(8.dp))
        Text("Anteprima 360\u00b0", fontSize=28.sp, fontWeight=FontWeight.Bold, color=TextPrimary)
        Text("Scorri la maglietta per vederla da ogni angolazione", fontSize=13.sp, color=TextSecondary)
        Spacer(Modifier.height(12.dp))

        // 3D Rotation Preview
        Box(Modifier.fillMaxWidth().height(350.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F0F5))) {
            AndroidView(
                factory = { c -> TShirt3DView(c).also { v ->
                    v.frontBitmap = mockupBitmaps[TShirtSide.FRONT]; v.backBitmap = mockupBitmaps[TShirtSide.BACK]
                    v.sleeveLeftBitmap = mockupBitmaps[TShirtSide.SLEEVE_LEFT]; v.sleeveRightBitmap = mockupBitmaps[TShirtSide.SLEEVE_RIGHT]
                    v.onSideChanged = { s -> currentSide = s }
                }},
                update = { v ->
                    v.frontBitmap = mockupBitmaps[TShirtSide.FRONT]; v.backBitmap = mockupBitmaps[TShirtSide.BACK]
                    v.sleeveLeftBitmap = mockupBitmaps[TShirtSide.SLEEVE_LEFT]; v.sleeveRightBitmap = mockupBitmaps[TShirtSide.SLEEVE_RIGHT]
                },
                modifier = Modifier.fillMaxSize()
            )
            Text(currentSide.label, modifier=Modifier.align(Alignment.TopStart).padding(8.dp)
                .background(Primary.copy(alpha=0.85f), RoundedCornerShape(4.dp)).padding(horizontal=10.dp, vertical=4.dp),
                color=Color.White, fontSize=12.sp, fontWeight=FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))
        Text("Colore: ${shirtColor.label}", fontSize=14.sp, color=TextPrimary)
        val (cmW,cmH) = SidePrintArea.getCmArea(currentSide)
        Text("Area stampa: $cmW x $cmH cm", fontSize=14.sp, color=TextSecondary)
        Spacer(Modifier.height(20.dp))

        // Download
        Text("Download", fontSize=16.sp, fontWeight=FontWeight.Bold, color=TextPrimary, modifier=Modifier.padding(bottom=8.dp))
        Button(onClick={mockupBitmaps[currentSide]?.let{saveToGallery(ctx,it,true,"mockup_${currentSide.fileSuffix}")}},
            colors=ButtonDefaults.buttonColors(containerColor=Primary), modifier=Modifier.fillMaxWidth().height(48.dp)) {
            Icon(Icons.Default.Download,null,Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Scarica ${currentSide.label} (PNG)") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick={mockupBitmaps.forEach{(s,b)->saveToGallery(ctx,b,true,"mockup_${s.fileSuffix}")};Toast.makeText(ctx,"Tutti i lati scaricati!",Toast.LENGTH_SHORT).show()},
            modifier=Modifier.fillMaxWidth().height(48.dp)){ Icon(Icons.Default.DownloadForOffline,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Scarica tutti i lati") }
        Spacer(Modifier.height(8.dp))
        Button(onClick={sideBitmaps[currentSide]?.let{saveToGallery(ctx,it,true,"design_${currentSide.fileSuffix}")}},
            colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF8B5CF6)), modifier=Modifier.fillMaxWidth().height(48.dp)){
            Icon(Icons.Default.Print,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Design DTF - ${currentSide.label}") }

        // Firebase upload
        if (AuthManager.isLoggedIn) { Spacer(Modifier.height(20.dp)); Divider(color=Color(0xFF333333)); Spacer(Modifier.height(16.dp))
            Text("Salva su Cloud", fontSize=16.sp, fontWeight=FontWeight.Bold, color=TextPrimary); Spacer(Modifier.height(8.dp))
            when { uploading -> Row(verticalAlignment=Alignment.CenterVertically){CircularProgressIndicator(Modifier.size(20.dp),Primary,strokeWidth=2.dp);Spacer(Modifier.width(8.dp));Text("Caricamento...",color=TextSecondary)}
                uploadSuccess==true -> Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.CheckCircle,null,tint=Color(0xFF4ade80));Spacer(Modifier.width(8.dp));Text("Salvato!",color=Color(0xFF4ade80),fontWeight=FontWeight.SemiBold)}
                else -> Button(onClick={uploading=true;scope.launch{val ok=FirebaseManager.saveCreation(sideBitmaps,mockupBitmaps[TShirtSide.FRONT],sideBitmaps[TShirtSide.FRONT],shirtColor);uploading=false;uploadSuccess=ok}},
                    colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF2563EB)), modifier=Modifier.fillMaxWidth().height(48.dp)){
                    Icon(Icons.Default.CloudUpload,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Salva su Firebase")}
            }
        }

        // Email order
        Spacer(Modifier.height(20.dp)); Divider(color=Color(0xFF333333)); Spacer(Modifier.height(16.dp))
        Text("Invia Ordine", fontSize=16.sp, fontWeight=FontWeight.Bold, color=TextPrimary); Spacer(Modifier.height(8.dp))
        if(!showEmail) Button(onClick={showEmail=true}, colors=ButtonDefaults.buttonColors(containerColor=Cta), modifier=Modifier.fillMaxWidth().height(52.dp)){
            Icon(Icons.Default.Email,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Invia via Email",fontSize=16.sp,fontWeight=FontWeight.SemiBold) }
        else { OutlinedTextField(email,{email=it},label={Text("Email destinatario")},modifier=Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp))
            OutlinedTextField(notes,{notes=it},label={Text("Note (opzionale)")},modifier=Modifier.fillMaxWidth(),minLines=2); Spacer(Modifier.height(12.dp))
            Button(onClick={
                val files=mutableListOf<File>(); mockupBitmaps.forEach{(s,b)->val f=File(ctx.cacheDir,"mockup_${s.fileSuffix}.png");FileOutputStream(f).use{o->b.compress(Bitmap.CompressFormat.PNG,100,o)};files.add(f)}
                val uris=files.map{FileProvider.getUriForFile(ctx,"${ctx.packageName}.fileprovider",it)}
                val intent=Intent(Intent.ACTION_SEND_MULTIPLE).apply{type="message/rfc822";putExtra(Intent.EXTRA_EMAIL,arrayOf(email))
                    putExtra(Intent.EXTRA_SUBJECT,"Ordine MevaStyle - ${shirtColor.label}");putExtra(Intent.EXTRA_TEXT,"Ordine MevaStyle\nColore: ${shirtColor.label}\n${if(notes.isNotBlank())"Note: $notes" else ""}")
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM,ArrayList(uris));addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)}
                ctx.startActivity(Intent.createChooser(intent,"Invia via..."))
            }, colors=ButtonDefaults.buttonColors(containerColor=Cta), modifier=Modifier.fillMaxWidth().height(48.dp)){
                Icon(Icons.Default.Send,null,Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Invia Ordine",fontWeight=FontWeight.SemiBold)} }
        Spacer(Modifier.height(32.dp))
    }
}

fun saveToGallery(ctx: Context, bmp: Bitmap, isPng: Boolean = true, prefix: String = "mevastyle") {
    val ext = if(isPng) "png" else "jpg"; val mime = if(isPng) "image/png" else "image/jpeg"
    val fmt = if(isPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
    val fn = "${prefix}_${System.currentTimeMillis()}.$ext"
    try { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val v = ContentValues().apply { put(MediaStore.Images.Media.DISPLAY_NAME,fn); put(MediaStore.Images.Media.MIME_TYPE,mime)
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+"/MevaStyle") }
        ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v)?.let{ctx.contentResolver.openOutputStream(it)?.use{o->bmp.compress(fmt,95,o)}}
    } else { val d=File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"MevaStyle");d.mkdirs();FileOutputStream(File(d,fn)).use{bmp.compress(fmt,95,it)} }
        Toast.makeText(ctx,"Salvato!",Toast.LENGTH_SHORT).show()
    } catch(e:Exception){Toast.makeText(ctx,"Errore: ${e.message}",Toast.LENGTH_SHORT).show()}
}
