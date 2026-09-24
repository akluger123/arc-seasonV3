package com.arcseason.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arcseason.app.ui.coach.CoachScreen
import com.arcseason.app.ui.macros.MacrosScreen
import com.arcseason.app.ui.rules.RulesScreen
import com.arcseason.app.ui.schedule.ScheduleScreen
import com.arcseason.app.ui.theme.ArcOnSurfaceMuted
import com.arcseason.app.ui.theme.ArcSurface
import com.arcseason.app.ui.theme.NeonEmerald
import com.arcseason.app.ui.workout.WorkoutScreen

/** The 5 top-level destinations shown in the bottom navigation bar. */
enum class ArcDestination(val route: String, val label: String, val icon: ImageVector) {
    RULES("rules", "Rules", Icons.Filled.CheckCircle),
    SCHEDULE("schedule", "Schedule", Icons.Filled.Schedule),
    MACROS("macros", "Macros", Icons.Filled.Restaurant),
    WORKOUT("workout", "Workout", Icons.Filled.FitnessCenter),
    COACH("coach", "Coach", Icons.Filled.Forum)
}

@Composable
fun ArcBottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    NavigationBar(containerColor = ArcSurface, tonalElevation = 0.dp) {
        ArcDestination.entries.forEach { destination ->
            val selected = currentRoute?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonEmerald,
                    selectedTextColor = NeonEmerald,
                    indicatorColor = ArcSurface,
                    unselectedIconColor = ArcOnSurfaceMuted,
                    unselectedTextColor = ArcOnSurfaceMuted
                )
            )
        }
    }
}

@Composable
fun ArcNavHost(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = ArcDestination.RULES.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(ArcDestination.RULES.route) { RulesScreen() }
        composable(ArcDestination.SCHEDULE.route) { ScheduleScreen() }
        composable(ArcDestination.MACROS.route) { MacrosScreen() }
        composable(ArcDestination.WORKOUT.route) { WorkoutScreen() }
        composable(ArcDestination.COACH.route) { CoachScreen() }
    }
}

@Composable
fun rememberArcNavController(): NavHostController = rememberNavController()
