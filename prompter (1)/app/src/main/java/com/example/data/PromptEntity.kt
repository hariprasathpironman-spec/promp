package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rawIdea: String,
    val optimizedPrompt: String,
    val modelId: String,
    val modelName: String,
    val category: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val keepColorHex: String = "#FFFFFF",
    val isPinnedToKeep: Boolean = false,
    val isKeepNote: Boolean = false
)
