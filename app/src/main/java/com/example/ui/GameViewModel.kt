package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameSoundEngine
import com.example.data.Achievement
import com.example.data.HeroDatabase
import com.example.data.HeroRepository
import com.example.data.PlayerProgress
import com.example.game.GameEngine
import com.example.game.GameLevelId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = HeroDatabase.getInstance(application)
    private val repository = HeroRepository(database.heroDao())
    val soundEngine = GameSoundEngine()
    val engine = GameEngine(soundEngine)

    val progress: StateFlow<PlayerProgress?> = repository.progress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val achievements: StateFlow<List<Achievement>> = repository.achievements
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            progress.collect { p ->
                if (p != null) {
                    engine.loadFromProgress(p)
                }
            }
        }
    }

    fun startLevel(level: GameLevelId) {
        engine.loadLevel(level)
    }

    fun saveCurrentProgress() {
        viewModelScope.launch {
            val prog = engine.toPlayerProgress()
            repository.updateProgress(prog)

            // Unlock achievements based on stats
            if (engine.peopleHelped >= 2) {
                repository.unlockAchievement("cuj_level1")
            }
            if (engine.peopleHelped >= 5) {
                repository.unlockAchievement("cuj_level2")
            }
            if (engine.crimesStopped >= 1) {
                repository.unlockAchievement("cuj_level3")
            }
            if (engine.crimesStopped >= 3) {
                repository.unlockAchievement("cuj_level4")
            }
            if (engine.missionsCompleted >= 5) {
                repository.unlockAchievement("cuj_level5")
            }
            if (engine.freeRoamUnlocked) {
                repository.unlockAchievement("cuj_boss")
            }
            if (engine.comboState.isHeroMode) {
                repository.unlockAchievement("combo_hero_mode")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.stopAmbientRhythm()
    }
}
