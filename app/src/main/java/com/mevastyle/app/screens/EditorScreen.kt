package com.mevastyle.app.screens

import android.graphics.BitmapFactory; import android.net.Uri; import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*; import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape; import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons; import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*; import androidx.compose.runtime.*
import androidx.compose.ui.Alignment; import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip; import androidx.compose.ui.graphics.Color; import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext; import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp; import androidx.compose.ui.unit.sp; import androidx.compose.ui.viewinterop.AndroidView
import com.mevastyle.app.data.*; import com.mevastyle.app.editor.*; import com.mevastyle.app.ui.theme.*
import androidx.compose.foundation.rememberScrollState
import kotlinx.coroutines.Dispatchers; import kotlinx.coroutines.launch; import kotlinx.coroutines.withContext

val TEXT_COLORS = listOf(Color.Black, Color.White, Color(0xFFDC2626), Color(0xFF2563EB),
    Color(0xFF16A34A), Color(0xFFEAB308), Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFFF97316), Color(0xFF6B7280))
val FONT_OPTIONS = listOf("sans-serif" to "Sans Serif", "serif" to "Serif", "monospace" to "Monospace", "cursive" to "Corsivo", "sans-serif-condensed" to "Condensed")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    shirtColor: TShirtColor, onShirtColorChanged: (TShirtColor) -> Unit,
    projectId: String, onBack: () -> Unit,
    onPreview: (Map<TShirtSide, android.graphics.Bitmap>, Map<TShirtSide, android.graphics.Bitmap>) -> Unit,
    modelPath: String? = null, modelIsAsset: Boolean = false, modelName: String? = null
) {
    val hasGlbModel = modelPath != null
    val context = LocalContext.current; val scope = rememberCoroutineScope()
    var canvasView by remember { mutableStateOf<EditorCanvasView?>(null) }
    var rotationView by remember { mutableStateOf<TShirt3DView?>(null) }
    var selectedEl by remember { mutableStateOf<CanvasElement?>(null) }
    var textInput by remember { mutableStateOf("") }; var fontSize by remember { mutableStateOf(48f) }
    var textColor by remember { mutableStateOf(Color.Black) }; var selectedFont by remember { mutableStateOf("sans-serif") }
    var dtfEnabled by remember { mutableStateOf(true) }; var showGrid by remember { mutableStateOf(false) }
    var showTextDialog by remember { mutableStateOf(false) }; var editingTextElement by remember { mutableStateOf<CanvasElement.TextElement?>(null) }
    var viewZoom by remember { mutableStateOf(1f) }; var showColorPicker by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }; var saveName by remember { mutableStateOf("") }; var loaded by remember { mutableStateOf(false) }
    var currentSide by remember { mutableStateOf(TShirtSide.FRONT) }
    // Modalita: "3d" = vista 3D rotabile, "edit" = canvas 2D per editing
    var mode by remember { mutableStateOf("3d") }

    fun loadShirtBitmap(side: TShirtSide): android.graphics.Bitmap? {
        val sideFile = "tshirts/tshirt_${shirtColor.name}_${side.fileSuffix}.png"
        val fallback = "tshirts/tshirt_${shirtColor.name}.png"
        return try { context.assets.open(sideFile).use { BitmapFactory.decodeStream(it) } }
            catch (_: Exception) { try { context.assets.open(fallback).use { BitmapFactory.decodeStream(it) } } catch (_: Exception) { null } }
    }

    val shirtBitmap = remember(shirtColor.name, currentSide) { loadShirtBitmap(currentSide) }

    LaunchedEffect(projectId) {
        if (!loaded) { withContext(Dispatchers.IO) {
            val proj = ProjectDatabase.get(context).projectDao().getById(projectId)
            if (proj != null && proj.canvasJson.isNotBlank()) {
                val els = CanvasSerializer.deserialize(context, proj.canvasJson, projectId)
                withContext(Dispatchers.Main) { canvasView?.elements?.clear(); canvasView?.elements?.addAll(els); canvasView?.invalidate(); saveName = proj.name }
            }
        }; loaded = true }
    }

    fun saveProject(name: String = saveName.ifBlank { "Bozza" }, silent: Boolean = false) {
        val cv = canvasView ?: return
        scope.launch(Dispatchers.IO) {
            val json = CanvasSerializer.serialize(context, cv.elements.toList(), projectId)
            ProjectDatabase.get(context).projectDao().upsert(SavedProject(projectId, name, shirtColor.name, canvasJson = json, updatedAt = System.currentTimeMillis()))
            if (!silent) withContext(Dispatchers.Main) { Toast.makeText(context, "Salvato!", Toast.LENGTH_SHORT).show() }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { val st = context.contentResolver.openInputStream(it); val bmp = BitmapFactory.decodeStream(st); st?.close()
            if (bmp != null) { val el = CanvasElement.ImageElement(x=100f, y=100f, bitmap=bmp, side=currentSide)
                canvasView?.elements?.add(el); canvasView?.selectedElement=el; canvasView?.invalidate(); selectedEl=el } }
    }

    Column(Modifier.fillMaxSize()) {
        // Top bar
        Row(Modifier.fillMaxWidth().padding(horizontal=8.dp, vertical=4.dp), verticalAlignment=Alignment.CenterVertically, horizontalArrangement=Arrangement.SpaceBetween) {
            IconButton(onClick = { if (mode == "edit") mode = "3d" else { saveProject(silent=true); onBack() } }) {
                Icon(Icons.Default.ArrowBack, "Indietro", tint = TextPrimary)
            }
            Text(if (mode == "3d") "Vista 3D" else "Editor - ${currentSide.label}", fontWeight=FontWeight.Bold, fontSize=16.sp, color=TextPrimary)
            Row {
                if (mode == "edit") IconButton(onClick = { mode = "3d" }) { Icon(Icons.Default.ViewInAr, "3D", tint = Primary) }
                IconButton(onClick = { showSaveDialog = true }) { Icon(Icons.Default.Save, "Salva", tint = Primary) }
                IconButton(onClick = { saveProject(silent=true)
                    val cv = canvasView ?: return@IconButton
                    val designs = mutableMapOf<TShirtSide, android.graphics.Bitmap>()
                    val mockups = mutableMapOf<TShirtSide, android.graphics.Bitmap>()
                    for (s in TShirtSide.all) { designs[s] = cv.exportDesignBitmap(s); mockups[s] = cv.exportMockupBitmap(s, loadShirtBitmap(s)) }
                    onPreview(designs, mockups)
                }) { Icon(Icons.Default.Visibility, "Preview", tint = Cta) }
            }
        }

        if (mode == "3d") {
            // ── MODALITA 3D ──
            Box(Modifier.weight(1f).fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                if (hasGlbModel) {
                    // Modello GLB reale — Model3DViewer gestisce errori internamente
                    Model3DViewer(modelPath = modelPath!!, isAsset = modelIsAsset,
                        modifier = Modifier.fillMaxSize())
                } else {
                    // Vista 3D classica con immagini flat
                    AndroidView(
                        factory = { ctx -> TShirt3DView(ctx).also { v ->
                            v.frontBitmap = loadShirtBitmap(TShirtSide.FRONT)
                            v.backBitmap = loadShirtBitmap(TShirtSide.BACK)
                            v.sleeveLeftBitmap = loadShirtBitmap(TShirtSide.SLEEVE_LEFT)
                            v.sleeveRightBitmap = loadShirtBitmap(TShirtSide.SLEEVE_RIGHT)
                            canvasView?.let { cv ->
                                v.frontDesign = cv.exportDesignBitmap(TShirtSide.FRONT)
                                v.backDesign = cv.exportDesignBitmap(TShirtSide.BACK)
                                v.sleeveLeftDesign = cv.exportDesignBitmap(TShirtSide.SLEEVE_LEFT)
                                v.sleeveRightDesign = cv.exportDesignBitmap(TShirtSide.SLEEVE_RIGHT)
                            }
                            v.onSideChanged = { side -> currentSide = side }
                            rotationView = v
                        }},
                        update = { v ->
                            v.frontBitmap = loadShirtBitmap(TShirtSide.FRONT)
                            v.backBitmap = loadShirtBitmap(TShirtSide.BACK)
                            v.sleeveLeftBitmap = loadShirtBitmap(TShirtSide.SLEEVE_LEFT)
                            v.sleeveRightBitmap = loadShirtBitmap(TShirtSide.SLEEVE_RIGHT)
                            canvasView?.let { cv ->
                                v.frontDesign = cv.exportDesignBitmap(TShirtSide.FRONT)
                                v.backDesign = cv.exportDesignBitmap(TShirtSide.BACK)
                                v.sleeveLeftDesign = cv.exportDesignBitmap(TShirtSide.SLEEVE_LEFT)
                                v.sleeveRightDesign = cv.exportDesignBitmap(TShirtSide.SLEEVE_RIGHT)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Labels
                Text(if (hasGlbModel) (modelName ?: "Modello 3D") else currentSide.label,
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                    .background(Primary.copy(alpha=0.85f), RoundedCornerShape(6.dp)).padding(horizontal=12.dp, vertical=6.dp),
                    color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(if (hasGlbModel) "Trascina per ruotare" else "Scorri per ruotare",
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom=8.dp),
                    color = TextSecondary, fontSize = 12.sp)
            }
            // Edit button per il lato corrente
            Button(onClick = { mode = "edit" }, colors = ButtonDefaults.buttonColors(containerColor = Primary),
                modifier = Modifier.fillMaxWidth().padding(horizontal=16.dp, vertical=8.dp).height(52.dp)) {
                Icon(Icons.Default.Edit, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                Text("Modifica ${currentSide.label}", fontSize=16.sp, fontWeight=FontWeight.SemiBold)
            }
            // Quick side buttons
            Row(Modifier.fillMaxWidth().padding(horizontal=16.dp, vertical=4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                TShirtSide.all.forEach { side ->
                    val isActive = currentSide == side
                    val hasEl = canvasView?.elements?.any { it.side == side } == true
                    FilterChip(selected=isActive, onClick={ currentSide = side; rotationView?.rotateTo(side) },
                        label={ Row(verticalAlignment=Alignment.CenterVertically) { Text(side.label, fontSize=11.sp)
                            if(hasEl){ Spacer(Modifier.width(3.dp)); Box(Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF4ade80))) } } })
                }
            }
        } else {
            // ── MODALITA EDIT: Canvas 2D piatto per il lato corrente ──
            Box(Modifier.weight(1f).fillMaxWidth().padding(4.dp), contentAlignment = Alignment.Center) {
                AndroidView(
                    factory = { ctx -> EditorCanvasView(ctx).also { cv ->
                        cv.dtfEnabled=dtfEnabled; cv.showGrid=showGrid; cv.shirtBitmap=shirtBitmap; cv.currentSide=currentSide
                        cv.onSelectionChanged = { el -> selectedEl = el }; cv.onViewZoomChanged = { z -> viewZoom = z }; canvasView = cv
                    }},
                    update = { cv -> cv.dtfEnabled=dtfEnabled; cv.showGrid=showGrid; cv.shirtBitmap=shirtBitmap; cv.currentSide=currentSide },
                    modifier = Modifier.fillMaxSize()
                )
                if (viewZoom != 1f) Text("${(viewZoom*100).toInt()}%", modifier=Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .background(Color.Black.copy(alpha=0.6f), RoundedCornerShape(4.dp)).padding(horizontal=8.dp, vertical=2.dp), color=Color.White, fontSize=11.sp)
                Text(currentSide.label, modifier=Modifier.align(Alignment.TopStart).padding(8.dp)
                    .background(Primary.copy(alpha=0.8f), RoundedCornerShape(4.dp)).padding(horizontal=10.dp, vertical=4.dp), color=Color.White, fontSize=12.sp, fontWeight=FontWeight.Bold)
            }
            // Toolbar
            Row(Modifier.fillMaxWidth().background(Color(0xFF1A1A22)).padding(8.dp), horizontalArrangement=Arrangement.SpaceEvenly) {
                IconButton(onClick={imagePicker.launch("image/*")}) { Column(horizontalAlignment=Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Image, "Img", tint=Color.White, modifier=Modifier.size(24.dp)); Text("Immagine", fontSize=10.sp, color=Color.White) } }
                IconButton(onClick={editingTextElement=null;textInput="";showTextDialog=true}) { Column(horizontalAlignment=Alignment.CenterHorizontally) {
                    Icon(Icons.Default.TextFields, "Txt", tint=Color.White, modifier=Modifier.size(24.dp)); Text("Testo", fontSize=10.sp, color=Color.White) } }
                IconButton(onClick={showGrid=!showGrid}) { Column(horizontalAlignment=Alignment.CenterHorizontally) {
                    Icon(Icons.Default.GridOn, "Grid", tint=if(showGrid) Primary else Color.White, modifier=Modifier.size(24.dp)); Text("Griglia", fontSize=10.sp, color=Color.White) } }
                IconButton(onClick={dtfEnabled=!dtfEnabled}) { Column(horizontalAlignment=Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Palette, "DTF", tint=if(dtfEnabled) Primary else Color.White, modifier=Modifier.size(24.dp)); Text("DTF", fontSize=10.sp, color=Color.White) } }
                IconButton(onClick={showColorPicker=true}) { Column(horizontalAlignment=Alignment.CenterHorizontally) {
                    Box(Modifier.size(24.dp).clip(CircleShape).background(shirtColor.color).border(1.dp,Color.White,CircleShape)); Text("Colore", fontSize=10.sp, color=Color.White) } }
                if (selectedEl != null) IconButton(onClick={canvasView?.elements?.remove(selectedEl);canvasView?.selectedElement=null;canvasView?.invalidate();selectedEl=null}) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally) { Icon(Icons.Default.Delete, "Del", tint=Color(0xFFEF4444), modifier=Modifier.size(24.dp)); Text("Elimina", fontSize=10.sp, color=Color(0xFFEF4444)) } }
            }
            // Selected element info
            selectedEl?.let { el -> Row(Modifier.fillMaxWidth().background(Color(0xFF22222C)).padding(horizontal=12.dp, vertical=6.dp), verticalAlignment=Alignment.CenterVertically) {
                Text(when(el){ is CanvasElement.TextElement -> "Testo: \"${el.text.take(20)}\""; is CanvasElement.ImageElement -> "Immagine" }, fontSize=12.sp, color=TextSecondary, modifier=Modifier.weight(1f))
                if(el is CanvasElement.TextElement) TextButton(onClick={editingTextElement=el;textInput=el.text;fontSize=el.fontSize;textColor=Color(el.color);selectedFont=el.fontFamily;showTextDialog=true}, contentPadding=PaddingValues(4.dp)){Text("Modifica",fontSize=12.sp,color=Primary)}
            }}
        }
    }

    // Text dialog
    if (showTextDialog) AlertDialog(onDismissRequest={showTextDialog=false},
        title={Text(if(editingTextElement!=null) "Modifica Testo" else "Aggiungi Testo")},
        text={ Column { OutlinedTextField(textInput,{textInput=it},label={Text("Testo")},modifier=Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp)); Text("Dimensione: ${fontSize.toInt()}px",fontSize=12.sp); Slider(fontSize,{fontSize=it},valueRange=16f..120f)
            Spacer(Modifier.height(8.dp)); Text("Colore",fontSize=12.sp)
            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){TEXT_COLORS.forEach{c->Box(Modifier.size(32.dp).clip(CircleShape).background(c).border(2.dp,if(textColor==c) Primary else Color.Transparent,CircleShape).clickable{textColor=c})}}
            Spacer(Modifier.height(8.dp)); Text("Font",fontSize=12.sp)
            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){FONT_OPTIONS.forEach{(f,l)->FilterChip(selectedFont==f,{selectedFont=f},label={Text(l,fontSize=11.sp)})}}
        }},
        confirmButton={TextButton(onClick={if(textInput.isNotBlank()){
            if(editingTextElement!=null){editingTextElement!!.text=textInput;editingTextElement!!.fontSize=fontSize;editingTextElement!!.color=textColor.toArgb();editingTextElement!!.fontFamily=selectedFont}
            else{val el=CanvasElement.TextElement(x=100f,y=200f,text=textInput,fontSize=fontSize,color=textColor.toArgb(),fontFamily=selectedFont,side=currentSide);canvasView?.elements?.add(el);canvasView?.selectedElement=el;selectedEl=el}
            canvasView?.invalidate()};showTextDialog=false}){Text("OK")}},
        dismissButton={TextButton(onClick={showTextDialog=false}){Text("Annulla")}})

    // Color picker
    if(showColorPicker) AlertDialog(onDismissRequest={showColorPicker=false}, title={Text("Cambia Colore")},
        text={ Column { TSHIRT_COLORS.chunked(3).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){row.forEach{c->
            Column(Modifier.clip(RoundedCornerShape(8.dp)).clickable{onShirtColorChanged(c);showColorPicker=false}.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally){
                Box(Modifier.size(40.dp).clip(CircleShape).background(c.color).border(2.dp,if(shirtColor.name==c.name) Primary else Color.Transparent,CircleShape)); Text(c.label,fontSize=11.sp,color=TextPrimary)}}}}}},
        confirmButton={TextButton(onClick={showColorPicker=false}){Text("Chiudi")}})

    // Save dialog
    if(showSaveDialog) AlertDialog(onDismissRequest={showSaveDialog=false}, title={Text("Salva Progetto")},
        text={OutlinedTextField(saveName,{saveName=it},label={Text("Nome")},modifier=Modifier.fillMaxWidth())},
        confirmButton={TextButton(onClick={saveProject(saveName.ifBlank{"Bozza"});showSaveDialog=false}){Text("Salva")}},
        dismissButton={TextButton(onClick={showSaveDialog=false}){Text("Annulla")}})
}
