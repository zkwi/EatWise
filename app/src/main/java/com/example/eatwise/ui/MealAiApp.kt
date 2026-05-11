package com.example.eatwise.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eatwise.EatWiseApplication
import com.example.eatwise.domain.model.AppSettings
import com.example.eatwise.ui.i18n.AppStrings
import com.example.eatwise.ui.i18n.LocalAppLanguage
import com.example.eatwise.ui.i18n.LocalAppStrings
import com.example.eatwise.ui.navigation.AppNavGraph
import com.example.eatwise.ui.theme.EatWiseTheme

@Composable
fun MealAiApp() {
    val container = (LocalContext.current.applicationContext as EatWiseApplication).appContainer
    val settings = container.settingsRepository.settings.collectAsStateWithLifecycle(AppSettings()).value
    EatWiseTheme {
        CompositionLocalProvider(
            LocalAppLanguage provides settings.language,
            LocalAppStrings provides AppStrings.of(settings.language),
        ) {
            AppNavGraph(container)
        }
    }
}
