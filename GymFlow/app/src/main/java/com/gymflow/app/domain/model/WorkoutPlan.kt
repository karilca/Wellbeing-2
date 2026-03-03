package com.gymflow.app.domain.model

data class WorkoutPlan(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val exerciseType: String = "",
    val durationMinutes: Int = 0,
    val caloriesBurned: Int = 0,
    val formScore: Float = 0f,
    val musclesWorked: List<String> = emptyList(),
    val notes: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val exercises: List<Exercise> = emptyList()
) {
    val formScorePercentage: Int
        get() = (formScore * 100).toInt()

    val durationDisplay: String
        get() = if (durationMinutes < 60) {
            "$durationMinutes min"
        } else {
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            if (minutes == 0) "${hours}h" else "${hours}h ${minutes}min"
        }

    val musclesWorkedDisplay: String
        get() = musclesWorked.joinToString(", ")

    companion object {
        fun generatePlan(userProfile: UserProfile): List<WorkoutPlan> {
            return when (userProfile.goal) {
                "weight_loss" -> listOf(
                    WorkoutPlan(
                        name = "Kardio trening",
                        description = "Trening visokog intenziteta za sagorijevanje kalorija",
                        exerciseType = "cardio",
                        durationMinutes = 45,
                        musclesWorked = listOf("legs", "core", "full_body")
                    ),
                    WorkoutPlan(
                        name = "HIIT trening",
                        description = "Intervalni trening visokog intenziteta",
                        exerciseType = "hiit",
                        durationMinutes = 30,
                        musclesWorked = listOf("full_body")
                    )
                )
                "muscle_gain" -> listOf(
                    WorkoutPlan(
                        name = "Trening grudi i tricepsa",
                        description = "Jacanje misica gornjeg dijela tijela",
                        exerciseType = "strength",
                        durationMinutes = 60,
                        musclesWorked = listOf("chest", "triceps", "shoulders")
                    ),
                    WorkoutPlan(
                        name = "Trening leda i bicepsa",
                        description = "Jacanje misica leda",
                        exerciseType = "strength",
                        durationMinutes = 60,
                        musclesWorked = listOf("back", "biceps")
                    ),
                    WorkoutPlan(
                        name = "Trening nogu",
                        description = "Jacanje misica nogu",
                        exerciseType = "strength",
                        durationMinutes = 60,
                        musclesWorked = listOf("quadriceps", "hamstrings", "glutes", "calves")
                    )
                )
                else -> listOf(
                    WorkoutPlan(
                        name = "Opci fitness trening",
                        description = "Uravnotezeni trening cijelog tijela",
                        exerciseType = "general",
                        durationMinutes = 45,
                        musclesWorked = listOf("full_body")
                    )
                )
            }
        }
    }
}