package com.foqos.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.foqos.presentation.home.HomeScreen
import com.foqos.presentation.insights.InsightsScreen
import com.foqos.presentation.profile.ProfileEditScreen
import com.foqos.presentation.settings.SettingsScreen

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Insights : Screen("insights", "Insights", Icons.Filled.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object ProfileEdit : Screen("profile_edit/{profile_id}", "Edit Profile", Icons.Filled.Edit) {
        fun createRoute(profileId: String?) = if (profileId != null) {
            "profile_edit/$profileId"
        } else {
            "profile_edit/new"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoqosApp() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Insights, Screen.Settings)
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.Insights.route) { InsightsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(
                route = "profile_edit/{profile_id}",
                arguments = listOf(navArgument("profile_id") { type = NavType.StringType })
            ) {
                ProfileEditScreen(navController)
            }
        }
    }
}
