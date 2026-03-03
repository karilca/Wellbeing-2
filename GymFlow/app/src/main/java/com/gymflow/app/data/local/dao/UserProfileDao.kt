package com.gymflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymflow.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfileEntity)

    @Delete
    suspend fun deleteUserProfile(userProfile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getUserProfileById(id: Int): UserProfileEntity?

    @Query("SELECT * FROM user_profiles ORDER BY createdAt DESC LIMIT 1")
    fun getLatestUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles ORDER BY createdAt DESC")
    fun getAllUserProfiles(): Flow<List<UserProfileEntity>>

    @Query("DELETE FROM user_profiles")
    suspend fun deleteAllUserProfiles()

    @Query("SELECT COUNT(*) FROM user_profiles")
    suspend fun getUserProfileCount(): Int
}