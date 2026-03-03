package com.gymflow.app.navigation

sealed class Screen(val route: String) {

    object Main : Screen("main")
    object Camera : Screen("camera")
    object ProfileSetup : Screen("profile_setup")
    object WorkoutPlan : Screen("workout_plan")
    object Progress : Screen("progress")
    object Challenge : Screen("challenge")

    companion object {
        fun fromRoute(route: String?): Screen {
            return when (route) {
                Main.route -> Main
                Camera.route -> Camera
                ProfileSetup.route -> ProfileSetup
                WorkoutPlan.route -> WorkoutPlan
                Progress.route -> Progress
                Challenge.route -> Challenge
                else -> Main
            }
        }
    }
}