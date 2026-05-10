package com.example.eatwise.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.eatwise.EatWiseApplication
import com.example.eatwise.ui.navigation.AppNavGraph
import com.example.eatwise.ui.theme.EatWiseTheme

@Composable
fun MealAiApp() {
    val container = (LocalContext.current.applicationContext as EatWiseApplication).appContainer
    EatWiseTheme {
        AppNavGraph(container)
    }
}
