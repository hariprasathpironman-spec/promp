package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY timestamp DESC")
    fun getAllPrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoritePrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE isKeepNote = 1 ORDER BY isPinnedToKeep DESC, timestamp DESC")
    fun getKeepNotes(): Flow<List<PromptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptEntity): Long

    @Update
    suspend fun updatePrompt(prompt: PromptEntity)

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun deletePromptById(id: Int)

    @Query("DELETE FROM prompts")
    suspend fun clearHistory()

    @Query("UPDATE prompts SET isFavorite = 0")
    suspend fun clearAllFavorites()
}
