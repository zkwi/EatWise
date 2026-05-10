package com.example.eatwise.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MealRecordDao {
    @Query("SELECT * FROM meal_records ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MealRecordEntity>>

    @Query("SELECT * FROM meal_records ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<MealRecordEntity>>

    @Query("SELECT * FROM meal_records WHERE id = :id")
    fun observeById(id: String): Flow<MealRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MealRecordEntity)

    @Query("DELETE FROM meal_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE meal_records SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())
}
