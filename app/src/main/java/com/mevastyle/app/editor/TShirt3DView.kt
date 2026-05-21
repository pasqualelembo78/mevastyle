package com.mevastyle.app.editor

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.mevastyle.app.data.TShirtSide
import kotlin.math.abs

/**
 * Vista 3D della maglietta con rotazione a 360 gradi via touch.
 *
 * Swipe orizzontale con un dito per ruotare la maglietta.
 * Snap automatico al lato piu' vicino (fronte/retro/manica).
 * Design overlay per ogni lato.
 */
class TShirt3DView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : View(ctx, attrs) {

    // Immagini per ogni lato
    var frontBitmap: Bitmap? = null
    var backBitmap: Bitmap? = null
    var sleeveLeftBitmap: Bitmap? = null
    var sleeveRightBitmap: Bitmap? = null

    // Design overlay per ogni lato (generati dall'editor)
    var frontDesign: Bitmap? = null
    var backDesign: Bitmap? = null
    var sleeveLeftDesign: Bitmap? = null
    var sleeveRightDesign: Bitmap? = null

    // Callback quando cambia il lato attivo
    var onSideChanged: ((TShirtSide) -> Unit)? = null

    // Angolo corrente di rotazione (0=fronte, 90=manica dx, 180=retro, 270=manica sx)
    private var currentAngle = 0f
    private var targetAngle = 0f
    private var lastTouchX = 0f
    private var isDragging = false
    private val sensitivity = 0.4f

    // 3D rendering
    private val camera = Camera()
    private val transformMatrix = Matrix()
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val designPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply { alpha = 220 }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 36f; textAlign = Paint.Align.CENTER; isFakeBoldText = true
        setShadowLayer(4f, 0f, 2f, Color.argb(128, 0, 0, 0))
    }
    private val bgPaint = Paint().apply { color = Color.argb(20, 0, 0, 0) }

    // Snap animation
    private var snapAnimator: ValueAnimator? = null

    /** Imposta l'angolo direttamente (es. per navigare a un lato specifico) */
    fun setAngle(angle: Float, animate: Boolean = true) {
        if (animate) animateToAngle(angle) else { currentAngle = angle; invalidate() }
    }

    /** Ritorna il lato corrente basato sull'angolo */
    fun getCurrentSide(): TShirtSide = TShirtSide.fromAngle(currentAngle)

    /** Ruota al lato specificato con animazione */
    fun rotateTo(side: TShirtSide) = animateToAngle(side.angle)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        // Sfondo sottile
        canvas.drawRoundRect(RectF(4f, 4f, w - 4f, h - 4f), 16f, 16f, bgPaint)

        val normalizedAngle = ((currentAngle % 360f) + 360f) % 360f
        val currentSide = TShirtSide.fromAngle(normalizedAngle)

        // Determina quale bitmap mostrare + quello successivo per la transizione
        val (primaryBmp, secondaryBmp, blendFactor) = getBlendedBitmaps(normalizedAngle)

        // Calcola l'angolo locale dentro il quadrante (-45 a +45)
        val quadrantAngle = getQuadrantAngle(normalizedAngle)

        // ── Disegna la maglietta con prospettiva 3D ──
        drawRotatedBitmap(canvas, primaryBmp, secondaryBmp, blendFactor, quadrantAngle, w, h)

        // ── Overlay del design ──
        val currentDesign = when (currentSide) {
            TShirtSide.FRONT -> frontDesign; TShirtSide.BACK -> backDesign
            TShirtSide.SLEEVE_LEFT -> sleeveLeftDesign; TShirtSide.SLEEVE_RIGHT -> sleeveRightDesign
        }
        if (currentDesign != null && abs(quadrantAngle) < 35f) {
            drawRotatedDesign(canvas, currentDesign, quadrantAngle, w, h, currentSide)
        }

        // ── Label del lato corrente ──
        canvas.drawText(currentSide.label, w / 2f, h - 20f, labelPaint)

        // ── Indicatore di rotazione (pallini) ──
        drawRotationIndicator(canvas, normalizedAngle, w, h)
    }

    private fun getBlendedBitmaps(angle: Float): Triple<Bitmap?, Bitmap?, Float> {
        val side = TShirtSide.fromAngle(angle)
        val primary = getBitmapForSide(side)
        // Per transizioni fluide, calcoliamo se siamo vicini al bordo del quadrante
        val qa = getQuadrantAngle(angle)
        return if (abs(qa) > 35f) {
            val nextSide = if (qa > 0) getNextSide(side) else getPrevSide(side)
            Triple(primary, getBitmapForSide(nextSide), (abs(qa) - 35f) / 10f)
        } else Triple(primary, null, 0f)
    }

    private fun getBitmapForSide(side: TShirtSide): Bitmap? = when (side) {
        TShirtSide.FRONT -> frontBitmap; TShirtSide.BACK -> backBitmap
        TShirtSide.SLEEVE_LEFT -> sleeveLeftBitmap ?: frontBitmap
        TShirtSide.SLEEVE_RIGHT -> sleeveRightBitmap ?: frontBitmap
    }

    private fun getNextSide(side: TShirtSide): TShirtSide = when (side) {
        TShirtSide.FRONT -> TShirtSide.SLEEVE_RIGHT; TShirtSide.SLEEVE_RIGHT -> TShirtSide.BACK
        TShirtSide.BACK -> TShirtSide.SLEEVE_LEFT; TShirtSide.SLEEVE_LEFT -> TShirtSide.FRONT
    }
    private fun getPrevSide(side: TShirtSide): TShirtSide = when (side) {
        TShirtSide.FRONT -> TShirtSide.SLEEVE_LEFT; TShirtSide.SLEEVE_LEFT -> TShirtSide.BACK
        TShirtSide.BACK -> TShirtSide.SLEEVE_RIGHT; TShirtSide.SLEEVE_RIGHT -> TShirtSide.FRONT
    }

    private fun getQuadrantAngle(angle: Float): Float {
        val n = ((angle % 360f) + 360f) % 360f
        return when {
            n < 45f -> n; n < 90f -> n - 90f; n < 135f -> n - 90f; n < 180f -> n - 180f
            n < 225f -> n - 180f; n < 270f -> n - 270f; n < 315f -> n - 270f; else -> n - 360f
        }
    }

    private fun drawRotatedBitmap(canvas: Canvas, primary: Bitmap?, secondary: Bitmap?, blend: Float,
                                   rotY: Float, w: Float, h: Float) {
        val bmp = primary ?: return
        val padding = 40f
        val drawW = w - padding * 2; val drawH = h - padding * 2 - 60f

        // Scala per contenere l'immagine
        val scaleX = drawW / bmp.width; val scaleY = drawH / bmp.height
        val scale = minOf(scaleX, scaleY)
        val bmpW = bmp.width * scale; val bmpH = bmp.height * scale
        val left = (w - bmpW) / 2f; val top = (h - bmpH) / 2f - 20f

        // Applica trasformazione 3D
        camera.save()
        camera.rotateY(rotY * 0.6f) // Effetto prospettiva ridotto per look naturale
        camera.getMatrix(transformMatrix)
        camera.restore()

        // Centra la rotazione
        transformMatrix.preTranslate(-w / 2f, -h / 2f)
        transformMatrix.postTranslate(w / 2f, h / 2f)

        canvas.save()
        canvas.concat(transformMatrix)

        // Disegna bitmap principale
        val destRect = RectF(left, top, left + bmpW, top + bmpH)
        bitmapPaint.alpha = if (blend > 0f) ((1f - blend) * 255).toInt().coerceIn(0, 255) else 255
        canvas.drawBitmap(bmp, null, destRect, bitmapPaint)

        // Blend con il bitmap successivo durante la transizione
        if (secondary != null && blend > 0f) {
            bitmapPaint.alpha = (blend * 255).toInt().coerceIn(0, 255)
            canvas.drawBitmap(secondary, null, destRect, bitmapPaint)
            bitmapPaint.alpha = 255
        }

        canvas.restore()
    }

    private fun drawRotatedDesign(canvas: Canvas, design: Bitmap, rotY: Float, w: Float, h: Float, side: TShirtSide) {
        // Il design occupa un'area piu' piccola centrata sulla maglietta
        val (areaW, areaH) = when (side) {
            TShirtSide.FRONT, TShirtSide.BACK -> (w * 0.35f) to (h * 0.40f)
            else -> (w * 0.25f) to (h * 0.30f)
        }
        val left = (w - areaW) / 2f; val top = (h - areaH) / 2f - 20f

        camera.save()
        camera.rotateY(rotY * 0.6f)
        camera.getMatrix(transformMatrix)
        camera.restore()
        transformMatrix.preTranslate(-w / 2f, -h / 2f)
        transformMatrix.postTranslate(w / 2f, h / 2f)

        canvas.save()
        canvas.concat(transformMatrix)
        val destRect = RectF(left, top, left + areaW, top + areaH)
        canvas.drawBitmap(design, null, destRect, designPaint)
        canvas.restore()
    }

    private fun drawRotationIndicator(canvas: Canvas, angle: Float, w: Float, h: Float) {
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val y = h - 50f; val spacing = 18f; val startX = w / 2f - spacing * 1.5f
        TShirtSide.all.forEachIndexed { i, side ->
            val isActive = TShirtSide.fromAngle(angle) == side
            dotPaint.color = if (isActive) Color.argb(255, 37, 99, 235) else Color.argb(80, 128, 128, 128)
            canvas.drawCircle(startX + i * spacing, y, if (isActive) 5f else 3.5f, dotPaint)
        }
    }

    // ── Touch Handling ──
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                snapAnimator?.cancel()
                lastTouchX = event.x; isDragging = true
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val dx = event.x - lastTouchX
                    currentAngle += dx * sensitivity
                    lastTouchX = event.x
                    // Notifica cambio lato
                    val newSide = TShirtSide.fromAngle(currentAngle)
                    onSideChanged?.invoke(newSide)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                parent?.requestDisallowInterceptTouchEvent(false)
                // Snap al lato piu' vicino
                snapToNearestSide()
            }
        }
        return true
    }

    private fun snapToNearestSide() {
        val currentSide = TShirtSide.fromAngle(currentAngle)
        animateToAngle(currentSide.angle)
    }

    private fun animateToAngle(target: Float) {
        snapAnimator?.cancel()
        // Trova il percorso piu' breve (gestendo il wrap-around)
        val normalized = ((currentAngle % 360f) + 360f) % 360f
        var diff = target - normalized
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        val finalTarget = currentAngle + diff

        snapAnimator = ValueAnimator.ofFloat(currentAngle, finalTarget).apply {
            duration = 300L; interpolator = DecelerateInterpolator()
            addUpdateListener { a ->
                currentAngle = a.animatedValue as Float
                onSideChanged?.invoke(getCurrentSide())
                invalidate()
            }
            start()
        }
    }
}
