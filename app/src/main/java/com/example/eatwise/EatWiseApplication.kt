package com.example.eatwise

import android.app.Application
import com.example.eatwise.core.di.AppContainer

class EatWiseApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
