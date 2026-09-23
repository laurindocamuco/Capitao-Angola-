package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.audio.GameSoundEngine
import com.example.data.PlayerProgress
import com.example.engine3d.Vector3
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class GameEngine(
    val soundEngine: GameSoundEngine
) {
    // Player State
    var playerPosition = Vector3(0f, 0f, 0f)
    var playerVelocity = Vector3(0f, 0f, 0f)
    var playerRotationY = 0f
    var playerHealth = 100f
    var playerEnergy = 100f
    var isRunning = false
    var isAttacking = false
    var isDodging = false
    var attackTimer = 0f
    var dodgeTimer = 0f
    var capeWaveTime = 0f

    // Current Level & Progress
    var currentLevel = GameLevelId.LEVEL_1
    val comboState = ComboState()
    var isBossLevel = false
    var bossPhase = 1
    var bossHealth = 100f
    var bossMaxHealth = 100f
    var bossShieldActive = false
    var bossPillarsRemaining = 2

    // NPCs, Vehicles, Objectives
    val npcs = mutableListOf<NPC>()
    val candongueiros = mutableListOf<Candongueiro>()
    val objectives = mutableListOf<Objective>()
    var currentObjectiveIndex = 0

    // City Mesh Reference
    val worldCity = WorldCity()

    // Floating notifications
    private val _notifications = MutableStateFlow<List<FloatingNotification>>(emptyList())
    val notifications: StateFlow<List<FloatingNotification>> = _notifications.asStateFlow()

    // Cinematic Dialogue & Cutscene State
    var activeDialogue = mutableListOf<DialogueLine>()
    var currentDialogueIndex = -1
    var isShowingDialogue = false
    var isEndingCutscene = false
    var isLevelComplete = false

    // Player stats tracker (synced to Room DB)
    var totalHeroismPoints = 0
    var totalExperience = 0
    var heroRankLevel = 1
    var heroRankTitle = "Cidadão Solidário"
    var reputation = 100
    var peopleHelped = 0
    var crimesStopped = 0
    var objectsRecovered = 0
    var missionsCompleted = 0
    var freeRoamUnlocked = false
    var fullSuitUnlocked = false

    init {
        loadLevel(GameLevelId.LEVEL_1)
    }

    fun loadFromProgress(progress: PlayerProgress?) {
        if (progress != null) {
            totalHeroismPoints = progress.heroismPoints
            totalExperience = progress.experience
            heroRankLevel = progress.heroRankLevel
            heroRankTitle = progress.heroRankTitle
            reputation = progress.reputation
            peopleHelped = progress.peopleHelped
            crimesStopped = progress.crimesStopped
            objectsRecovered = progress.objectsRecovered
            missionsCompleted = progress.missionsCompleted
            freeRoamUnlocked = progress.freeRoamUnlocked
            fullSuitUnlocked = progress.fullSuitUnlocked
        }
    }

    fun loadLevel(level: GameLevelId) {
        currentLevel = level
        isBossLevel = (level == GameLevelId.LEVEL_6)
        isLevelComplete = false
        isEndingCutscene = false
        isShowingDialogue = false
        currentDialogueIndex = -1
        activeDialogue.clear()
        npcs.clear()
        objectives.clear()
        candongueiros.clear()
        currentObjectiveIndex = 0

        soundEngine.startAmbientRhythm(isBoss = isBossLevel)

        if (isBossLevel) {
            setupBossBattle()
        } else {
            setupCityLevel(level)
        }
    }

    private fun setupCityLevel(level: GameLevelId) {
        playerPosition = Vector3(0f, 0f, -5f)
        playerVelocity = Vector3.ZERO
        playerRotationY = 0f
        playerHealth = 100f
        playerEnergy = 100f

        // Add iconic Candongueiros (Luanda Blue & White Minibuses)
        candongueiros.add(Candongueiro("van_1", Vector3(-4.5f, 1.1f, -60f), speed = 14f, minZ = -100f, maxZ = 100f, laneX = -4.5f, direction = 1f))
        candongueiros.add(Candongueiro("van_2", Vector3(4.5f, 1.1f, 80f), speed = 12f, minZ = -100f, maxZ = 100f, laneX = 4.5f, direction = -1f))
        candongueiros.add(Candongueiro("van_3", Vector3(-4.5f, 1.1f, 20f), speed = 15f, minZ = -100f, maxZ = 100f, laneX = -4.5f, direction = 1f))

        when (level) {
            GameLevelId.LEVEL_1 -> {
                // School pedestrian crossing & elderly assistance
                npcs.add(
                    NPC(
                        id = "child_1",
                        type = NPCType.CRIANCA,
                        position = Vector3(-8.5f, 0f, -15f),
                        name = "Menino Zito",
                        promptMessage = "Ajudar a atravessar a passadeira",
                        targetPosition = Vector3(8.5f, 0f, -15f)
                    )
                )
                npcs.add(
                    NPC(
                        id = "child_2",
                        type = NPCType.CRIANCA,
                        position = Vector3(-9.5f, 0f, -14f),
                        name = "Menina Neide",
                        promptMessage = "Ajudar a atravessar a passadeira",
                        targetPosition = Vector3(8.5f, 0f, -14f)
                    )
                )
                npcs.add(
                    NPC(
                        id = "elder_1",
                        type = NPCType.IDOSA,
                        position = Vector3(10f, 0f, -3f),
                        name = "Vovó Teresa",
                        promptMessage = "Ajudar a recolher compras caídas"
                    )
                )

                objectives.add(
                    Objective(
                        id = "obj_1",
                        title = "Atravessar o Menino Zito",
                        description = "Ajude o Menino Zito a cruzar a passadeira escolar com segurança.",
                        targetPos = Vector3(-9f, 0f, -15f),
                        pointsReward = 100,
                        targetNpcId = "child_1"
                    )
                )
                objectives.add(
                    Objective(
                        id = "obj_2",
                        title = "Atravessar a Menina Neide",
                        description = "Ajude a Menina Neide a cruzar a passadeira escolar com segurança.",
                        targetPos = Vector3(-9f, 0f, -14f),
                        pointsReward = 100,
                        targetNpcId = "child_2"
                    )
                )
                objectives.add(
                    Objective(
                        id = "obj_3",
                        title = "Amparar a Vovó Teresa",
                        description = "Ajude a Vovó Teresa a recolher as suas compras e atravessar a rua com carinho.",
                        targetPos = Vector3(10f, 0f, -3f),
                        pointsReward = 250,
                        targetNpcId = "elder_1"
                    )
                )
            }

            GameLevelId.LEVEL_2 -> {
                // Market: Zungueiras with head baskets + protect from market thief
                playerPosition = Vector3(-12f, 0f, 20f)
                npcs.add(
                    NPC(
                        id = "zung_1",
                        type = NPCType.ZUNGUEIRA,
                        position = Vector3(-18f, 0f, 26f),
                        name = "Dona Joana",
                        promptMessage = "Ajudar a levantar e equilibrar a cesta"
                    )
                )
                npcs.add(
                    NPC(
                        id = "zung_2",
                        type = NPCType.ZUNGUEIRA,
                        position = Vector3(-24f, 0f, 34f),
                        name = "Dona Esperança",
                        promptMessage = "Ajudar a transportar mercadorias"
                    )
                )
                npcs.add(
                    NPC(
                        id = "thief_market",
                        type = NPCType.LADRAO,
                        position = Vector3(-20f, 0f, 42f),
                        name = "Meliante do Mercado",
                        promptMessage = "Conter e desarmar meliante"
                    )
                )

                objectives.add(
                    Objective(
                        id = "obj_m1",
                        title = "Solidariedade com a Zungueira",
                        description = "Ajude a Dona Joana a equilibrar a cesta de frutas na cabeça com respeito e dignidade.",
                        targetPos = Vector3(-18f, 0f, 26f),
                        pointsReward = 150,
                        targetNpcId = "zung_1"
                    )
                )
                objectives.add(
                    Objective(
                        id = "obj_m2",
                        title = "Apoiar a Comerciante",
                        description = "Auxilie a Dona Esperança no transporte dos caixotes de alimentos da banca.",
                        targetPos = Vector3(-24f, 0f, 34f),
                        pointsReward = 150,
                        targetNpcId = "zung_2"
                    )
                )
                objectives.add(
                    Objective(
                        id = "obj_m3",
                        title = "Proteger os Feirantes",
                        description = "Impeça o meliante de intimidar os comerciantes no mercado popular.",
                        targetPos = Vector3(-20f, 0f, 42f),
                        pointsReward = 200,
                        targetNpcId = "thief_market"
                    )
                )
            }

            GameLevelId.LEVEL_3 -> {
                // Street chase: Fleeing thief dodging Candongueiros
                playerPosition = Vector3(0f, 0f, -40f)
                val thief = NPC(
                    id = "chase_thief",
                    type = NPCType.LADRAO,
                    position = Vector3(0f, 0f, -10f),
                    rotationY = 0f,
                    state = NPCState.FLEEING,
                    name = "Ladrão em Fuga",
                    promptMessage = "Alcançar e recuperar a mala roubada"
                )
                npcs.add(thief)
                npcs.add(
                    NPC(
                        id = "victim_elder",
                        type = NPCType.IDOSA,
                        position = Vector3(10f, 0f, -42f),
                        name = "Dona Rosa",
                        promptMessage = "Devolver a mala recuperada"
                    )
                )

                objectives.add(
                    Objective(
                        id = "obj_chase",
                        title = "Perseguir o Ladrão",
                        description = "Corra pela avenida, desvie do tráfego e deserte o assaltante sem ferir civis.",
                        targetPos = thief.position,
                        pointsReward = 300,
                        targetNpcId = "chase_thief"
                    )
                )
            }

            GameLevelId.LEVEL_4 -> {
                // Protect neighborhood: Multi-emergency chain
                playerPosition = Vector3(0f, 0f, 0f)
                npcs.add(NPC("bairro_1", NPCType.IDOSA, Vector3(10f, 0f, -20f), name = "Senhora Maria", promptMessage = "Ajudar a atravessar em segurança"))
                npcs.add(NPC("bairro_2", NPCType.COMERCIANTE, Vector3(-12f, 0f, 15f), name = "Sr. Afonso", promptMessage = "Defender banca do comerciante"))
                npcs.add(NPC("bairro_3", NPCType.LADRAO, Vector3(-10f, 0f, 16f), name = "Assaltante", promptMessage = "Impedir assalto ao comerciante"))
                npcs.add(NPC("bairro_4", NPCType.CRIANCA, Vector3(8f, 0f, 40f), name = "Menino Lito", promptMessage = "Acompanhar criança perdida"))

                objectives.add(Objective("obj_b1", "Socorrer a Senhora Maria", "Acompanhe a idosa até a calçada com segurança.", Vector3(10f, 0f, -20f), 250, "bairro_1"))
                objectives.add(Objective("obj_b2", "Impedir Assalto à Banca", "Neutralize o meliante que ameaça o Sr. Afonso.", Vector3(-10f, 0f, 16f), 300, "bairro_3"))
                objectives.add(Objective("obj_b3", "Acolher Criança Perdida", "Guie o Menino Lito de volta aos familiares.", Vector3(8f, 0f, 40f), 100, "bairro_4"))
            }

            GameLevelId.LEVEL_5 -> {
                // Collect evidence against Super Mineiro
                playerPosition = Vector3(0f, 0f, 20f)
                npcs.add(NPC("informant", NPCType.COMERCIANTE, Vector3(-15f, 0f, 35f), name = "Informante da Comunidade", promptMessage = "Ouvir revelação sobre o Super Mineiro"))
                npcs.add(NPC("dossier_1", NPCType.COMERCIANTE, Vector3(10f, 0f, -30f), name = "Dossiê Bancário", promptMessage = "Recolher registos financeiros ilícitos"))
                npcs.add(NPC("dossier_2", NPCType.COMERCIANTE, Vector3(-26f, 0f, 75f), name = "Plantas do Arranha-Céus", promptMessage = "Recolher planta da torre do Super Mineiro"))

                objectives.add(Objective("obj_d1", "Dialogar com o Informante", "Descubra quem financia a onda de crimes na cidade.", Vector3(-15f, 0f, 35f), 300, "informant"))
                objectives.add(Objective("obj_d2", "Recuperar Dossiê Oculto", "Recolha as provas bancárias dos desvios corporativos.", Vector3(10f, 0f, -30f), 300, "dossier_1"))
                objectives.add(Objective("obj_d3", "Obter Plantas da Torre", "Encontre os códigos de acesso ao arranha-céus.", Vector3(-26f, 0f, 75f), 300, "dossier_2"))
            }

            GameLevelId.FREE_ROAM -> {
                playerPosition = Vector3(0f, 0f, 0f)
                // Infinite community protection missions
                npcs.add(NPC("fr_1", NPCType.ZUNGUEIRA, Vector3(-18f, 0f, 25f), name = "Zungueira", promptMessage = "Ajudar com a cesta"))
                npcs.add(NPC("fr_2", NPCType.CRIANCA, Vector3(-8f, 0f, -15f), name = "Criança", promptMessage = "Ajudar a atravessar"))
                npcs.add(NPC("fr_3", NPCType.LADRAO, Vector3(6f, 0f, 30f), name = "Ladrão", promptMessage = "Impedir assalto"))
                objectives.add(Objective("obj_fr", "Patrulhar a Cidade", "Mantenha a paz e ajude quem precisar pelas ruas de Angola.", Vector3(0f, 0f, 0f), 200))
            }

            else -> {}
        }
    }

    private fun setupBossBattle() {
        playerPosition = Vector3(0f, 0f, -10f)
        playerVelocity = Vector3.ZERO
        playerRotationY = 0f
        playerHealth = 100f
        playerEnergy = 100f
        bossPhase = 1
        bossHealth = 100f
        bossMaxHealth = 100f
        bossShieldActive = true
        bossPillarsRemaining = 2

        val boss = NPC(
            id = "super_mineiro",
            type = NPCType.SUPER_MINEIRO,
            position = Vector3(0f, 0f, 8f),
            name = "Super Mineiro",
            promptMessage = "Confrontar o Magnata Corrupto"
        )
        npcs.add(boss)

        // Bodyguards
        npcs.add(NPC("guard_1", NPCType.SEGURANCA_BOSS, Vector3(-4f, 0f, 4f), name = "Segurança de Elite 1", promptMessage = "Desarmar guarda"))
        npcs.add(NPC("guard_2", NPCType.SEGURANCA_BOSS, Vector3(4f, 0f, 4f), name = "Segurança de Elite 2", promptMessage = "Desarmar guarda"))

        // Dialogue setup
        activeDialogue.clear()
        activeDialogue.addAll(
            listOf(
                DialogueLine("SUPER MINEIRO", "Então finalmente chegaste, Capitão Angola.", isVillain = true),
                DialogueLine("CAPITÃO ANGOLA", "..."),
                DialogueLine("SUPER MINEIRO", "Diz-me uma coisa... por que lutas por esse povo? Podes ter poder. Podes ter respeito.", isVillain = true),
                DialogueLine("SUPER MINEIRO", "Mas eu tenho dinheiro, influência e poder suficiente para mudar o teu destino.", isVillain = true),
                DialogueLine("SUPER MINEIRO", "Podes fazer parte do meu elenco. Senta-te à minha mesa. Trabalha comigo e nunca mais precisarás lutar nas ruas.", isVillain = true),
                DialogueLine("CAPITÃO ANGOLA", "Eu nunca vou trair o meu povo."),
                DialogueLine("CAPITÃO ANGOLA", "Enquanto houver alguém precisando de ajuda, eu estarei lá."),
                DialogueLine("CAPITÃO ANGOLA", "Eu não luto por dinheiro."),
                DialogueLine("CAPITÃO ANGOLA", "Eu não luto por poder."),
                DialogueLine("CAPITÃO ANGOLA", "Eu luto por justiça."),
                DialogueLine("CAPITÃO ANGOLA", "EU SOU O CAPITÃO ANGOLA!")
            )
        )
        currentDialogueIndex = 0
        isShowingDialogue = true
    }

    fun advanceDialogue(): Boolean {
        if (!isShowingDialogue) return false
        if (currentDialogueIndex < activeDialogue.size - 1) {
            currentDialogueIndex++
            soundEngine.playCivilianThankYou()
            return true
        } else {
            isShowingDialogue = false
            currentDialogueIndex = -1
            if (isEndingCutscene) {
                // Return to main menu / free roam
                freeRoamUnlocked = true
                isLevelComplete = true
            }
            return false
        }
    }

    fun update(dt: Float) {
        if (isShowingDialogue) return

        capeWaveTime += dt * (if (isRunning) 14f else 4f)

        // Combo timer cooldown
        if (comboState.timer > 0f) {
            comboState.timer -= dt
            if (comboState.timer <= 0f) {
                comboState.reset()
            }
        }

        // Action timers
        if (attackTimer > 0f) {
            attackTimer -= dt
            if (attackTimer <= 0f) isAttacking = false
        }
        if (dodgeTimer > 0f) {
            dodgeTimer -= dt
            if (dodgeTimer <= 0f) isDodging = false
        }

        // Stamina recharge
        if (!isRunning && playerEnergy < 100f) {
            playerEnergy = (playerEnergy + 20f * dt).coerceAtMost(100f)
        }

        // Update player position
        val speedMultiplier = if (comboState.isHeroMode) 1.6f else 1.0f
        playerPosition = playerPosition + playerVelocity * (dt * speedMultiplier)

        // Keep player in bounds
        if (!isBossLevel) {
            playerPosition = Vector3(
                playerPosition.x.coerceIn(-34f, 15f),
                playerPosition.y,
                playerPosition.z.coerceIn(-115f, 115f)
            )
        } else {
            playerPosition = Vector3(
                playerPosition.x.coerceIn(-15f, 15f),
                playerPosition.y,
                playerPosition.z.coerceIn(-15f, 15f)
            )
        }

        // Candongueiro simulation (traffic)
        for (van in candongueiros) {
            van.position = Vector3(
                van.position.x,
                van.position.y,
                van.position.z + van.speed * van.direction * dt
            )
            if (van.direction > 0 && van.position.z > van.maxZ) {
                van.position = Vector3(van.position.x, van.position.y, van.minZ)
            } else if (van.direction < 0 && van.position.z < van.minZ) {
                van.position = Vector3(van.position.x, van.position.y, van.maxZ)
            }

            // Check collision with player
            val dist = (playerPosition - van.position).length()
            if (dist < 2.8f && !isDodging) {
                // Collateral bump warning
                soundEngine.playPunchImpact()
                reputation = (reputation - 5).coerceAtLeast(10)
                addNotification("Cuidado com o trânsito! (-5 Reputação)", 0, Color(0xFFFF5252))
                // Push player back
                playerPosition = playerPosition + Vector3(van.direction * 2f, 0f, 0f)
            }
        }

        // Fleeing thief in Level 3
        if (currentLevel == GameLevelId.LEVEL_3) {
            val thief = npcs.find { it.id == "chase_thief" }
            if (thief != null && thief.state == NPCState.FLEEING) {
                thief.position = Vector3(
                    thief.position.x,
                    thief.position.y,
                    thief.position.z + 8.5f * dt
                )
                if (thief.position.z > 95f) {
                    thief.position = Vector3(thief.position.x, thief.position.y, 95f)
                }
            }
        }

        // Boss Battle updates
        if (isBossLevel) {
            updateBoss(dt)
        }

        // Clean expired notifications
        val now = System.currentTimeMillis()
        val currentList = _notifications.value.filter { now - it.startTime < 3000 }
        _notifications.value = currentList
    }

    private fun updateBoss(dt: Float) {
        val boss = npcs.find { it.id == "super_mineiro" } ?: return
        val distToBoss = (playerPosition - boss.position).length()

        // Turn boss towards hero
        val dx = playerPosition.x - boss.position.x
        val dz = playerPosition.z - boss.position.z
        boss.rotationY = atan2(dx, dz)

        // Boss combat phases
        when (bossPhase) {
            1 -> {
                // Guards attack
                val activeGuards = npcs.filter { it.type == NPCType.SEGURANCA_BOSS && it.state != NPCState.DEFEATED }
                if (activeGuards.isEmpty()) {
                    bossPhase = 2
                    addNotification("Fase 2: Super Mineiro avança!", 200, Color(0xFFFFD100))
                    soundEngine.playHeroModeActivate()
                }
            }
            2 -> {
                // Super Mineiro gauntlet charge
                if (distToBoss < 10f) {
                    val step = (playerPosition - boss.position).normalized() * (4.5f * dt)
                    boss.position = boss.position + Vector3(step.x, 0f, step.z)
                }
            }
            3 -> {
                // Shield emitters active
                bossShieldActive = bossPillarsRemaining > 0
                if (bossPillarsRemaining <= 0) {
                    bossPhase = 4
                    addNotification("Fase 4: Geradores destruídos!", 300, Color(0xFFFFD100))
                }
            }
            4 -> {
                // Destructible room overload
                bossShieldActive = false
                bossPhase = 5
                addNotification("Fase Final: Duelo Decisivo!", 500, Color(0xFFFFD100))
            }
            5 -> {
                // Final Duel
                if (bossHealth <= 0f) {
                    startEndingCutscene()
                }
            }
        }
    }

    fun handleMoveInput(inputX: Float, inputZ: Float, sprint: Boolean) {
        val length = kotlin.math.sqrt(inputX * inputX + inputZ * inputZ)
        if (length > 0.05f) {
            val normX = inputX / length
            val normZ = inputZ / length
            playerRotationY = atan2(normX, normZ)

            isRunning = sprint && playerEnergy > 10f
            val baseSpeed = if (isRunning) 11f else 5.5f
            if (isRunning) {
                playerEnergy = (playerEnergy - 15f * 0.016f).coerceAtLeast(0f)
            }

            playerVelocity = Vector3(normX * baseSpeed, 0f, normZ * baseSpeed)
        } else {
            playerVelocity = Vector3.ZERO
            isRunning = false
        }
    }

    fun handleDodge() {
        if (dodgeTimer <= 0f && playerEnergy >= 20f) {
            isDodging = true
            dodgeTimer = 0.45f
            playerEnergy -= 20f
            soundEngine.playDodgeWhoosh()
            val forwardX = sin(playerRotationY)
            val forwardZ = cos(playerRotationY)
            playerVelocity = Vector3(forwardX * 14f, 0f, forwardZ * 14f)
        }
    }

    fun handleAttack() {
        if (attackTimer > 0f) return
        isAttacking = true
        attackTimer = 0.35f
        soundEngine.playPunchImpact()

        // Check if hitting any enemies or boss
        val attackRange = 3.2f

        if (isBossLevel) {
            val boss = npcs.find { it.id == "super_mineiro" }
            if (boss != null && (playerPosition - boss.position).length() < attackRange) {
                if (!bossShieldActive) {
                    bossHealth = (bossHealth - 25f).coerceAtLeast(0f)
                    soundEngine.playPunchImpact()
                    addNotification("Golpe não-letal desferido!", 100, Color(0xFFFFD100))
                    if (bossHealth <= 0f) {
                        startEndingCutscene()
                    }
                } else {
                    addNotification("Campo de força ativo! Destrua os geradores!", 0, Color(0xFFFF5252))
                }
                return
            }

            // Check tech pillars
            if (bossPhase == 3 && bossPillarsRemaining > 0) {
                val pillar1Dist = (playerPosition - Vector3(-8f, 0f, 0f)).length()
                val pillar2Dist = (playerPosition - Vector3(8f, 0f, 0f)).length()
                if (pillar1Dist < attackRange || pillar2Dist < attackRange) {
                    bossPillarsRemaining = (bossPillarsRemaining - 1).coerceAtLeast(0)
                    soundEngine.playPunchImpact()
                    addNotification("Gerador desativado!", 250, Color(0xFF00E5FF))
                    if (bossPillarsRemaining <= 0) {
                        bossShieldActive = false
                        bossPhase = 4
                    }
                    return
                }
            }

            // Check guards
            val nearestGuard = npcs.find { it.type == NPCType.SEGURANCA_BOSS && it.state != NPCState.DEFEATED && (playerPosition - it.position).length() < attackRange }
            if (nearestGuard != null) {
                nearestGuard.state = NPCState.DEFEATED
                crimesStopped++
                addHeroismPoints(200, "Segurança imobilizado com honra!")
                return
            }
        } else {
            // City thieves
            val nearestThief = npcs.find { it.type == NPCType.LADRAO && it.state != NPCState.DEFEATED && (playerPosition - it.position).length() < attackRange }
            if (nearestThief != null) {
                nearestThief.state = NPCState.DEFEATED
                crimesStopped++
                addHeroismPoints(200, "Assalto impedido com bravura!")
                checkObjectiveProgress(nearestThief.id)
            }
        }
    }

    fun handleInteract() {
        val interactRange = 3.6f
        val nearbyNpc = npcs.find { it.isInteractable && it.state != NPCState.HELPED && it.state != NPCState.DEFEATED && (playerPosition - it.position).length() < interactRange }

        if (nearbyNpc != null) {
            when (nearbyNpc.type) {
                NPCType.CRIANCA -> {
                    nearbyNpc.state = NPCState.HELPED
                    nearbyNpc.position = nearbyNpc.targetPosition ?: nearbyNpc.position
                    peopleHelped++
                    soundEngine.playCivilianThankYou()
                    addHeroismPoints(100, "Ajudou a criança a atravessar a estrada!")
                    checkObjectiveProgress(nearbyNpc.id)
                }
                NPCType.ZUNGUEIRA -> {
                    nearbyNpc.state = NPCState.HELPED
                    peopleHelped++
                    soundEngine.playCivilianThankYou()
                    addHeroismPoints(150, "Cesta equilibrada na cabeça com dignidade!")
                    checkObjectiveProgress(nearbyNpc.id)
                }
                NPCType.IDOSA -> {
                    nearbyNpc.state = NPCState.HELPED
                    peopleHelped++
                    soundEngine.playCivilianThankYou()
                    addHeroismPoints(250, "Ajudou a senhora idosa com carinho!")
                    checkObjectiveProgress(nearbyNpc.id)
                }
                NPCType.COMERCIANTE -> {
                    nearbyNpc.state = NPCState.HELPED
                    peopleHelped++
                    soundEngine.playCivilianThankYou()
                    addHeroismPoints(300, "Comerciante protegido e amparado!")
                    checkObjectiveProgress(nearbyNpc.id)
                }
                NPCType.LADRAO -> {
                    nearbyNpc.state = NPCState.DEFEATED
                    crimesStopped++
                    objectsRecovered++
                    soundEngine.playPunchImpact()
                    addHeroismPoints(300, "Objeto recuperado e devolvido!")
                    checkObjectiveProgress(nearbyNpc.id)
                }
                else -> {}
            }
        }
    }

    private fun checkObjectiveProgress(npcId: String) {
        val matchingObj = objectives.find { it.targetNpcId == npcId && !it.isCompleted }
        if (matchingObj != null) {
            matchingObj.isCompleted = true
            val nextUncompleted = objectives.indexOfFirst { !it.isCompleted }
            if (nextUncompleted != -1) {
                currentObjectiveIndex = nextUncompleted
            } else {
                currentObjectiveIndex = objectives.size
                onLevelFinished()
            }
        }
    }

    private fun onLevelFinished() {
        isLevelComplete = true
        missionsCompleted++
        addHeroismPoints(500, "MISSÃO CONCLUÍDA SEM DANOS A CIVIS!")
        soundEngine.playHeroModeActivate()

        if (currentLevel == GameLevelId.LEVEL_1) {
            fullSuitUnlocked = true
            addNotification("Recompensa: Uniforme Completo do Capitão Angola Desbloqueado!", 0, Color(0xFFFFD100))
        } else if (currentLevel == GameLevelId.LEVEL_2) {
            reputation = (reputation + 50).coerceAtMost(500)
            addNotification("Recompensa: +1 Nível de Reputação!", 0, Color(0xFFFFD100))
        }
    }

    private fun startEndingCutscene() {
        isEndingCutscene = true
        activeDialogue.clear()
        activeDialogue.addAll(
            listOf(
                DialogueLine("NARRADOR", "O Super Mineiro é desarmado e a cidade amanhece sob um novo horizonte."),
                DialogueLine("CRIANÇA", "Capitão Angola... tu vais continuar a ajudar-nos?"),
                DialogueLine("CAPITÃO ANGOLA", "Enquanto houver alguém precisando de ajuda... eu estarei aqui."),
                DialogueLine("CAPITÃO ANGOLA", "CORAGEM PARA SERVIR."),
                DialogueLine("TEASER", "CAPITÃO ANGOLA — UMA NOVA AMEAÇA SURGE...", emotion = "MYSTERY")
            )
        )
        currentDialogueIndex = 0
        isShowingDialogue = true
        soundEngine.playHeroModeActivate()
    }

    fun addHeroismPoints(basePoints: Int, description: String) {
        comboState.addCombo()
        val totalGained = basePoints * comboState.multiplier
        totalHeroismPoints += totalGained
        totalExperience += totalGained

        reputation = (reputation + (10 * comboState.multiplier)).coerceAtMost(500)
        updateHeroRank()

        soundEngine.playHeroicChime()
        val comboSuffix = if (comboState.multiplier > 1) " (Combo x${comboState.multiplier}!)" else ""
        addNotification("+$totalGained PONTOS DE HEROÍSMO$comboSuffix\n$description", totalGained, Color(0xFFFFD100))

        if (comboState.isHeroMode) {
            soundEngine.playHeroModeActivate()
            addNotification("★ MODO HERÓI ATIVADO! ★", 0, Color(0xFFFFD700))
        }
    }

    private fun updateHeroRank() {
        val oldRank = heroRankLevel
        when {
            totalExperience >= 2800 -> {
                heroRankLevel = 5
                heroRankTitle = "Capitão Angola"
            }
            totalExperience >= 1800 -> {
                heroRankLevel = 4
                heroRankTitle = "Herói da Cidade"
            }
            totalExperience >= 1000 -> {
                heroRankLevel = 3
                heroRankTitle = "Guardião do Bairro"
            }
            totalExperience >= 400 -> {
                heroRankLevel = 2
                heroRankTitle = "Protetor da Rua"
            }
            else -> {
                heroRankLevel = 1
                heroRankTitle = "Cidadão Solidário"
            }
        }
        if (heroRankLevel > oldRank) {
            addNotification("NOVA PATENTE DE HEROÍSMO: $heroRankTitle!", 0, Color(0xFFFFD100))
        }
    }

    private fun addNotification(text: String, points: Int, color: Color) {
        val current = _notifications.value.toMutableList()
        current.add(FloatingNotification(text = text, points = points, color = color))
        _notifications.value = current
    }

    fun toPlayerProgress(): PlayerProgress {
        return PlayerProgress(
            heroismPoints = totalHeroismPoints,
            experience = totalExperience,
            heroRankLevel = heroRankLevel,
            heroRankTitle = heroRankTitle,
            reputation = reputation,
            peopleHelped = peopleHelped,
            crimesStopped = crimesStopped,
            objectsRecovered = objectsRecovered,
            missionsCompleted = missionsCompleted,
            highestCombo = comboState.count.coerceAtLeast(1),
            highestLevelUnlocked = (currentLevel.levelNumber + 1).coerceAtMost(6),
            freeRoamUnlocked = freeRoamUnlocked,
            fullSuitUnlocked = fullSuitUnlocked
        )
    }
}
