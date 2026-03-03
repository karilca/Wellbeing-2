package com.gymflow.app.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.gymflow.app.domain.model.UserProfile
import com.gymflow.app.domain.model.WorkoutPlan
import kotlinx.coroutines.tasks.await

class FirebaseService {

    private val firestore = FirebaseFirestore.getInstance()

    private val usersCollection = firestore.collection("users")
    private val workoutsCollection = firestore.collection("workouts")
    private val challengesCollection = firestore.collection("challenges")

    suspend fun saveUserProfile(userId: String, userProfile: UserProfile): Boolean {
        return try {
            val data = hashMapOf(
                "name" to userProfile.name,
                "age" to userProfile.age,
                "weight" to userProfile.weight,
                "height" to userProfile.height,
                "goal" to userProfile.goal,
                "fitnessLevel" to userProfile.fitnessLevel,
                "updatedAt" to System.currentTimeMillis()
            )
            usersCollection.document(userId).set(data).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getUserProfile(userId: String): Map<String, Any>? {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) document.data else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveWorkout(userId: String, workoutPlan: WorkoutPlan): Boolean {
        return try {
            val data = hashMapOf(
                "userId" to userId,
                "name" to workoutPlan.name,
                "description" to workoutPlan.description,
                "exerciseType" to workoutPlan.exerciseType,
                "durationMinutes" to workoutPlan.durationMinutes,
                "caloriesBurned" to workoutPlan.caloriesBurned,
                "formScore" to workoutPlan.formScore,
                "musclesWorked" to workoutPlan.musclesWorked,
                "isCompleted" to workoutPlan.isCompleted,
                "createdAt" to workoutPlan.createdAt,
                "completedAt" to workoutPlan.completedAt
            )
            workoutsCollection.add(data).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getLeaderboard(): List<Map<String, Any>> {
        return try {
            val snapshot = challengesCollection
                .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateChallengeScore(userId: String, score: Int): Boolean {
        return try {
            val data = hashMapOf(
                "userId" to userId,
                "score" to score,
                "updatedAt" to System.currentTimeMillis()
            )
            challengesCollection.document(userId).set(data).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getUserWorkouts(userId: String): List<Map<String, Any>> {
        return try {
            val snapshot = workoutsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}