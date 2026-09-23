package com.example.engine3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.game.Candongueiro
import com.example.game.GameEngine
import com.example.game.NPC
import com.example.game.NPCState
import com.example.game.NPCType
import com.example.ui.theme.HeroCandongueiroBlue
import com.example.ui.theme.HeroGoldAura
import com.example.ui.theme.HeroRed
import com.example.ui.theme.HeroYellow
import kotlin.math.cos
import kotlin.math.sin

class SceneRenderer(val engine: GameEngine) {
    val camera = Camera3D()
    private val polygonsToDraw = mutableListOf<ScreenPolygon>()

    fun render(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        dt: Float
    ) {
        // 1. Update Camera
        camera.update(engine.playerPosition, dt)

        // 2. Draw Sky & Atmosphere Gradient
        val isBoss = engine.isBossLevel
        val skyBrush = if (isBoss) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0A12),
                    Color(0xFF141424),
                    Color(0xFF1F1A30)
                )
            )
        } else {
            // Golden African Sunset
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A1F36), // Twilight Indigo
                    Color(0xFFB03A2E), // Sunset Crimson
                    Color(0xFFE59866), // Golden Warm Orange
                    Color(0xFFF9E79F)  // Horizon Yellow
                )
            )
        }
        drawScope.drawRect(brush = skyBrush)

        // 3. Prepare 3D Projections
        val viewMatrix = camera.getViewMatrix()
        val aspect = width / height
        val fov = 65f
        val projMatrix = Matrix4.createPerspective(fov, aspect, 0.5f, 250f)
        val viewProj = projMatrix * viewMatrix

        polygonsToDraw.clear()

        // 4. Project World Static Meshes
        val activeMesh = if (isBoss) engine.worldCity.bossRoomMesh else engine.worldCity.staticMesh
        projectMesh(activeMesh, viewProj, width, height)

        // 5. Project Candongueiros (Minibuses)
        if (!isBoss) {
            for (van in engine.candongueiros) {
                projectCandongueiro(van, viewProj, width, height)
            }
        }

        // 6. Project NPCs
        for (npc in engine.npcs) {
            projectNPC(npc, viewProj, width, height)
        }

        // 7. Project Capitão Angola
        projectPlayer(viewProj, width, height)

        // 8. Sort Polygons by Depth (Painter's Algorithm)
        polygonsToDraw.sortByDescending { it.depth }

        // 9. Draw all sorted polygons to Compose Canvas
        for (poly in polygonsToDraw) {
            if (poly.points.size >= 3) {
                val path = Path().apply {
                    moveTo(poly.points[0].x, poly.points[0].y)
                    for (i in 1 until poly.points.size) {
                        lineTo(poly.points[i].x, poly.points[i].y)
                    }
                    close()
                }
                drawScope.drawPath(path = path, color = poly.color)
            }
        }

        // 10. Draw Hero Aura Particle Rings if in "MODO HERÓI"
        if (engine.comboState.isHeroMode) {
            val heroScreenPos = projectPoint(engine.playerPosition + Vector3(0f, 1.1f, 0f), viewProj, width, height)
            if (heroScreenPos != null) {
                val pulse = (sin(engine.capeWaveTime.toDouble()) * 12).toFloat()
                drawScope.drawCircle(
                    color = HeroGoldAura.copy(alpha = 0.35f),
                    radius = 48f + pulse,
                    center = heroScreenPos
                )
                drawScope.drawCircle(
                    color = HeroRed.copy(alpha = 0.25f),
                    radius = 62f + pulse * 1.5f,
                    center = heroScreenPos
                )
            }
        }

        // 11. Draw Objective Beacons / Quest Markers
        val currentObj = engine.objectives.getOrNull(engine.currentObjectiveIndex)
        if (currentObj != null) {
            val beaconPos = projectPoint(currentObj.targetPos + Vector3(0f, 2.8f, 0f), viewProj, width, height)
            if (beaconPos != null) {
                val bounce = (sin(engine.capeWaveTime.toDouble() * 3.0) * 8).toFloat()
                drawScope.drawCircle(
                    color = HeroYellow,
                    radius = 12f,
                    center = Offset(beaconPos.x, beaconPos.y + bounce)
                )
                drawScope.drawCircle(
                    color = Color.White,
                    radius = 6f,
                    center = Offset(beaconPos.x, beaconPos.y + bounce)
                )
            }
        }
    }

    private fun projectMesh(mesh: Mesh3D, viewProj: Matrix4, width: Float, height: Float) {
        val lightDir = Vector3(0.4f, 0.8f, -0.4f).normalized()

        for (poly in mesh.polygons) {
            val screenPoints = mutableListOf<Offset>()
            var totalDepth = 0f
            var isValid = true

            for (idx in poly.vertexIndices) {
                val v = mesh.vertices[idx].position
                val sp = projectPoint(v, viewProj, width, height)
                if (sp == null) {
                    isValid = false
                    break
                }
                screenPoints.add(sp)
                // Calculate camera space distance
                totalDepth += (camera.eye - v).length()
            }

            if (isValid && screenPoints.size >= 3) {
                val avgDepth = totalDepth / poly.vertexIndices.size
                // Basic directional lighting
                val diffuse = poly.normal.dot(lightDir).coerceIn(0f, 1f)
                val litFactor = 0.45f + 0.55f * diffuse
                val shadedColor = Color(
                    red = (poly.baseColor.red * litFactor).coerceIn(0f, 1f),
                    green = (poly.baseColor.green * litFactor).coerceIn(0f, 1f),
                    blue = (poly.baseColor.blue * litFactor).coerceIn(0f, 1f),
                    alpha = poly.baseColor.alpha
                )

                polygonsToDraw.add(ScreenPolygon(screenPoints, avgDepth, shadedColor))
            }
        }
    }

    private fun projectCandongueiro(van: Candongueiro, viewProj: Matrix4, width: Float, height: Float) {
        val vanMesh = Mesh3D()
        val pos = van.position

        // Minibus Lower Body (Luanda Candongueiro Blue)
        vanMesh.addBox(
            center = Vector3(pos.x, pos.y + 0.5f, pos.z),
            size = Vector3(2.2f, 1.2f, 4.4f),
            color = HeroCandongueiroBlue,
            sideColor = HeroCandongueiroBlue,
            topColor = Color.White
        )
        // Upper Cabin & Roof (White with windows)
        vanMesh.addBox(
            center = Vector3(pos.x, pos.y + 1.3f, pos.z),
            size = Vector3(2.1f, 0.9f, 4.2f),
            color = Color.White,
            sideColor = Color(0xFF333D47) // Window strip
        )
        // Wheels (Black)
        vanMesh.addBox(Vector3(pos.x - 1.1f, pos.y - 0.2f, pos.z - 1.4f), Vector3(0.3f, 0.6f, 0.6f), Color.Black)
        vanMesh.addBox(Vector3(pos.x + 1.1f, pos.y - 0.2f, pos.z - 1.4f), Vector3(0.3f, 0.6f, 0.6f), Color.Black)
        vanMesh.addBox(Vector3(pos.x - 1.1f, pos.y - 0.2f, pos.z + 1.4f), Vector3(0.3f, 0.6f, 0.6f), Color.Black)
        vanMesh.addBox(Vector3(pos.x + 1.1f, pos.y - 0.2f, pos.z + 1.4f), Vector3(0.3f, 0.6f, 0.6f), Color.Black)

        projectMesh(vanMesh, viewProj, width, height)
    }

    private fun projectNPC(npc: NPC, viewProj: Matrix4, width: Float, height: Float) {
        val npcMesh = Mesh3D()
        val p = npc.position

        when (npc.type) {
            NPCType.ZUNGUEIRA -> {
                // African street vendor with colorful dress and fruit basket on head
                npcMesh.addBox(Vector3(p.x, p.y + 0.6f, p.z), Vector3(0.5f, 1.1f, 0.4f), Color(0xFFE67E22)) // Samakaka orange dress
                npcMesh.addBox(Vector3(p.x, p.y + 1.35f, p.z), Vector3(0.3f, 0.35f, 0.3f), Color(0xFF4A2E18)) // Head
                // Head Basket (Cesta de Frutas)
                npcMesh.addBox(Vector3(p.x, p.y + 1.7f, p.z), Vector3(0.85f, 0.35f, 0.85f), Color(0xFFD35400), topColor = Color(0xFF27AE60))
            }
            NPCType.CRIANCA -> {
                // Child: Smaller scale, school uniform
                npcMesh.addBox(Vector3(p.x, p.y + 0.45f, p.z), Vector3(0.4f, 0.85f, 0.35f), Color(0xFF2980B9)) // Blue uniform
                npcMesh.addBox(Vector3(p.x, p.y + 1.05f, p.z), Vector3(0.26f, 0.26f, 0.26f), Color(0xFF4A2E18)) // Head
                // School backpack
                npcMesh.addBox(Vector3(p.x, p.y + 0.5f, p.z - 0.22f), Vector3(0.32f, 0.4f, 0.16f), Color(0xFFC0392B))
            }
            NPCType.IDOSA -> {
                // Elderly lady with shawl
                npcMesh.addBox(Vector3(p.x, p.y + 0.55f, p.z), Vector3(0.52f, 1.05f, 0.45f), Color(0xFF7D3C98))
                npcMesh.addBox(Vector3(p.x, p.y + 1.3f, p.z), Vector3(0.3f, 0.3f, 0.3f), Color(0xFF4A2E18))
                // Shopping bag
                npcMesh.addBox(Vector3(p.x + 0.35f, p.y + 0.35f, p.z), Vector3(0.25f, 0.35f, 0.25f), Color(0xFFF1C40F))
            }
            NPCType.COMERCIANTE -> {
                // Shopkeeper with yellow apron
                npcMesh.addBox(Vector3(p.x, p.y + 0.65f, p.z), Vector3(0.55f, 1.25f, 0.45f), Color(0xFF27AE60))
                npcMesh.addBox(Vector3(p.x, p.y + 1.45f, p.z), Vector3(0.32f, 0.32f, 0.32f), Color(0xFF4A2E18))
            }
            NPCType.LADRAO -> {
                // Thief in dark hoodie
                val thiefColor = if (npc.state == NPCState.DEFEATED) Color(0xFF566573) else Color(0xFF17202A)
                npcMesh.addBox(Vector3(p.x, p.y + 0.65f, p.z), Vector3(0.5f, 1.25f, 0.45f), thiefColor)
                npcMesh.addBox(Vector3(p.x, p.y + 1.45f, p.z), Vector3(0.3f, 0.3f, 0.3f), Color(0xFF4A2E18))
            }
            NPCType.SEGURANCA_BOSS -> {
                // Super Mineiro Bodyguards in black suits and sunglasses
                npcMesh.addBox(Vector3(p.x, p.y + 0.75f, p.z), Vector3(0.65f, 1.45f, 0.5f), Color(0xFF111116), topColor = Color.White)
                npcMesh.addBox(Vector3(p.x, p.y + 1.65f, p.z), Vector3(0.35f, 0.35f, 0.35f), Color(0xFF4A2E18))
            }
            NPCType.SUPER_MINEIRO -> {
                // Super Mineiro: Corrupt tycoon in charcoal designer suit, gold tie, and cybernetic gauntlet
                npcMesh.addBox(Vector3(p.x, p.y + 0.75f, p.z), Vector3(0.65f, 1.45f, 0.5f), Color(0xFF1C2833), topColor = Color(0xFFF1C40F))
                npcMesh.addBox(Vector3(p.x, p.y + 1.65f, p.z), Vector3(0.35f, 0.35f, 0.35f), Color(0xFF4A2E18))
                // Gauntlet (Right Arm)
                npcMesh.addBox(Vector3(p.x + 0.45f, p.y + 0.7f, p.z), Vector3(0.25f, 0.55f, 0.25f), Color(0xFF00E5FF))
            }
        }

        projectMesh(npcMesh, viewProj, width, height)
    }

    private fun projectPlayer(viewProj: Matrix4, width: Float, height: Float) {
        val playerMesh = Mesh3D()
        val pos = engine.playerPosition
        val rotY = engine.playerRotationY

        // Body dimensions for athletic African superhero
        val torsoCenter = pos + Vector3(0f, 1.15f, 0f)

        // 1. Legs (Black Tactical Suit Pants with Red/Yellow Stripe)
        playerMesh.addBox(
            center = pos + Vector3(-0.16f, 0.45f, 0f),
            size = Vector3(0.24f, 0.85f, 0.24f),
            color = Color(0xFF15151C),
            sideColor = HeroRed
        )
        playerMesh.addBox(
            center = pos + Vector3(0.16f, 0.45f, 0f),
            size = Vector3(0.24f, 0.85f, 0.24f),
            color = Color(0xFF15151C),
            sideColor = HeroRed
        )

        // 2. Hero Boots (Deep Red with Black soles)
        playerMesh.addBox(pos + Vector3(-0.16f, 0.12f, 0.04f), Vector3(0.26f, 0.24f, 0.32f), HeroRed, sideColor = Color.Black)
        playerMesh.addBox(pos + Vector3(0.16f, 0.12f, 0.04f), Vector3(0.26f, 0.24f, 0.32f), HeroRed, sideColor = Color.Black)

        // 3. Torso (Hero Red Chest, Black Flanks, Yellow Belt & Emblem)
        playerMesh.addBox(
            center = torsoCenter,
            size = Vector3(0.6f, 0.75f, 0.36f),
            color = HeroRed,
            topColor = HeroRed,
            sideColor = Color(0xFF15151C)
        )

        // Yellow Utility Belt
        playerMesh.addBox(
            center = pos + Vector3(0f, 0.85f, 0f),
            size = Vector3(0.64f, 0.12f, 0.38f),
            color = HeroYellow
        )

        // Chest Emblem (Golden Sun & Star of Angola Motif)
        val chestFrontOffset = Vector3(sin(rotY) * 0.2f, 0f, cos(rotY) * 0.2f)
        playerMesh.addBox(
            center = torsoCenter + chestFrontOffset,
            size = Vector3(0.26f, 0.26f, 0.05f),
            color = HeroYellow
        )

        // 4. Arms & Hero Gloves (Red gauntlets)
        playerMesh.addBox(torsoCenter + Vector3(-0.38f, 0f, 0f), Vector3(0.18f, 0.65f, 0.2f), HeroRed)
        playerMesh.addBox(torsoCenter + Vector3(0.38f, 0f, 0f), Vector3(0.18f, 0.65f, 0.2f), HeroRed)

        // 5. Head (Hero Cowl / African Male Features)
        playerMesh.addBox(
            center = pos + Vector3(0f, 1.72f, 0f),
            size = Vector3(0.32f, 0.34f, 0.32f),
            color = Color(0xFF4A2E18), // Rich African Skin Tone
            topColor = Color(0xFF15151C), // Hero Cowl Top
            sideColor = Color(0xFF15151C)
        )

        // 6. Dynamic Flowing Cape (Physics & Wind Billow)
        val wave = sin(engine.capeWaveTime.toDouble()).toFloat() * 0.18f
        val capeBlowBack = if (engine.isRunning) 0.55f else 0.2f
        val capeBackOffset = Vector3(-sin(rotY) * 0.2f, 0f, -cos(rotY) * 0.2f)
        val capeAnchor = torsoCenter + Vector3(0f, 0.3f, 0f) + capeBackOffset

        playerMesh.addQuad(
            p0 = capeAnchor + Vector3(-0.26f, 0f, 0f),
            p1 = capeAnchor + Vector3(0.26f, 0f, 0f),
            p2 = capeAnchor + Vector3(0.35f + wave, -1.1f, -capeBlowBack),
            p3 = capeAnchor + Vector3(-0.35f - wave, -1.1f, -capeBlowBack),
            color = HeroRed,
            doubleSided = true
        )

        projectMesh(playerMesh, viewProj, width, height)
    }

    private fun projectPoint(point: Vector3, viewProj: Matrix4, width: Float, height: Float): Offset? {
        val p = viewProj.transform(point)
        // Check if behind camera
        val toPoint = point - camera.eye
        val forward = (camera.target - camera.eye).normalized()
        if (toPoint.dot(forward) <= 0.1f) return null

        val screenX = (p.x * 0.5f + 0.5f) * width
        val screenY = (-p.y * 0.5f + 0.5f) * height

        if (screenX < -width * 0.2f || screenX > width * 1.2f ||
            screenY < -height * 0.2f || screenY > height * 1.2f
        ) {
            return null
        }
        return Offset(screenX, screenY)
    }
}
