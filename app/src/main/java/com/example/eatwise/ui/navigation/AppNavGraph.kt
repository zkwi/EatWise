package com.example.eatwise.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.eatwise.core.di.AppContainer
import com.example.eatwise.ui.theme.GreenSoft
import com.example.eatwise.ui.analysis.AnalysisScreen
import com.example.eatwise.ui.analysis.AnalysisViewModel
import com.example.eatwise.ui.camera.CameraScreen
import com.example.eatwise.ui.detail.MealDetailScreen
import com.example.eatwise.ui.detail.MealDetailViewModel
import com.example.eatwise.ui.history.HistoryScreen
import com.example.eatwise.ui.history.HistoryViewModel
import com.example.eatwise.ui.home.HomeScreen
import com.example.eatwise.ui.home.HomeViewModel
import com.example.eatwise.ui.settings.SettingsScreen
import com.example.eatwise.ui.settings.SettingsViewModel
import com.example.eatwise.ui.util.simpleFactory

@Composable
fun AppNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in setOf(Routes.Home, Routes.History, Routes.Settings)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .height(86.dp)
                        .shadow(8.dp, RoundedCornerShape(34.dp))
                        .clip(RoundedCornerShape(34.dp)),
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                ) {
                    listOf(
                        BottomItem(Routes.Home, "首页", Icons.Rounded.Home),
                        BottomItem(Routes.History, "记录", Icons.Rounded.History),
                        BottomItem(Routes.Settings, "设置", Icons.Rounded.Settings),
                    ).forEach { item ->
                        val selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(if (selected) 30.dp else 28.dp),
                                )
                            },
                            label = { Text(item.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = GreenSoft,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController = navController, startDestination = Routes.Home, modifier = Modifier.padding(padding)) {
            composable(Routes.Home) {
                val vm: HomeViewModel = viewModel(
                    factory = simpleFactory { HomeViewModel(container.mealRepository, container.imageStorage) },
                )
                HomeScreen(
                    viewModel = vm,
                    onOpenSettings = { navController.navigate(Routes.Settings) },
                    onOpenHistory = { navController.navigate(Routes.History) },
                    onOpenCamera = { navController.navigate(Routes.Camera) },
                    onAnalyze = { navController.navigate(Routes.analysis(it)) },
                    onOpenDetail = { navController.navigate(Routes.detail(it)) },
                )
            }
            composable(Routes.History) {
                val vm: HistoryViewModel = viewModel(
                    factory = simpleFactory { HistoryViewModel(container.mealRepository) },
                )
                HistoryScreen(vm, onOpenDetail = { navController.navigate(Routes.detail(it)) })
            }
            composable(Routes.Settings) {
                val vm: SettingsViewModel = viewModel(
                    factory = simpleFactory { SettingsViewModel(container.settingsRepository, container.openAiClient) },
                )
                SettingsScreen(vm)
            }
            composable(Routes.Camera) {
                CameraScreen(
                    imageStorage = container.imageStorage,
                    onBack = { navController.popBackStack() },
                    onImageReady = { navController.navigate(Routes.analysis(it)) { popUpTo(Routes.Home) } },
                )
            }
            composable(Routes.Analysis) { entry ->
                val imagePath = Uri.decode(entry.arguments?.getString("imagePath").orEmpty())
                val vm: AnalysisViewModel = viewModel(
                    key = imagePath,
                    factory = simpleFactory {
                        AnalysisViewModel(imagePath, container.analyzeMealUseCase, container.saveMealRecordUseCase)
                    },
                )
                AnalysisScreen(vm, onBack = { navController.popBackStack() }, onSaved = { navController.navigate(Routes.detail(it)) })
            }
            composable(Routes.Detail) { entry ->
                val recordId = Uri.decode(entry.arguments?.getString("recordId").orEmpty())
                val vm: MealDetailViewModel = viewModel(
                    key = recordId,
                    factory = simpleFactory { MealDetailViewModel(recordId, container.mealRepository) },
                )
                MealDetailScreen(vm, onBack = { navController.popBackStack() }, onDeleted = { navController.popBackStack() })
            }
        }
    }
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
