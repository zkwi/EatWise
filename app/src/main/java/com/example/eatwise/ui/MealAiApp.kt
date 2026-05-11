package com.example.eatwise.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eatwise.EatWiseApplication
import com.example.eatwise.core.i18n.AppLanguage
import com.example.eatwise.core.i18n.MealLanguageText
import com.example.eatwise.domain.model.AppSettings
import com.example.eatwise.ui.i18n.AppStrings
import com.example.eatwise.ui.i18n.LocalAppLanguage
import com.example.eatwise.ui.i18n.LocalAppStrings
import com.example.eatwise.ui.navigation.AppNavGraph
import com.example.eatwise.ui.theme.EatWiseTheme

@Composable
fun MealAiApp() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val container = (context.applicationContext as EatWiseApplication).appContainer
    val initialLanguage = remember(configuration) { AppLanguage.fromLocale(configuration.locales[0]) }
    val initialSettings = remember(initialLanguage) {
        AppSettings(
            language = initialLanguage,
            userGoal = MealLanguageText.defaultUserGoal(initialLanguage),
        )
    }
    val settings = container.settingsRepository.settings.collectAsStateWithLifecycle(initialSettings).value
    EatWiseTheme {
        CompositionLocalProvider(
            LocalAppLanguage provides settings.language,
            LocalAppStrings provides AppStrings.of(settings.language),
        ) {
            AppNavGraph(container)
        }
    }
}
