package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_pages")
data class SavedPage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val title: String,
    val htmlContent: String,
    val savedAt: Long = System.currentTimeMillis()
)
