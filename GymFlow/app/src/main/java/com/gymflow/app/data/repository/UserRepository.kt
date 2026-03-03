package com.gymflow.app.data.repository

import com.gymflow.app.data.local.dao.UserProfileDao
import com.gymflow.app.data.local.entity.UserProfileEntity
import com.gymflow.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(
    private val userProfileDao: UserProfileDao
) {

    fun getLatestUserProfile(): Flow<UserProfile?> {
        return userProfileDao.getLatestUserProfile().map { entity ->
            entity?.toDomain()
        }
    }

    fun getAllUserProfiles(): Flow<List<UserProfile>> {
        return userProfileDao.getAllUserProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun saveUserProfile(userProfile: UserProfile) {
        userProfileDao.insertUserProfile(userProfile.toEntity())
    }

    suspend fun updateUserProfile(userProfile: UserProfile) {
        userProfileDao.updateUserProfile(userProfile.toEntity())
    }

    suspend fun deleteUserProfile(userProfile: UserProfile) {
        userProfileDao.deleteUserProfile(userProfile.toEntity())
    }

    suspend fun getUserProfileById(id: Int): UserProfile? {
        return userProfileDao.getUserProfileById(id)?.toDomain()
    }

    suspend fun hasUserProfile(): Boolean {
        return userProfileDao.getUserProfileCount() > 0
    }

    private fun UserProfileEntity.toDomain(): UserProfile {
        return UserProfile(
            id = id,
            name = name,
            age = age,
            weight = weight,
            height = height,
            goal = goal,
            fitnessLevel = fitnessLevel,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun UserProfile.toEntity(): UserProfileEntity {
        return UserProfileEntity(
            id = id,
            name = name,
            age = age,
            weight = weight,
            height = height,
            goal = goal,
            fitnessLevel = fitnessLevel,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }
}