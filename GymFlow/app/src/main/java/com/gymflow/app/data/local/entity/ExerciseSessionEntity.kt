package com.gymflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_sessions")
data class ExerciseSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val workoutId: Int,
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val weightKg: Float,
    val formScore: Float,
    val feedbackGiven: String,
    val muscleGroup: String,
    val durationSeconds: Int,
    val repsDone: Int = 0,
    val isPersonalBest: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)