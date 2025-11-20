package com.example.dataloft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dataloft.feature.load.LoadTimerRoute
import com.example.dataloft.feature.workout.WorkoutRoute
import com.example.dataloft.navigation.AppDestinations
import com.example.dataloft.ui.theme.DataloftTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DataloftTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DataloftApp()
                }
            }
        }
    }
}

@Composable
fun DataloftApp() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppDestinations.LOAD
    ) {
        composable(AppDestinations.LOAD) {
            LoadTimerRoute(navController = navController)
        }
        composable(
            route = AppDestinations.WORKOUT,
            arguments = listOf(navArgument("timerId") { type = NavType.IntType })
        ) { entry ->
            val timerId = entry.arguments?.getInt("timerId")
            WorkoutRoute(timerId = timerId)
        }
    }
}
