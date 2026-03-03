package com.gymflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.gymflow.app.data.local.dao.ExerciseSessionDao
import com.gymflow.app.data.local.dao.UserProfileDao
import com.gymflow.app.data.local.dao.WorkoutDao
import com.gymflow.app.data.local.entity.ExerciseSessionEntity
import com.gymflow.app.data.local.entity.UserProfileEntity
import com.gymflow.app.data.local.entity.WorkoutEntity

@Database(
    entities = [
        UserProfileEntity::class,
        WorkoutEntity::class,
        ExerciseSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseSessionDao(): ExerciseSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gymflow_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}