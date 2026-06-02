package com.example.data.repository

import com.example.data.database.SavedPage
import com.example.data.database.SavedPageDao
import kotlinx.coroutines.flow.Flow

class SavedPageRepository(private val savedPageDao: SavedPageDao) {
    val allSavedPages: Flow<List<SavedPage>> = savedPageDao.getAllSavedPages()

    suspend fun getSavedPageByUrl(url: String): SavedPage? {
        return savedPageDao.getSavedPageByUrl(url)
    }

    suspend fun insert(page: SavedPage) {
        savedPageDao.insertSavedPage(page)
    }

    suspend fun deleteById(id: Int) {
        savedPageDao.deleteSavedPageById(id)
    }

    suspend fun deleteByUrl(url: String) {
        savedPageDao.deleteSavedPageByUrl(url)
    }
}
