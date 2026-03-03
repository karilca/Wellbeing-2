package com.gymflow.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gymflow.app.data.local.AppDatabase
import com.gymflow.app.data.repository.UserRepository
import com.gymflow.app.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val userRepository = UserRepository(database.userProfileDao())

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _hasProfile = MutableStateFlow(false)
    val hasProfile: StateFlow<Boolean> = _hasProfile.asStateFlow()

    init {
        loadUserProfile()
        checkHasProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userRepository.getLatestUserProfile().collect { profile ->
                _userProfile.value = profile
            }
        }
    }

    private fun checkHasProfile() {
        viewModelScope.launch {
            _hasProfile.value = userRepository.hasUserProfile()
        }
    }

    fun saveProfile(
        name: String,
        age: Int,
        weight: Float,
        height: Float,
        goal: String,
        fitnessLevel: String
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val profile = UserProfile(
                    name = name,
                    age = age,
                    weight = weight,
                    height = height,
                    goal = goal,
                    fitnessLevel = fitnessLevel
                )
                userRepository.saveUserProfile(profile)
                _saveSuccess.value = true
                _hasProfile.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                userRepository.updateUserProfile(userProfile)
                _saveSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}