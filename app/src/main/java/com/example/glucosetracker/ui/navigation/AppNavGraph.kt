package com.example.glucosetracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.glucosetracker.ui.components.AppBottomBar
import com.example.glucosetracker.ui.components.BottomBarItem
import com.example.glucosetracker.ui.screens.HistoryScreen
import com.example.glucosetracker.ui.screens.HomeScreen
import com.example.glucosetracker.ui.screens.ProfileScreen
import com.example.glucosetracker.ui.screens.ReportsScreen
import com.example.glucosetracker.ui.theme.AppColors
import com.example.glucosetracker.viewmodel.HomeViewModel

private object AppRoute {
    const val Home = "home"
    const val History = "history"
    const val Reports = "reports"
    const val Profile = "profile"
}

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel()
    val bottomItems = remember {
        listOf(
            BottomBarItem(route = AppRoute.Home, label = "Главная", icon = "●"),
            BottomBarItem(route = AppRoute.History, label = "История", icon = "●"),
            BottomBarItem(route = AppRoute.Reports, label = "Отчёты", icon = "●"),
            BottomBarItem(route = AppRoute.Profile, label = "Профиль", icon = "●")
        )
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AppColors.Background,
        bottomBar = {
            AppBottomBar(
                items = bottomItems,
                currentRoute = currentRoute,
                onItemClick = { item ->
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(AppRoute.Home) {
                HomeScreen(viewModel = homeViewModel, contentPadding = innerPadding)
            }
            composable(AppRoute.History) {
                HistoryScreen(viewModel = homeViewModel, contentPadding = innerPadding)
            }
            composable(AppRoute.Reports) {
                ReportsScreen(viewModel = homeViewModel, contentPadding = innerPadding)
            }
            composable(AppRoute.Profile) {
                ProfileScreen(viewModel = homeViewModel, contentPadding = innerPadding)
            }
        }
    }
}