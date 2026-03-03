package com.gymflow.app.domain.model

data class UserProfile(
    val id: Int = 0,
    val name: String = "",
    val age: Int = 0,
    val weight: Float = 0f,
    val height: Float = 0f,
    val goal: String = "",
    val fitnessLevel: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val bmi: Float
        get() {
            val heightInMeters = height / 100f
            return if (heightInMeters > 0) weight / (heightInMeters * heightInMeters) else 0f
        }

    val goalDisplayName: String
        get() = when (goal) {
            "weight_loss" -> "Mrsavljenje"
            "muscle_gain" -> "Izgradnja misica"
            "endurance" -> "Izdrzljivost"
            "flexibility" -> "Fleksibilnost"
            "general_fitness" -> "Opca kondicija"
            else -> goal
        }

    val fitnessLevelDisplayName: String
        get() = when (fitnessLevel) {
            "beginner" -> "Pocetnik"
            "intermediate" -> "Srednja razina"
            "advanced" -> "Napredni"
            else -> fitnessLevel
        }

    companion object {
        val GOALS = listOf(
            "weight_loss",
            "muscle_gain",
            "endurance",
            "flexibility",
            "general_fitness"
        )

        val FITNESS_LEVELS = listOf(
            "beginner",
            "intermediate",
            "advanced"
        )
    }
}