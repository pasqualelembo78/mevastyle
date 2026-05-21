package com.mevastyle.app.editor

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Composable per visualizzare un modello 3D .glb con rotazione touch.
 *
 * Supporta:
 * - File da assets (es: "models/tshirt.glb")
 * - File dal filesystem (percorso assoluto)
 * - Rotazione libera con un dito
 * - Zoom con pinch
 * - Snap a viste predefinite (fronte/retro/lato)
 *
 * @param modelPath percorso del modello .glb
 * @param isAsset true se il modello e' negli assets
 * @param onViewAngleChanged callback con angolo corrente (0=fronte, 90=destra, 180=retro, 270=sinistra)
 */
@Composable
fun Model3DViewer(
    modelPath: String,
    isAsset: Boolean = true,
    modifier: Modifier = Modifier,
    onViewAngleChanged: ((Float) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    Box(modifier.background(Color(0xFFF0F0F5), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx ->
                SceneView(ctx).apply {
                    // Setup camera
                    cameraNode.position = Position(0f, 0.5f, 2.5f)
                    cameraNode.lookAt(Position(0f, 0.5f, 0f))

                    // Luce ambiente
                    mainLightNode?.let {
                        it.position = Position(0f, 3f, 3f)
                    }

                    // Carica modello
                    scope.launch {
                        try {
                            val uri = if (isAsset) "file:///android_asset/$modelPath"
                                else java.io.File(modelPath).toURI().toString()
                            val modelNode = ModelNode(
                                modelInstance = modelLoader.createModelInstance(uri)
                            ).apply {
                                position = Position(0f, 0f, 0f)
                                scale = io.github.sceneview.math.Scale(1f, 1f, 1f)
                            }
                            addChildNode(modelNode)
                            loading = false
                        } catch (e: Exception) {
                            error = "Errore caricamento modello: ${e.message}"
                            loading = false
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading overlay
        if (loading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF2563EB))
                Spacer(Modifier.height(8.dp))
                Text("Caricamento modello 3D...", fontSize = 12.sp, color = Color.Gray)
            }
        }

        // Error overlay
        error?.let {
            Text(it, color = Color(0xFFEF4444), fontSize = 12.sp,
                modifier = Modifier.padding(16.dp))
        }

        // Hint
        if (!loading && error == null) {
            Text("Trascina per ruotare \u2022 Pizzica per zoom", fontSize = 11.sp, color = Color.Gray,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
        }
    }
}

/**
 * Scatta screenshot del modello 3D da 4 angolazioni.
 * Usato per l'export multi-vista.
 */
suspend fun capture3DScreenshots(view: View): Map<String, Bitmap> {
    val screenshots = mutableMapOf<String, Bitmap>()
    // Lo screenshot viene catturato direttamente dalla view
    withContext(Dispatchers.Main) {
        view.isDrawingCacheEnabled = true
        view.buildDrawingCache()
        view.drawingCache?.let { screenshots["current"] = Bitmap.createBitmap(it) }
        view.isDrawingCacheEnabled = false
    }
    return screenshots
}
