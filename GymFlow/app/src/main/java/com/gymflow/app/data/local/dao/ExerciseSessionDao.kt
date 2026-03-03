package com.gymflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymflow.app.data.local.entity.ExerciseSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSession(session: ExerciseSessionEntity)

    @Update
    suspend fun updateExerciseSession(session: ExerciseSessionEntity)

    @Delete
    suspend fun deleteExerciseSession(session: ExerciseSessionEntity)

    @Query("SELECT * FROM exercise_sessions WHERE id = :id")
    suspend fun getExerciseSessionById(id: Int): ExerciseSessionEntity?

    @Query("SELECT * FROM exercise_sessions WHERE workoutId = :workoutId ORDER BY createdAt DESC")
    fun getSessionsByWorkoutId(workoutId: Int): Flow<List<ExerciseSessionEntity>>

    @Query("SELECT * FROM exercise_sessions WHERE exerciseName = :exerciseName ORDER BY createdAt DESC")
    fun getSessionsByExerciseName(exerciseName: String): Flow<List<ExerciseSessionEntity>>

    @Query("SELECT * FROM exercise_sessions WHERE muscleGroup = :muscleGroup ORDER BY createdAt DESC")
    fun getSessionsByMuscleGroup(muscleGroup: String): Flow<List<ExerciseSessionEntity>>

    @Query("SELECT * FROM exercise_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<ExerciseSessionEntity>>

    @Query("SELECT * FROM exercise_sessions WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getSessionsBetweenDates(startTime: Long, endTime: Long): Flow<List<ExerciseSessionEntity>>

    @Query("SELECT AVG(formScore) FROM exercise_sessions WHERE exerciseName = :exerciseName")
    fun getAverageFormScoreForExercise(exerciseName: String): Flow<Float?>

    @Query("SELECT * FROM exercise_sessions WHERE isPersonalBest = 1 ORDER BY createdAt DESC")
    fun getPersonalBests(): Flow<List<ExerciseSessionEntity>>

    @Query("SELECT DISTINCT muscleGroup FROM exercise_sessions WHERE createdAt >= :startTime")
    fun getActiveMuscleGroupsSince(startTime: Long): Flow<List<String>>

    @Query("DELETE FROM exercise_sessions WHERE workoutId = :workoutId")
    suspend fun deleteSessionsByWorkoutId(workoutId: Int)

    @Query("DELETE FROM exercise_sessions")
    suspend fun deleteAllSessions()
}