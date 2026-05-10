package com.example.eatwise.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_records")
data class MealRecordEntity(
    @PrimaryKey val id: String,
    val mealName: String,
    val summary: String,
    val imagePath: String,
    val thumbnailPath: String?,
    val totalKcal: Double?,
    val proteinG: Double?,
    val carbsG: Double?,
    val fatG: Double?,
    val goalMatchLevel: String?,
    val goalMatchScore: Int?,
    val goalMatchReason: String?,
    val tagsJson: String,
    val suggestionsJson: String,
    val ingredientsJson: String,
    val aiResultJson: String,
    val userGoalSnapshot: String,
    val note: String?,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)
