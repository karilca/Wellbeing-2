package com.gymflow.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.app.data.local.AppDatabase
import com.gymflow.app.data.repository.WorkoutRepository
import com.gymflow.app.domain.model.WorkoutPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val workoutRepository = WorkoutRepository(
        database.workoutDao(),
        database.exerciseSessionDao()
    )

    private val _completedWorkouts = MutableStateFlow<List<WorkoutPlan>>(emptyList())
    val completedWorkouts: StateFlow<List<WorkoutPlan>> = _completedWorkouts.asStateFlow()

    private val _averageFormScore = MutableStateFlow(0f)
    val averageFormScore: StateFlow<Float> = _averageFormScore.asStateFlow()

    private val _totalCaloriesBurned = MutableStateFlow(0)
    val totalCaloriesBurned: StateFlow<Int> = _totalCaloriesBurned.asStateFlow()

    private val _completedWorkoutCount = MutableStateFlow(0)
    val completedWorkoutCount: StateFlow<Int> = _completedWorkoutCount.asStateFlow()

    private val _activeMuscleGroups = MutableStateFlow<Map<String, Float>>(emptyMap())
    val activeMuscleGroups: StateFlow<Map<String, Float>> = _activeMuscleGroups.asStateFlow()

    private val _weeklyWorkouts = MutableStateFlow<List<WorkoutPlan>>(emptyList())
    val weeklyWorkouts: StateFlow<List<WorkoutPlan>> = _weeklyWorkouts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProgressData()
    }

    private fun loadProgressData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                loadCompletedWorkouts()
                loadStats()
                loadWeeklyWorkouts()
                loadActiveMuscleGroups()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadCompletedWorkouts() {
        viewModelScope.launch {
            workoutRepository.getCompletedWorkouts().collect { workouts ->
                _completedWorkouts.value = workouts
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            workoutRepository.getAverageFormScore().collect { score ->
                _averageFormScore.value = score ?: 0f
            }
        }
        viewModelScope.launch {
            workoutRepository.getTotalCaloriesBurned().collect { calories ->
                _totalCaloriesBurned.value = calories ?: 0
            }
        }
        viewModelScope.launch {
            workoutRepository.getCompletedWorkoutCount().collect { count ->
                _completedWorkoutCount.value = count
            }
        }
    }

    private fun loadWeeklyWorkouts() {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            workoutRepository.getWorkoutsBetweenDates(sevenDaysAgo, now).collect { workouts ->
                _weeklyWorkouts.value = workouts
            }
        }
    }

    private fun loadActiveMuscleGroups() {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        viewModelScope.launch {
            workoutRepository.getActiveMuscleGroupsSince(sevenDaysAgo).collect { muscles ->
                val muscleMap = muscles.associateWith { muscle ->
                    when (muscle) {
                        "chest" -> 0.8f
                        "back" -> 0.6f
                        "shoulders" -> 0.4f
                        "biceps" -> 0.5f
                        "triceps" -> 0.7f
                        "quadriceps" -> 0.9f
                        "hamstrings" -> 0.3f
                        "glutes" -> 0.6f
                        "calves" -> 0.2f
                        "core" -> 0.5f
                        else -> 0.3f
                    }
                }
                _activeMuscleGroups.value = muscleMap
            }
        }
    }

    fun getWorkoutsForDay(dayOffset: Int): Int {
        val dayStart = System.currentTimeMillis() - (dayOffset * 24 * 60 * 60 * 1000L)
        val dayEnd = dayStart + (24 * 60 * 60 * 1000L)
        return _weeklyWorkouts.value.count { workout ->
            workout.completedAt != null &&
                    workout.completedAt >= dayStart &&
                    workout.completedAt < dayEnd
        }
    }

    fun refresh() {
        loadProgressData()
    }
}