package com.mevastyle.app.data
import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth; import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query; import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await; import java.io.ByteArrayOutputStream; import java.text.SimpleDateFormat; import java.util.*

data class UserCreation(val id:String="", val userId:String="", val userEmail:String="", val clothingType:String="tshirt",
    val colorName:String="", val colorLabel:String="", val createdAt:Long=0L,
    val sides:Map<String,String> = emptyMap(), val mockupUrl:String="", val designUrl:String="")

object FirebaseManager {
    private val storage = FirebaseStorage.getInstance(); private val db = FirebaseFirestore.getInstance(); private val auth = FirebaseAuth.getInstance()

    suspend fun uploadBitmap(bmp: Bitmap, path: String, quality: Int = 95): String? = try {
        val baos = ByteArrayOutputStream(); bmp.compress(Bitmap.CompressFormat.PNG, quality, baos)
        val ref = storage.reference.child(path); ref.putBytes(baos.toByteArray()).await(); ref.downloadUrl.await().toString()
    } catch (e: Exception) { null }

    suspend fun saveCreation(sideBitmaps: Map<TShirtSide, Bitmap>, mockupBitmap: Bitmap?, designBitmap: Bitmap?, shirtColor: TShirtColor): Boolean {
        val user = auth.currentUser ?: return false
        val ts = System.currentTimeMillis(); val ds = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(ts))
        val bp = "creations/\${user.uid}/$ds"
        try {
            val sideUrls = mutableMapOf<String,String>()
            for ((side, bmp) in sideBitmaps) { uploadBitmap(bmp, "$bp/\${side.fileSuffix}.png")?.let { sideUrls[side.fileSuffix] = it } }
            val mu = mockupBitmap?.let { uploadBitmap(it, "$bp/mockup.png") } ?: ""
            val du = designBitmap?.let { uploadBitmap(it, "$bp/design.png") } ?: ""
            db.collection("creations").add(hashMapOf("userId" to user.uid, "userEmail" to (user.email?:""),
                "clothingType" to "tshirt", "colorName" to shirtColor.name, "colorLabel" to shirtColor.label,
                "createdAt" to ts, "sides" to sideUrls, "mockupUrl" to mu, "designUrl" to du)).await()
            return true
        } catch (_: Exception) { return false }
    }

    suspend fun getUserCreations(limit: Long = 50): List<UserCreation> {
        val user = auth.currentUser ?: return emptyList()
        return try { db.collection("creations").whereEqualTo("userId", user.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING).limit(limit).get().await()
            .documents.map { d -> UserCreation(d.id, d.getString("userId")?:"", d.getString("userEmail")?:"",
                d.getString("clothingType")?:"tshirt", d.getString("colorName")?:"", d.getString("colorLabel")?:"",
                d.getLong("createdAt")?:0L, (d.get("sides") as? Map<String,String>)?: emptyMap(),
                d.getString("mockupUrl")?:"", d.getString("designUrl")?:"") }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun getColors(typeId: String = "tshirt"): List<TShirtColor> = try {
        db.collection("clothing_types").document(typeId).collection("colors").get().await()
            .documents.mapNotNull { d -> try { TShirtColor(d.getString("name")!!, d.getLong("hex")!!, d.getString("label")?:"", typeId) } catch(_:Exception){null} }
    } catch (_: Exception) { emptyList() }

    suspend fun getClothingTypes(): List<ClothingType> = try {
        db.collection("clothing_types").orderBy("order").get().await()
            .documents.mapNotNull { d -> try { ClothingType(d.id, d.getString("name")?:"", d.getString("label")?:"",
                (d.get("sides") as? List<String>)?:listOf("front","back"), d.getLong("order")?.toInt()?:0) } catch(_:Exception){null} }
    } catch (_: Exception) { emptyList() }

    suspend fun addColor(typeId: String, c: TShirtColor): Boolean = try {
        db.collection("clothing_types").document(typeId).collection("colors").document(c.name)
            .set(hashMapOf("name" to c.name, "hex" to c.hex, "label" to c.label)).await(); true
    } catch (_: Exception) { false }
    suspend fun removeColor(typeId: String, name: String): Boolean = try {
        db.collection("clothing_types").document(typeId).collection("colors").document(name).delete().await(); true
    } catch (_: Exception) { false }
    suspend fun addClothingType(t: ClothingType): Boolean = try {
        db.collection("clothing_types").document(t.id).set(hashMapOf("name" to t.name, "label" to t.label, "sides" to t.sides, "order" to t.order)).await(); true
    } catch (_: Exception) { false }
}
