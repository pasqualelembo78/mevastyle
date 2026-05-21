package com.mevastyle.app
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.mevastyle.app.data.*
import com.mevastyle.app.data.GarmentManager
import com.mevastyle.app.screens.*
import com.mevastyle.app.ui.theme.MevaStyleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MevaStyleTheme {
                var screen by remember { mutableStateOf(if (AuthManager.isLoggedIn) "landing" else "login") }
                var shirtColor by remember { mutableStateOf(TSHIRT_COLORS[0]) }
                var sideBitmaps by remember { mutableStateOf<Map<TShirtSide, Bitmap>>(emptyMap()) }
                var mockupBitmaps by remember { mutableStateOf<Map<TShirtSide, Bitmap>>(emptyMap()) }
                var projectId by remember { mutableStateOf(CanvasSerializer.newProjectId()) }
                var isAdmin by remember { mutableStateOf(false) }
                var editGarmentId by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(AuthManager.currentUser) { if (AuthManager.isLoggedIn) isAdmin = AuthManager.isAdmin() }

                when (screen) {
                    "login" -> LoginScreen(
                        onLoginSuccess = { admin -> isAdmin = admin; screen = "landing" },
                        onSkip = { screen = "landing" }
                    )
                    "landing" -> LandingScreen(
                        onStart = { projectId = CanvasSerializer.newProjectId(); screen = "select" },
                        onDrafts = { screen = "drafts" }, onCreations = { screen = "creations" },
                        onAdmin = { screen = "admin" }, onLogin = { screen = "login" },
                        onLogout = { AuthManager.signOut(this@MainActivity); isAdmin = false; screen = "login" },
                        onUpload = { screen = "upload" },
                        onModels = { screen = "models" },
                        isAdmin = isAdmin
                    )
                    "drafts" -> DraftsScreen(
                        onBack = { screen = "landing" },
                        onOpen = { p -> projectId = p.id; shirtColor = TSHIRT_COLORS.find { it.name == p.shirtColor } ?: TSHIRT_COLORS[0]; screen = "editor" }
                    )
                    "creations" -> UserCreationsScreen(onBack = { screen = "landing" })
                    "admin" -> AdminPanelScreen(onBack = { screen = "landing" })
                    "select" -> ShirtSelectorScreen(shirtColor, { shirtColor = it }, { screen = "editor" }, { screen = "landing" })
                    "editor" -> {
                        val garment = editGarmentId?.let { GarmentManager.getById(it) }
                        val mPath = garment?.let { GarmentManager.getModelFilePath(this@MainActivity, it) }
                        EditorScreen(
                            shirtColor = shirtColor, onShirtColorChanged = { shirtColor = it },
                            projectId = projectId,
                            onBack = { editGarmentId = null; screen = if (garment != null) "models" else "select" },
                            onPreview = { d, m -> sideBitmaps = d; mockupBitmaps = m; screen = "preview" },
                            modelPath = mPath,
                            modelIsAsset = garment?.isAsset ?: false,
                            modelName = garment?.name
                        )
                    }
                    "models" -> MyModelsScreen(
                        onBack = { screen = "landing" },
                        onOpenViewer = { garment ->
                            editGarmentId = garment.id
                            projectId = CanvasSerializer.newProjectId()
                            screen = "editor"
                        }
                    )
                    "upload" -> UploadGarmentScreen(
                        onBack = { screen = "landing" },
                        onGarmentAdded = { screen = "models" }
                    )
                    "preview" -> PreviewScreen(sideBitmaps = sideBitmaps, mockupBitmaps = mockupBitmaps, shirtColor = shirtColor, onBack = { screen = "editor" })
                }
            }
        }
    }
}
