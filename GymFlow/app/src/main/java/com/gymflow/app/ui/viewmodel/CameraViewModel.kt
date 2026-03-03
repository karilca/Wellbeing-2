package com.gymflow.app.ui.viewmodel

import android.app.Application
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.app.domain.model.Exercise
import com.gymflow.app.domain.model.PoseResult
import com.gymflow.app.ml.ExerciseFormChecker
import com.gymflow.app.ml.FeedbackGenerator
import com.gymflow.app.ml.PoseAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val exerciseFormChecker = ExerciseFormChecker()
    private val poseAnalyzer = PoseAnalyzer(application, exerciseFormChecker)
    private val feedbackGenerator = FeedbackGenerator(application)

    private val _poseResult = MutableStateFlow<PoseResult?>(null)
    val poseResult: StateFlow<PoseResult?> = _poseResult.asStateFlow()

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex.asStateFlow()

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    private val _sessionDurationSeconds = MutableStateFlow(0)
    val sessionDurationSeconds: StateFlow<Int> = _sessionDurationSeconds.asStateFlow()

    private val _feedbackMessage = MutableStateFlow("")
    val feedbackMessage: StateFlow<String> = _feedbackMessage.asStateFlow()

    private val exercises = Exercise.DEFAULT_EXERCISES

    init {
        observePoseResults()
    }

    private fun observePoseResults() {
        viewModelScope.launch {
            poseAnalyzer.poseResult.collect { result ->
                result?.let {
                    _poseResult.value = it
                    val feedback = feedbackGenerator.generateFeedback(it)
                    if (feedback.isNotEmpty()) {
                        _feedbackMessage.value = feedback
                    }
                }
            }
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    fun analyzeImage(imageProxy: ImageProxy) {
        poseAnalyzer.analyze(imageProxy)
    }

    fun nextExercise() {
        val nextIndex = (_currentExerciseIndex.value + 1) % exercises.size
        _currentExerciseIndex.value = nextIndex
        updateCurrentExercise(nextIndex)
    }

    fun previousExercise() {
        val prevIndex = if (_currentExerciseIndex.value == 0) {
            exercises.size - 1
        } else {
            _currentExerciseIndex.value - 1
        }
        _currentExerciseIndex.value = prevIndex
        updateCurrentExercise(prevIndex)
    }

    private fun updateCurrentExercise(index: Int) {
        val exercise = exercises.getOrNull(index) ?: return
        poseAnalyzer.setCurrentExercise(exercise.name)
        val tip = feedbackGenerator.getExerciseTip(exercise.name)
        feedbackGenerator.speakFeedback(tip)
        _feedbackMessage.value = tip
    }

    fun startSession() {
        _isSessionActive.value = true
    }

    fun stopSession() {
        _isSessionActive.value = false
    }

    fun getCurrentExercise(): Exercise? {
        return exercises.getOrNull(_currentExerciseIndex.value)
    }

    fun release() {
        poseAnalyzer.close()
        feedbackGenerator.release()
    }

    override fun onCleared() {
        super.onCleared()
        release()
    }
}