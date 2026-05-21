package com.mevastyle.app.data
import android.content.Context; import androidx.room.*

@Entity(tableName = "projects")
data class SavedProject(@PrimaryKey val id: String, val name: String, val shirtColor: String,
    val clothingType: String = "tshirt", val canvasJson: String, val updatedAt: Long)
@Dao interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC") suspend fun getAll(): List<SavedProject>
    @Query("SELECT * FROM projects WHERE id = :id") suspend fun getById(id: String): SavedProject?
    @Upsert suspend fun upsert(project: SavedProject)
    @Query("DELETE FROM projects WHERE id = :id") suspend fun delete(id: String)
}
@Database(entities = [SavedProject::class], version = 2, exportSchema = false)
abstract class ProjectDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    companion object {
        @Volatile private var INSTANCE: ProjectDatabase? = null
        fun get(ctx: Context): ProjectDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(ctx.applicationContext, ProjectDatabase::class.java, "mevastyle.db")
                .fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}
