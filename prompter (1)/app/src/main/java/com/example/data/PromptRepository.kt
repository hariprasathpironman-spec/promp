package com.example.data

import kotlinx.coroutines.flow.Flow

class PromptRepository(private val promptDao: PromptDao) {
    val allPrompts: Flow<List<PromptEntity>> = promptDao.getAllPrompts()
    val favoritePrompts: Flow<List<PromptEntity>> = promptDao.getFavoritePrompts()
    val keepPrompts: Flow<List<PromptEntity>> = promptDao.getKeepNotes()

    suspend fun insertPrompt(prompt: PromptEntity): Long = promptDao.insertPrompt(prompt)

    suspend fun updatePrompt(prompt: PromptEntity) = promptDao.updatePrompt(prompt)

    suspend fun deletePromptById(id: Int) = promptDao.deletePromptById(id)

    suspend fun clearHistory() = promptDao.clearHistory()

    suspend fun clearAllFavorites() = promptDao.clearAllFavorites()
}
