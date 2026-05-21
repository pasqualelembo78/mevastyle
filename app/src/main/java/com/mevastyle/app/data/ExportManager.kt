package com.mevastyle.app.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Gestisce l'export del progetto per l'invio via email/whatsapp.
 * Genera:
 * 1. Immagine composita con tutte le viste (fronte, retro, lati)
 * 2. Singole immagini per ogni vista
 * 3. File design DTF (solo il design senza maglietta)
 * 4. Riepilogo ordine testuale
 */
object ExportManager {

    /**
     * Genera un'immagine composita 2x2 con le 4 viste della maglietta.
     * Utile per chi riceve l'ordine: vede tutto in un'unica immagine.
     */
    fun generateCompositeImage(
        sideBitmaps: Map<TShirtSide, Bitmap>,
        shirtColor: TShirtColor,
        garmentName: String = "Maglietta"
    ): Bitmap {
        val tileW = 600; val tileH = 800
        val padding = 20; val headerH = 80
        val totalW = tileW * 2 + padding * 3
        val totalH = tileH * 2 + padding * 3 + headerH

        val composite = Bitmap.createBitmap(totalW, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(composite)
        canvas.drawColor(Color.WHITE)

        // Header
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 36f; color = Color.BLACK; typeface = Typeface.DEFAULT_BOLD }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 24f; color = Color.GRAY }
        canvas.drawText("MevaStyle - $garmentName", padding.toFloat(), 45f, titlePaint)
        canvas.drawText("Colore: ${shirtColor.label} | ${TShirtSide.all.size} viste", padding.toFloat(), 72f, subtitlePaint)

        // Disegna le 4 viste in griglia 2x2
        val positions = listOf(
            TShirtSide.FRONT to (padding to headerH + padding),
            TShirtSide.BACK to (tileW + padding * 2 to headerH + padding),
            TShirtSide.SLEEVE_LEFT to (padding to headerH + tileH + padding * 2),
            TShirtSide.SLEEVE_RIGHT to (tileW + padding * 2 to headerH + tileH + padding * 2)
        )

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 28f; color = Color.parseColor("#2563EB"); typeface = Typeface.DEFAULT_BOLD }

        for ((side, pos) in positions) {
            val (x, y) = pos
            val bmp = sideBitmaps[side]
            if (bmp != null) {
                // Scala il bitmap per riempire il tile
                val scaled = Bitmap.createScaledBitmap(bmp, tileW, tileH, true)
                canvas.drawBitmap(scaled, x.toFloat(), y.toFloat(), null)
            } else {
                // Placeholder grigio
                val rect = android.graphics.RectF(x.toFloat(), y.toFloat(), (x + tileW).toFloat(), (y + tileH).toFloat())
                canvas.drawRoundRect(rect, 12f, 12f, Paint().apply { color = Color.LTGRAY })
            }
            // Label del lato
            canvas.drawText(side.label, x + 10f, y + 35f, labelPaint)
        }

        return composite
    }

    /**
     * Prepara e lancia l'intent per inviare l'ordine via email/whatsapp.
     * Include: composita + singole viste + design DTF.
     */
    fun sendOrder(
        context: Context,
        email: String,
        notes: String,
        sideBitmaps: Map<TShirtSide, Bitmap>,
        mockupBitmaps: Map<TShirtSide, Bitmap>,
        designBitmaps: Map<TShirtSide, Bitmap>,
        shirtColor: TShirtColor,
        garmentName: String = "Maglietta"
    ) {
        val cacheDir = File(context.cacheDir, "export"); cacheDir.mkdirs()
        val files = mutableListOf<File>()

        // 1. Immagine composita (panoramica completa)
        val composite = generateCompositeImage(mockupBitmaps, shirtColor, garmentName)
        val compositeFile = File(cacheDir, "PANORAMICA_${garmentName}.png")
        FileOutputStream(compositeFile).use { composite.compress(Bitmap.CompressFormat.PNG, 100, it) }
        files.add(compositeFile)

        // 2. Singole viste mockup
        for ((side, bmp) in mockupBitmaps) {
            val file = File(cacheDir, "MOCKUP_${side.label.uppercase()}.png")
            FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
            files.add(file)
        }

        // 3. Design DTF (solo il design, sfondo trasparente) - per la stampa
        for ((side, bmp) in designBitmaps) {
            // Verifica che il bitmap non sia tutto trasparente
            val hasContent = !isBlankBitmap(bmp)
            if (hasContent) {
                val file = File(cacheDir, "DTF_${side.label.uppercase()}.png")
                FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                files.add(file)
            }
        }

        // Crea intent
        val uris = files.map { FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it) }
        val sidesSummary = mockupBitmaps.keys.joinToString(", ") { it.label }
        val designSides = designBitmaps.filter { !isBlankBitmap(it.value) }.keys.joinToString(", ") { it.label }

        val body = buildString {
            appendLine("═══ ORDINE MEVASTYLE ═══")
            appendLine()
            appendLine("Capo: $garmentName")
            appendLine("Colore: ${shirtColor.label}")
            appendLine("Viste incluse: $sidesSummary")
            appendLine("Lati con design: $designSides")
            if (notes.isNotBlank()) { appendLine(); appendLine("Note: $notes") }
            appendLine()
            appendLine("═══ FILE ALLEGATI ═══")
            appendLine("• PANORAMICA: Vista completa 4 lati in un'immagine")
            appendLine("• MOCKUP_*: Mockup singoli per ogni lato")
            appendLine("• DTF_*: File design per stampa DTF (sfondo trasparente)")
            appendLine()
            appendLine("Generato con MevaStyle v3.0")
        }

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, "Ordine MevaStyle - $garmentName ${shirtColor.label}")
            putExtra(Intent.EXTRA_TEXT, body)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Invia ordine via..."))
    }

    private fun isBlankBitmap(bmp: Bitmap): Boolean {
        // Campiona alcuni pixel per verificare se il bitmap ha contenuto
        val w = bmp.width; val h = bmp.height
        val samples = listOf(w/4 to h/4, w/2 to h/2, 3*w/4 to 3*h/4, w/3 to h/3)
        return samples.all { (x, y) -> bmp.getPixel(x.coerceIn(0, w-1), y.coerceIn(0, h-1)) == Color.TRANSPARENT }
    }
}
