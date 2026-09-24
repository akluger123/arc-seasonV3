package com.arcseason.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // FIXED: Required for 'by' state delegation
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Connecting the actual UI screens generated in Phase 3
import com.arcseason.app.ui.rules.RulesScreen
import com.arcseason.app.ui.schedule.ScheduleScreen
import com.arcseason.app.ui.macros.MacrosScreen
import com.arcseason.app.ui.coach.CoachScreen
import com.arcseason.app.ui.workout.WorkoutScreen

sealed class Screen(val route: String, val title: String) {
    object Rules : Screen("rules", "Rules")
    object Schedule : Screen("schedule", "Schedule")
    object Macros : Screen("macros", "Macros")
    object Coach : Screen("coach", "Coach")
    object Workout : Screen("workout", "Workout")
}

@Composable
fun ArcNavigation(
    navController: NavHostController = rememberNavController()
) {
    val items = listOf(
        Screen.Rules,
        Screen.Schedule,
        Screen.Macros,
        Screen.Coach,
        Screen.Workout
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                // FIXED: Replaced unresolved 'it' with 'screen.route'
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        label = { Text(screen.title) },
                        icon = { }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Rules.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Rules.route) {
                RulesScreen()
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen()
            }
            composable(Screen.Macros.route) {
                MacrosScreen()
            }
            composable(Screen.Coach.route) {
                CoachScreen()
            }
            composable(Screen.Workout.route) {
                WorkoutScreen()
            }
        }
    }
}
