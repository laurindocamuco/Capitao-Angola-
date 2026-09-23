package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.engine3d.Vector3

enum class NPCType {
    CRIANCA,
    ZUNGUEIRA,
    IDOSA,
    COMERCIANTE,
    LADRAO,
    SEGURANCA_BOSS,
    SUPER_MINEIRO
}

enum class NPCState {
    IDLE,
    WAITING_HELP,
    HELPED,
    FLEEING,
    DEFEATED,
    ATTACKING
}

data class NPC(
    val id: String,
    val type: NPCType,
    var position: Vector3,
    var rotationY: Float = 0f,
    var state: NPCState = NPCState.IDLE,
    val name: String,
    var promptMessage: String = "",
    var isInteractable: Boolean = true,
    var progress: Float = 0f, // 0 to 1 for actions like helping balance basket
    var targetPosition: Vector3? = null
)

data class Candongueiro(
    val id: String,
    var position: Vector3,
    val speed: Float,
    val minZ: Float,
    val maxZ: Float,
    val laneX: Float,
    val direction: Float = 1f // 1 for forward, -1 for backward
)

data class Objective(
    val id: String,
    val title: String,
    val description: String,
    val targetPos: Vector3,
    val pointsReward: Int,
    val targetNpcId: String? = null,
    var isCompleted: Boolean = false
)

data class FloatingNotification(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val points: Int = 0,
    val color: Color = Color(0xFFFFD100),
    val startTime: Long = System.currentTimeMillis()
)

data class DialogueLine(
    val speaker: String,
    val text: String,
    val isVillain: Boolean = false,
    val emotion: String = "NEUTRAL"
)

enum class GameLevelId(
    val levelNumber: Int,
    val title: String,
    val subtitle: String,
    val difficulty: String
) {
    LEVEL_1(1, "O PRIMEIRO CHAMADO", "Solidariedade e Primeiros Passos", "Fácil"),
    LEVEL_2(2, "O MERCADO", "Tradição, Zungueiras e Comércio", "Fácil / Média"),
    LEVEL_3(3, "PERSEGUIÇÃO NAS RUAS", "Velocidade Contra o Crime", "Média"),
    LEVEL_4(4, "PROTEGENDO O BAIRRO", "Guardião da Comunidade", "Média / Alta"),
    LEVEL_5(5, "A CIDADE EM PERIGO", "As Pistas do Super Mineiro", "Alta"),
    LEVEL_6(6, "A ASCENSÃO DO HERÓI", "Duelo na Cobertura do Arranha-Céus", "Muito Alta"),
    FREE_ROAM(7, "MODO PATRULHA LIVRE", "Protegendo a Cidade Sem Fim", "Livre")
}

data class ComboState(
    var count: Int = 0,
    var multiplier: Int = 1,
    var timer: Float = 0f,
    var isHeroMode: Boolean = false
) {
    fun addCombo() {
        count++
        multiplier = when {
            count >= 5 -> 5
            count >= 3 -> 3
            count >= 2 -> 2
            else -> 1
        }
        isHeroMode = count >= 5
        timer = 5f // 5 seconds reset timer
    }

    fun reset() {
        count = 0
        multiplier = 1
        isHeroMode = false
        timer = 0f
    }
}
