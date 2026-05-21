package com.mevastyle.app.editor

import android.content.Context; import android.graphics.*
import android.util.AttributeSet; import android.view.GestureDetector; import android.view.MotionEvent; import android.view.View
import com.mevastyle.app.data.DtfUtils; import com.mevastyle.app.data.TShirtSide; import com.mevastyle.app.data.SidePrintArea
import kotlin.math.atan2; import kotlin.math.sqrt

class EditorCanvasView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null) : View(ctx, attrs) {
    val elements = mutableListOf<CanvasElement>()
    var selectedElement: CanvasElement? = null
    var dtfEnabled = true; var showGrid = false; var shirtBitmap: Bitmap? = null
    var onSelectionChanged: ((CanvasElement?) -> Unit)? = null
    var onViewZoomChanged: ((Float) -> Unit)? = null

    var currentSide: TShirtSide = TShirtSide.FRONT
        set(v) { field = v; selectedElement = null; onSelectionChanged?.invoke(null); resetViewport() }

    private val currentElements get() = elements.filter { it.side == currentSide }
    private val canvasW get() = SidePrintArea.getArea(currentSide).first
    private val canvasH get() = SidePrintArea.getArea(currentSide).second

    private val dtfPaint = Paint().apply { colorFilter = ColorMatrixColorFilter(DtfUtils.getDtfColorMatrix()) }
    private val gridPaint = Paint().apply { color = Color.argb(40,128,128,128); strokeWidth = 1f; style = Paint.Style.STROKE }
    private val selPaint = Paint().apply { color = Color.argb(180,37,99,235); strokeWidth = 3f; style = Paint.Style.STROKE; pathEffect = DashPathEffect(floatArrayOf(10f,10f),0f) }
    private val txtPaint = Paint().apply { isAntiAlias = true }
    private val shirtPaint = Paint().apply { isAntiAlias = true; isFilterBitmap = true }

    private var vScale = 1f; private var vOffX = 0f; private var vOffY = 0f
    private var vpActive = false; private var vpLastMidX = 0f; private var vpLastMidY = 0f; private var vpLastDist = 0f
    private var offX = 0f; private var offY = 0f; private var dragging = false
    private var elPinching = false; private var initialDist = 0f; private var initialScale = 1f
    private var initialAngle = 0f; private var initialRotation = 0f

    private val gestureDetector = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (selectedElement == null && vScale > 1.01f) { vScale=1f; vOffX=0f; vOffY=0f; onViewZoomChanged?.invoke(1f); invalidate(); return true }; return false
        }
    })

    fun resetViewport() { vScale=1f; vOffX=0f; vOffY=0f; onViewZoomChanged?.invoke(1f); invalidate() }
    fun getViewScale() = vScale
    private fun dist(e:MotionEvent):Float { val dx=e.getX(0)-e.getX(1);val dy=e.getY(0)-e.getY(1);return sqrt(dx*dx+dy*dy) }
    private fun midX(e:MotionEvent)=(e.getX(0)+e.getX(1))/2f
    private fun midY(e:MotionEvent)=(e.getY(0)+e.getY(1))/2f
    private fun angle(e:MotionEvent):Float{val dx=e.getX(1)-e.getX(0);val dy=e.getY(1)-e.getY(0);return Math.toDegrees(atan2(dy.toDouble(),dx.toDouble())).toFloat()}
    private fun screenToCanvas(sx:Float,sy:Float):Pair<Float,Float>{val scX=width/canvasW;val scY=height/canvasH;return Pair((sx-vOffX)/(scX*vScale),(sy-vOffY)/(scY*vScale))}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas); val scX=width/canvasW; val scY=height/canvasH
        canvas.save(); canvas.translate(vOffX,vOffY); canvas.scale(scX*vScale,scY*vScale)
        shirtBitmap?.let { bmp ->
            val sw=canvasW*1.25f; val sh=sw*bmp.height/bmp.width; val sx=(canvasW-sw)/2; val sy=(canvasH-sh)/2
            canvas.drawBitmap(bmp,null,RectF(sx,sy,sx+sw,sy+sh),shirtPaint)
        }
        if(showGrid){ val gs=if(currentSide==TShirtSide.FRONT||currentSide==TShirtSide.BACK) 100f else 50f
            var gx=gs; while(gx<canvasW){canvas.drawLine(gx,0f,gx,canvasH,gridPaint);gx+=gs}
            var gy=gs; while(gy<canvasH){canvas.drawLine(0f,gy,canvasW,gy,gridPaint);gy+=gs}
        }
        for(el in currentElements){ val p=if(dtfEnabled) dtfPaint else null
            canvas.save(); val cx=el.x+el.getWidth()/2; val cy=el.y+el.getHeight()/2; canvas.rotate(el.rotation,cx,cy)
            when(el){ is CanvasElement.ImageElement -> el.bitmap?.let { bmp -> canvas.save();canvas.translate(el.x,el.y);canvas.scale(el.scale,el.scale);canvas.drawBitmap(bmp,0f,0f,p);canvas.restore() }
                is CanvasElement.TextElement -> { txtPaint.textSize=el.fontSize*el.scale; txtPaint.color=el.color
                    txtPaint.typeface=try{Typeface.create(el.fontFamily,Typeface.NORMAL)}catch(_:Exception){Typeface.DEFAULT}
                    canvas.drawText(el.text,el.x,el.y+el.fontSize*el.scale,if(dtfEnabled) Paint(txtPaint).apply{colorFilter=dtfPaint.colorFilter} else txtPaint)
                }
            }; canvas.restore()
            if(el==selectedElement){ canvas.save();canvas.rotate(el.rotation,cx,cy);canvas.drawRect(el.x-4,el.y-4,el.x+el.getWidth()+4,el.y+el.getHeight()+4,selPaint);canvas.restore() }
        }; canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        when(event.actionMasked){
            MotionEvent.ACTION_DOWN -> { val(cx,cy)=screenToCanvas(event.x,event.y)
                val hit=currentElements.lastOrNull{it.containsRotated(cx,cy)}
                if(hit!=null){selectedElement=hit;dragging=true;offX=cx-hit.x;offY=cy-hit.y} else{selectedElement=null;dragging=false}
                onSelectionChanged?.invoke(selectedElement); invalidate()
            }
            MotionEvent.ACTION_POINTER_DOWN -> { if(event.pointerCount==2){
                if(selectedElement!=null){elPinching=true;dragging=false;initialDist=dist(event);initialScale=selectedElement!!.scale;initialAngle=angle(event);initialRotation=selectedElement!!.rotation}
                else{vpActive=true;vpLastDist=dist(event);vpLastMidX=midX(event);vpLastMidY=midY(event)}
            }}
            MotionEvent.ACTION_MOVE -> {
                if(event.pointerCount==2&&elPinching&&selectedElement!=null){val d=dist(event);if(initialDist>10f)selectedElement!!.scale=initialScale*(d/initialDist);selectedElement!!.rotation=initialRotation+(angle(event)-initialAngle);invalidate()}
                else if(event.pointerCount==2&&vpActive){val d=dist(event);val mx=midX(event);val my=midY(event);if(vpLastDist>10f)vScale=(vScale*(d/vpLastDist)).coerceIn(0.5f,5f);vOffX+=mx-vpLastMidX;vOffY+=my-vpLastMidY;vpLastDist=d;vpLastMidX=mx;vpLastMidY=my;onViewZoomChanged?.invoke(vScale);invalidate()}
                else if(dragging&&selectedElement!=null){val(cx,cy)=screenToCanvas(event.x,event.y);selectedElement!!.x=cx-offX;selectedElement!!.y=cy-offY;invalidate()}
            }
            MotionEvent.ACTION_POINTER_UP -> {elPinching=false;vpActive=false}
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL -> {dragging=false;elPinching=false;vpActive=false}
        }; return true
    }

    fun exportDesignBitmap(side: TShirtSide = currentSide): Bitmap {
        val(w,h)=SidePrintArea.getArea(side); val bmp=Bitmap.createBitmap(w.toInt(),h.toInt(),Bitmap.Config.ARGB_8888); val c=Canvas(bmp)
        for(el in elements.filter{it.side==side}){ c.save(); val cx=el.x+el.getWidth()/2;val cy=el.y+el.getHeight()/2;c.rotate(el.rotation,cx,cy)
            when(el){ is CanvasElement.ImageElement->el.bitmap?.let{b->c.save();c.translate(el.x,el.y);c.scale(el.scale,el.scale);c.drawBitmap(b,0f,0f,null);c.restore()}
                is CanvasElement.TextElement->{val tp=Paint().apply{isAntiAlias=true;textSize=el.fontSize*el.scale;color=el.color;typeface=try{Typeface.create(el.fontFamily,Typeface.NORMAL)}catch(_:Exception){Typeface.DEFAULT}};c.drawText(el.text,el.x,el.y+el.fontSize*el.scale,tp)}
            };c.restore()
        }; return bmp
    }

    fun exportMockupBitmap(side: TShirtSide = currentSide, shirtBmp: Bitmap? = shirtBitmap): Bitmap {
        val(w,h)=SidePrintArea.getArea(side); val bmp=Bitmap.createBitmap(w.toInt(),h.toInt(),Bitmap.Config.ARGB_8888); val c=Canvas(bmp)
        shirtBmp?.let{val sw=w*1.25f;val sh=sw*it.height/it.width;val sx=(w-sw)/2;val sy=(h-sh)/2;c.drawBitmap(it,null,RectF(sx,sy,sx+sw,sy+sh),shirtPaint)}
        c.drawBitmap(exportDesignBitmap(side),0f,0f,if(dtfEnabled) dtfPaint else null); return bmp
    }
}
