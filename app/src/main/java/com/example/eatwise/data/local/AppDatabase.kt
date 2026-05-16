package com.example.eatwise.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MealRecordEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealRecordDao(): MealRecordDao
}
