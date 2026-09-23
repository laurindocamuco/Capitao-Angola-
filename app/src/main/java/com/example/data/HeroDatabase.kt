package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PlayerProgress::class, Achievement::class],
    version = 1,
    exportSchema = false
)
abstract class HeroDatabase : RoomDatabase() {
    abstract fun heroDao(): HeroDao

    companion object {
        @Volatile
        private var INSTANCE: HeroDatabase? = null

        fun getInstance(context: Context): HeroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HeroDatabase::class.java,
                    "capitao_angola_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
