package com.gymflow.app.data.repository

import com.gymflow.app.data.local.dao.ExerciseSessionDao
import com.gymflow.app.data.local.dao.WorkoutDao
import com.gymflow.app.data.local.entity.ExerciseSessionEntity
import com.gymflow.app.data.local.entity.WorkoutEntity
import com.gymflow.app.domain.model.Exercise
import com.gymflow.app.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkoutRepository(
    private val workoutDao: WorkoutDao,
    private val exerciseSessionDao: ExerciseSessionDao
) {

    fun getAllWorkouts(): Flow<List<WorkoutPlan>> {
        return workoutDao.getAllWorkouts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getCompletedWorkouts(): Flow<List<WorkoutPlan>> {
        return workoutDao.getCompletedWorkouts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getPendingWorkouts(): Flow<List<WorkoutPlan>> {
        return workoutDao.getPendingWorkouts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getAverageFormScore(): Flow<Float?> {
        return workoutDao.getAverageFormScore()
    }

    fun getCompletedWorkoutCount(): Flow<Int> {
        return workoutDao.getCompletedWorkoutCount()
    }

    fun getTotalCaloriesBurned(): Flow<Int?> {
        return workoutDao.getTotalCaloriesBurned()
    }

    fun getActiveMuscleGroupsSince(startTime: Long): Flow<List<String>> {
        return exerciseSessionDao.getActiveMuscleGroupsSince(startTime)
    }

    suspend fun saveWorkout(workoutPlan: WorkoutPlan) {
        workoutDao.insertWorkout(workoutPlan.toEntity())
    }

    suspend fun updateWorkout(workoutPlan: WorkoutPlan) {
        workoutDao.updateWorkout(workoutPlan.toEntity())
    }

    suspend fun deleteWorkout(workoutPlan: WorkoutPlan) {
        workoutDao.deleteWorkout(workoutPlan.toEntity())
    }

    suspend fun saveExerciseSession(exercise: Exercise, workoutId: Int) {
        exerciseSessionDao.insertExerciseSession(exercise.toEntity(workoutId))
    }

    fun getSessionsByWorkoutId(workoutId: Int): Flow<List<Exercise>> {
        return exerciseSessionDao.getSessionsByWorkoutId(workoutId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getWorkoutsBetweenDates(startTime: Long, endTime: Long): Flow<List<WorkoutPlan>> {
        return workoutDao.getWorkoutsBetweenDates(startTime, endTime).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun WorkoutEntity.toDomain(): WorkoutPlan {
        return WorkoutPlan(
            id = id,
            name = name,
            description = description,
            exerciseType = exerciseType,
            durationMinutes = durationMinutes,
            caloriesBurned = caloriesBurned,
            formScore = formScore,
            musclesWorked = musclesWorked.split(","),
            notes = notes,
            isCompleted = isCompleted,
            createdAt = createdAt,
            completedAt = completedAt
        )
    }

    private fun WorkoutPlan.toEntity(): WorkoutEntity {
        return WorkoutEntity(
            id = id,
            name = name,
            description = description,
            exerciseType = exerciseType,
            durationMinutes = durationMinutes,
            caloriesBurned = caloriesBurned,
            formScore = formScore,
            musclesWorked = musclesWorked.joinToString(","),
            notes = notes,
            isCompleted = isCompleted,
            createdAt = createdAt,
            completedAt = completedAt
        )
    }

    private fun ExerciseSessionEntity.toDomain(): Exercise {
        return Exercise(
            id = id,
            name = exerciseName,
            sets = sets,
            reps = reps,
            weightKg = weightKg,
            formScore = formScore,
            muscleGroup = muscleGroup,
            durationSeconds = durationSeconds
        )
    }

    private fun Exercise.toEntity(workoutId: Int): ExerciseSessionEntity {
        return ExerciseSessionEntity(
            id = id,
            workoutId = workoutId,
            exerciseName = name,
            sets = sets,
            reps = reps,
            weightKg = weightKg,
            formScore = formScore,
            feedbackGiven = "",
            muscleGroup = muscleGroup,
            durationSeconds = durationSeconds
        )
    }
}