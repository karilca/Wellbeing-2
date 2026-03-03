package com.gymflow.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.app.data.local.AppDatabase
import com.gymflow.app.data.repository.WorkoutRepository
import com.gymflow.app.domain.model.Exercise
import com.gymflow.app.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val workoutRepository = WorkoutRepository(
        database.workoutDao(),
        database.exerciseSessionDao()
    )

    private val _workouts = MutableStateFlow<List<WorkoutPlan>>(emptyList())
    val workouts: StateFlow<List<WorkoutPlan>> = _workouts.asStateFlow()

    private val _completedWorkouts = MutableStateFlow<List<WorkoutPlan>>(emptyList())
    val completedWorkouts: StateFlow<List<WorkoutPlan>> = _completedWorkouts.asStateFlow()

    private val _averageFormScore = MutableStateFlow<Float?>(null)
    val averageFormScore: StateFlow<Float?> = _averageFormScore.asStateFlow()

    private val _totalCaloriesBurned = MutableStateFlow<Int?>(null)
    val totalCaloriesBurned: StateFlow<Int?> = _totalCaloriesBurned.asStateFlow()

    private val _completedWorkoutCount = MutableStateFlow(0)
    val completedWorkoutCount: StateFlow<Int> = _completedWorkoutCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeMuscleGroups = MutableStateFlow<List<String>>(emptyList())
    val activeMuscleGroups: StateFlow<List<String>> = _activeMuscleGroups.asStateFlow()

    init {
        loadWorkouts()
        loadStats()
        loadActiveMuscleGroups()
    }

    private fun loadWorkouts() {
        viewModelScope.launch {
            workoutRepository.getAllWorkouts().collect { workoutList ->
                _workouts.value = workoutList
            }
        }
        viewModelScope.launch {
            workoutRepository.getCompletedWorkouts().collect { completedList ->
                _completedWorkouts.value = completedList
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            workoutRepository.getAverageFormScore().collect { score ->
                _averageFormScore.value = score
            }
        }
        viewModelScope.launch {
            workoutRepository.getTotalCaloriesBurned().collect { calories ->
                _totalCaloriesBurned.value = calories
            }
        }
        viewModelScope.launch {
            workoutRepository.getCompletedWorkoutCount().collect { count ->
                _completedWorkoutCount.value = count
            }
        }
    }

    private fun loadActiveMuscleGroups() {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        viewModelScope.launch {
            workoutRepository.getActiveMuscleGroupsSince(sevenDaysAgo).collect { muscles ->
                _activeMuscleGroups.value = muscles
            }
        }
    }

    fun saveWorkout(workoutPlan: WorkoutPlan) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                workoutRepository.saveWorkout(workoutPlan)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun completeWorkout(workoutPlan: WorkoutPlan) {
        viewModelScope.launch {
            try {
                val completedWorkout = workoutPlan.copy(
                    isCompleted = true,
                    completedAt = System.currentTimeMillis()
                )
                workoutRepository.updateWorkout(completedWorkout)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteWorkout(workoutPlan: WorkoutPlan) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkout(workoutPlan)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveExerciseSession(exercise: Exercise, workoutId: Int) {
        viewModelScope.launch {
            try {
                workoutRepository.saveExerciseSession(exercise, workoutId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun generateDefaultWorkouts() {
        viewModelScope.launch {
            try {
                val defaultWorkouts = listOf(
                    WorkoutPlan(
                        name = "Trening snage - Gornji dio",
                        description = "Vjezbe za gornji dio tijela",
                        exerciseType = "strength",
                        durationMinutes = 60,
                        musclesWorked = listOf("chest", "back", "shoulders", "biceps", "triceps")
                    ),
                    WorkoutPlan(
                        name = "Trening snage - Donji dio",
                        description = "Vjezbe za donji dio tijela",
                        exerciseType = "strength",
                        durationMinutes = 55,
                        musclesWorked = listOf("quadriceps", "hamstrings", "glutes", "calves")
                    ),
                    WorkoutPlan(
                        name = "Kardio i core",
                        description = "Kardiovaskularni trening i vjezbe za trbuh",
                        exerciseType = "cardio",
                        durationMinutes = 40,
                        musclesWorked = listOf("core", "full_body")
                    )
                )
                defaultWorkouts.forEach { workout ->
                    workoutRepository.saveWorkout(workout)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}