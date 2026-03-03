package com.gymflow.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gymflow.app.ui.screen.CameraScreen
import com.gymflow.app.ui.screen.ChallengeScreen
import com.gymflow.app.ui.screen.MainScreen
import com.gymflow.app.ui.screen.ProfileSetupScreen
import com.gymflow.app.ui.screen.ProgressScreen
import com.gymflow.app.ui.screen.WorkoutPlanScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }
        composable(Screen.Camera.route) {
            CameraScreen(navController = navController)
        }
        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(navController = navController)
        }
        composable(Screen.WorkoutPlan.route) {
            WorkoutPlanScreen(navController = navController)
        }
        composable(Screen.Progress.route) {
            ProgressScreen(navController = navController)
        }
        composable(Screen.Challenge.route) {
            ChallengeScreen(navController = navController)
        }
    }
}