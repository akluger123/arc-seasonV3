package com.arcseason.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arcseason.app.ui.navigation.ArcBottomNavBar
import com.arcseason.app.ui.navigation.ArcNavHost
import com.arcseason.app.ui.navigation.rememberArcNavController
import com.arcseason.app.ui.theme.ArcBackground
import com.arcseason.app.ui.theme.ArcSeasonTheme

/**
 * Single activity, Compose-only. All 5 tabs (Rules, Schedule, Macros,
 * Workout, Coach) live behind [ArcBottomNavBar] / [ArcNavHost] — see
 * ui/navigation/ArcNavigation.kt for the destinations themselves.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArcSeasonTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = ArcBackground) {
                    ArcSeasonApp()
                }
            }
        }
    }
}

@Composable
private fun ArcSeasonApp() {
    val navController = rememberArcNavController()
    Scaffold(
        containerColor = ArcBackground,
        bottomBar = { ArcBottomNavBar(navController) }
    ) { innerPadding ->
        ArcNavHost(navController = navController, innerPadding = innerPadding)
    }
}
