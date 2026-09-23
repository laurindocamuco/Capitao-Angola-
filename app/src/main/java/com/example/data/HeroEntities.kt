package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_progress")
data class PlayerProgress(
    @PrimaryKey val id: Int = 1,
    val heroismPoints: Int = 0,
    val experience: Int = 0,
    val heroRankLevel: Int = 1,
    val heroRankTitle: String = "Cidadão Solidário",
    val reputation: Int = 100, // 0 to 500
    val peopleHelped: Int = 0,
    val crimesStopped: Int = 0,
    val objectsRecovered: Int = 0,
    val missionsCompleted: Int = 0,
    val highestCombo: Int = 1,
    val highestLevelUnlocked: Int = 1,
    val freeRoamUnlocked: Boolean = false,
    val fullSuitUnlocked: Boolean = false
)

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L,
    val iconName: String = "shield"
)
