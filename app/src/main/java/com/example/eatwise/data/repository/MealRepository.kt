package com.example.eatwise.data.repository

import com.example.eatwise.core.util.JsonUtils
import com.example.eatwise.data.local.MealRecordDao
import com.example.eatwise.data.local.MealRecordEntity
import com.example.eatwise.domain.model.Ingredient
import com.example.eatwise.domain.model.MealAnalysisResult
import com.example.eatwise.domain.model.MealRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MealRepository(
    private val dao: MealRecordDao,
) {
    fun observeAll(): Flow<List<MealRecord>> = dao.observeAll().map { records -> records.map { it.toDomain() } }

    fun observeRecent(limit: Int): Flow<List<MealRecord>> =
        dao.observeRecent(limit).map { records -> records.map { it.toDomain() } }

    fun observeById(id: String): Flow<MealRecord?> = dao.observeById(id).map { it?.toDomain() }

    suspend fun saveAnalysis(
        result: MealAnalysisResult,
        imagePath: String,
        compressedPath: String?,
        aiResultJson: String,
        userGoalSnapshot: String,
    ): String {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        dao.insert(
            MealRecordEntity(
                id = id,
                mealName = result.mealName,
                summary = result.summary,
                imagePath = imagePath,
                thumbnailPath = compressedPath,
                eatingAdvice = result.eatingAdvice,
                goalMatchLevel = result.goalMatch.level,
                goalMatchReason = result.goalMatch.reason,
                tagsJson = JsonUtils.encode(result.tags),
                suggestionsJson = JsonUtils.encode(result.suggestions),
                ingredientsJson = JsonUtils.encode(result.ingredients),
                aiResultJson = aiResultJson,
                userGoalSnapshot = userGoalSnapshot,
                note = null,
                isFavorite = false,
                createdAt = now,
                updatedAt = now,
            ),
        )
        return id
    }

    suspend fun setFavorite(id: String, favorite: Boolean) = dao.setFavorite(id, favorite)

    suspend fun deleteById(id: String) = dao.deleteById(id)

    private fun MealRecordEntity.toDomain(): MealRecord = MealRecord(
        id = id,
        mealName = mealName,
        summary = summary,
        imagePath = imagePath,
        thumbnailPath = thumbnailPath,
        eatingAdvice = eatingAdvice,
        goalMatchLevel = goalMatchLevel,
        goalMatchReason = goalMatchReason,
        tags = JsonUtils.decodeList(tagsJson),
        suggestions = JsonUtils.decodeList(suggestionsJson),
        ingredients = JsonUtils.decodeList<Ingredient>(ingredientsJson),
        aiResultJson = aiResultJson,
        userGoalSnapshot = userGoalSnapshot,
        note = note,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
