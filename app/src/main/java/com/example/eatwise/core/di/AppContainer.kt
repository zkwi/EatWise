package com.example.eatwise.core.di

import android.content.Context
import androidx.room.Room
import com.example.eatwise.core.network.OpenAiCompatibleClient
import com.example.eatwise.core.storage.ImageStorage
import com.example.eatwise.core.storage.SettingsStore
import com.example.eatwise.core.util.ImageCompressor
import com.example.eatwise.data.local.AppDatabase
import com.example.eatwise.data.repository.MealRepository
import com.example.eatwise.data.repository.SettingsRepository
import com.example.eatwise.domain.usecase.AnalyzeMealUseCase
import com.example.eatwise.domain.usecase.SaveMealRecordUseCase
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val settingsStore = SettingsStore(appContext)
    val settingsRepository = SettingsRepository(settingsStore)

    val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "eatwise.db",
    ).build()

    val mealRepository = MealRepository(database.mealRecordDao())
    val imageStorage = ImageStorage(appContext)
    val imageCompressor = ImageCompressor(appContext)
    val openAiClient = OpenAiCompatibleClient(okHttpClient)
    val analyzeMealUseCase = AnalyzeMealUseCase(settingsRepository, openAiClient, imageCompressor)
    val saveMealRecordUseCase = SaveMealRecordUseCase(mealRepository)
}
