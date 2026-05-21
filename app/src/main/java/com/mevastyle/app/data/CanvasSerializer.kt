package com.mevastyle.app.data
import android.content.Context; import android.graphics.Bitmap; import android.graphics.BitmapFactory
import com.google.gson.*; import com.google.gson.reflect.TypeToken
import com.mevastyle.app.editor.CanvasElement; import java.io.File; import java.io.FileOutputStream; import java.util.UUID

object CanvasSerializer {
    private data class SerEl(val type:String, val id:String, val x:Float, val y:Float, val scale:Float, val rotation:Float,
        val side:String="front", val text:String?=null, val fontFamily:String?=null, val color:Int?=null,
        val fontSize:Float?=null, val imageFile:String?=null, val hasTransparency:Boolean?=null)

    fun newProjectId(): String = UUID.randomUUID().toString().take(12)

    fun serialize(ctx: Context, elements: List<CanvasElement>, projectId: String): String {
        val dir = File(ctx.filesDir, "projects/$projectId"); dir.mkdirs()
        val list = elements.map { el -> when (el) {
            is CanvasElement.TextElement -> SerEl("text",el.id,el.x,el.y,el.scale,el.rotation,el.side.fileSuffix,el.text,el.fontFamily,el.color,el.fontSize)
            is CanvasElement.ImageElement -> {
                val fn = el.id + ".png"
                el.bitmap?.let { bmp -> try { FileOutputStream(File(dir,fn)).use { bmp.compress(Bitmap.CompressFormat.PNG,100,it) } } catch(_:Exception){} }
                SerEl("image",el.id,el.x,el.y,el.scale,el.rotation,el.side.fileSuffix,imageFile=fn,hasTransparency=el.hasTransparency)
            }
        }}; return Gson().toJson(list)
    }
    fun deserialize(ctx: Context, json: String, projectId: String): List<CanvasElement> {
        val dir = File(ctx.filesDir, "projects/$projectId")
        val list: List<SerEl> = try { Gson().fromJson(json, object:TypeToken<List<SerEl>>(){}.type) } catch(_:Exception){ emptyList() }
        return list.mapNotNull { s ->
            val side = TShirtSide.values().find { it.fileSuffix == s.side } ?: TShirtSide.FRONT
            when(s.type) {
                "text" -> CanvasElement.TextElement(s.x,s.y,s.scale,s.rotation,s.id,side,s.text?:"",s.fontFamily?:"sans-serif",s.color?:0xFF000000.toInt(),s.fontSize?:48f)
                "image" -> { val bmp = s.imageFile?.let { val f=File(dir,it); if(f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null }
                    bmp?.let { CanvasElement.ImageElement(s.x,s.y,s.scale,s.rotation,s.id,side,it,s.hasTransparency?:false) } }
                else -> null
            }
        }
    }
    fun deleteProjectFiles(ctx: Context, projectId: String) { File(ctx.filesDir,"projects/$projectId").takeIf{it.exists()}?.deleteRecursively() }
}
