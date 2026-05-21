package com.mevastyle.app.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

/**
 * Rappresenta un capo d'abbigliamento disponibile nell'app.
 * Puo' essere un modello 3D (.glb) o immagini flat (PNG).
 */
data class Garment(
    val id: String,
    val name: String,
    val type: String = "tshirt",        // tshirt, hoodie, jeans, hat, custom
    val mode: String = "images",         // "model3d" = file .glb, "images" = PNG flat
    val modelPath: String? = null,       // percorso .glb (assets o file locale)
    val isAsset: Boolean = true,         // true = in assets/, false = in filesDir
    val sides: List<String> = listOf("front", "back", "sleeve_left", "sleeve_right"),
    val colors: List<String> = listOf(), // colori disponibili (per modo images)
    val isUserUploaded: Boolean = false
)

/**
 * Gestisce i capi disponibili: sia quelli precaricati che quelli aggiunti dall'utente.
 * Salva la lista in un file JSON locale.
 */
object GarmentManager {
    private const val CONFIG_FILE = "garments.json"
    private const val MODELS_DIR = "user_models"
    private const val IMAGES_DIR = "user_garment_images"

    private var garments = mutableListOf<Garment>()

    /** Inizializza con i capi di default */
    fun init(context: Context) {
        garments.clear()
        // Carica capi default dagli assets
        garments.addAll(getDefaultGarments(context))
        // Carica capi aggiunti dall'utente
        garments.addAll(loadUserGarments(context))
    }

    fun getAll(): List<Garment> = garments.toList()
    fun getByType(type: String): List<Garment> = garments.filter { it.type == type }
    fun getById(id: String): Garment? = garments.find { it.id == id }
    fun get3DModels(): List<Garment> = garments.filter { it.mode == "model3d" }
    fun getFlatGarments(): List<Garment> = garments.filter { it.mode == "images" }

    /** Restituisce il percorso del file .glb (per SceneView) */
    fun getModelFilePath(context: Context, garment: Garment): String? {
        if (garment.mode != "model3d" || garment.modelPath == null) return null
        return if (garment.isAsset) {
            // In assets: SceneView puo' caricare direttamente
            garment.modelPath
        } else {
            // File utente: percorso assoluto
            File(context.filesDir, "$MODELS_DIR/${garment.modelPath}").absolutePath
        }
    }

    /** Aggiunge un modello 3D caricato dall'utente */
    fun addUserModel(context: Context, uri: Uri, name: String, type: String): Garment? {
        try {
            val dir = File(context.filesDir, MODELS_DIR); dir.mkdirs()
            val filename = "model_${System.currentTimeMillis()}.glb"
            val destFile = File(dir, filename)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            }
            val garment = Garment(
                id = "user_${System.currentTimeMillis()}",
                name = name, type = type, mode = "model3d",
                modelPath = filename, isAsset = false,
                sides = listOf("front", "back", "left", "right"),
                isUserUploaded = true
            )
            garments.add(garment)
            saveUserGarments(context)
            return garment
        } catch (e: Exception) { return null }
    }

    /** Aggiunge immagini flat caricate dall'utente */
    fun addUserImages(context: Context, frontUri: Uri?, backUri: Uri?, name: String, type: String): Garment? {
        try {
            val dir = File(context.filesDir, IMAGES_DIR); dir.mkdirs()
            val id = "userimg_${System.currentTimeMillis()}"
            val subDir = File(dir, id); subDir.mkdirs()

            // Copia le immagini
            val sides = mutableListOf<String>()
            frontUri?.let { copyImage(context, it, File(subDir, "front.png")); sides.add("front") }
            backUri?.let { copyImage(context, it, File(subDir, "back.png")); sides.add("back") }

            val garment = Garment(
                id = id, name = name, type = type, mode = "images",
                modelPath = id, isAsset = false, sides = sides,
                isUserUploaded = true
            )
            garments.add(garment)
            saveUserGarments(context)
            return garment
        } catch (e: Exception) { return null }
    }

    /** Restituisce il bitmap path per un'immagine flat utente */
    fun getUserImagePath(context: Context, garmentId: String, side: String): String? {
        val file = File(context.filesDir, "$IMAGES_DIR/$garmentId/$side.png")
        return if (file.exists()) file.absolutePath else null
    }

    fun removeGarment(context: Context, id: String) {
        val g = garments.find { it.id == id } ?: return
        if (g.isUserUploaded) {
            // Elimina file associati
            if (g.mode == "model3d") File(context.filesDir, "$MODELS_DIR/${g.modelPath}").delete()
            else File(context.filesDir, "$IMAGES_DIR/${g.modelPath}").deleteRecursively()
        }
        garments.removeAll { it.id == id }
        saveUserGarments(context)
    }

    // ── Helpers ──

    private fun getDefaultGarments(context: Context): List<Garment> {
        val defaults = mutableListOf<Garment>()
        // Controlla se ci sono modelli .glb in assets/models/
        try {
            val models = context.assets.list("models") ?: emptyArray()
            for (m in models) {
                if (m.endsWith(".glb")) {
                    val name = m.removeSuffix(".glb").replace("_", " ").replaceFirstChar { it.uppercase() }
                    defaults.add(Garment(
                        id = "asset_$m", name = name, type = "tshirt", mode = "model3d",
                        modelPath = "models/$m", isAsset = true,
                        sides = listOf("front", "back", "left", "right")
                    ))
                }
            }
        } catch (_: Exception) {}

        // Immagini flat default (le magliette originali)
        defaults.add(Garment(
            id = "default_tshirt", name = "Maglietta Classic", type = "tshirt", mode = "images",
            isAsset = true, sides = listOf("front", "back", "sleeve_left", "sleeve_right"),
            colors = listOf("white","black","red","blue","green","yellow","gray","navy","pink")
        ))
        return defaults
    }

    private fun loadUserGarments(context: Context): List<Garment> {
        val file = File(context.filesDir, CONFIG_FILE)
        if (!file.exists()) return emptyList()
        return try {
            val json = file.readText()
            Gson().fromJson(json, object : TypeToken<List<Garment>>() {}.type)
        } catch (_: Exception) { emptyList() }
    }

    private fun saveUserGarments(context: Context) {
        val userGarments = garments.filter { it.isUserUploaded }
        val json = Gson().toJson(userGarments)
        File(context.filesDir, CONFIG_FILE).writeText(json)
    }

    private fun copyImage(context: Context, uri: Uri, dest: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
    }
}
