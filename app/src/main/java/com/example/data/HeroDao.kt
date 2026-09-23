package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HeroDao {
    @Query("SELECT * FROM player_progress WHERE id = 1 LIMIT 1")
    fun getPlayerProgress(): Flow<PlayerProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerProgress(progress: PlayerProgress)

    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedAt = :timestamp WHERE id = :id")
    suspend fun unlockAchievement(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM player_progress")
    suspend fun clearProgress()
}
