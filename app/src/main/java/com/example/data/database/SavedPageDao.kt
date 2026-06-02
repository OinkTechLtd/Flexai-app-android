package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPageDao {
    @Query("SELECT * FROM saved_pages ORDER BY savedAt DESC")
    fun getAllSavedPages(): Flow<List<SavedPage>>

    @Query("SELECT * FROM saved_pages WHERE url = :url LIMIT 1")
    suspend fun getSavedPageByUrl(url: String): SavedPage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPage(page: SavedPage)

    @Query("DELETE FROM saved_pages WHERE id = :id")
    suspend fun deleteSavedPageById(id: Int)

    @Query("DELETE FROM saved_pages WHERE url = :url")
    suspend fun deleteSavedPageByUrl(url: String)
}
