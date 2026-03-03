package com.gymflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymflow.app.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Int): WorkoutEntity?

    @Query("SELECT * FROM workouts ORDER BY createdAt DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getPendingWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE exerciseType = :type ORDER BY createdAt DESC")
    fun getWorkoutsByType(type: String): Flow<List<WorkoutEntity>>

    @Query("SELECT AVG(formScore) FROM workouts WHERE isCompleted = 1")
    fun getAverageFormScore(): Flow<Float?>

    @Query("SELECT * FROM workouts WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun getWorkoutsBetweenDates(startTime: Long, endTime: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT COUNT(*) FROM workouts WHERE isCompleted = 1")
    fun getCompletedWorkoutCount(): Flow<Int>

    @Query("SELECT SUM(caloriesBurned) FROM workouts WHERE isCompleted = 1")
    fun getTotalCaloriesBurned(): Flow<Int?>

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()
}