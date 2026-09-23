package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class HeroRepository(private val dao: HeroDao) {
    val progress: Flow<PlayerProgress?> = dao.getPlayerProgress()
    val achievements: Flow<List<Achievement>> = dao.getAllAchievements()

    private val initialAchievements = listOf(
        Achievement(
            id = "cuj_level1",
            title = "O Primeiro Chamado",
            description = "Ajudou as crianças e protegeu a comunidade na escola.",
            iconName = "shield"
        ),
        Achievement(
            id = "cuj_level2",
            title = "Coração do Mercado",
            description = "Apoiou as zungueiras e defendeu os feirantes com honra.",
            iconName = "store"
        ),
        Achievement(
            id = "cuj_level3",
            title = "Velocidade & Justiça",
            description = "Recuperou pertences roubados desviando do trânsito sem ferir civis.",
            iconName = "directions_run"
        ),
        Achievement(
            id = "cuj_level4",
            title = "Guardião do Povo",
            description = "Restaurou a paz no bairro resolvendo múltiplas ocorrências em série.",
            iconName = "security"
        ),
        Achievement(
            id = "cuj_level5",
            title = "A Verdade Revelada",
            description = "Reuniu provas irrefutáveis contra o cartel do Super Mineiro.",
            iconName = "visibility"
        ),
        Achievement(
            id = "cuj_boss",
            title = "Coragem Para Servir",
            description = "Venceu o Super Mineiro e libertou a cidade da corrupção.",
            iconName = "military_tech"
        ),
        Achievement(
            id = "combo_hero_mode",
            title = "Modo Herói Ativado",
            description = "Alcançou combo x5 com altruísmo e ações em cadeia perfeitas.",
            iconName = "bolt"
        )
    )

    suspend fun initializeDefaultsIfNeeded() {
        val currentProgress = dao.getPlayerProgress().firstOrNull()
        if (currentProgress == null) {
            dao.savePlayerProgress(
                PlayerProgress(
                    heroismPoints = 0,
                    experience = 0,
                    heroRankLevel = 1,
                    heroRankTitle = "Cidadão Solidário",
                    reputation = 100,
                    highestLevelUnlocked = 1
                )
            )
        }
        val currentAchievements = dao.getAllAchievements().firstOrNull()
        if (currentAchievements.isNullOrEmpty()) {
            dao.insertAchievements(initialAchievements)
        }
    }

    suspend fun updateProgress(progress: PlayerProgress) {
        dao.savePlayerProgress(progress)
    }

    suspend fun unlockAchievement(id: String) {
        dao.unlockAchievement(id)
    }

    suspend fun resetData() {
        dao.clearProgress()
        initializeDefaultsIfNeeded()
    }
}
