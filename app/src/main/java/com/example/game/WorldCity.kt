package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.engine3d.Mesh3D
import com.example.engine3d.Vector3
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class WorldCity {
    val staticMesh = Mesh3D()
    val bossRoomMesh = Mesh3D()

    init {
        buildCityEnvironment()
        buildBossPenthouse()
    }

    private fun buildCityEnvironment() {
        // --- 1. Ground & Avenues ---
        // Asphalt Avenue (-8f to 8f on X, from -120f to 120f on Z)
        staticMesh.addBox(
            center = Vector3(0f, -0.05f, 0f),
            size = Vector3(16f, 0.1f, 240f),
            color = Color(0xFF282830),
            topColor = Color(0xFF2F3136)
        )

        // Sidewalks (West and East)
        staticMesh.addBox(
            center = Vector3(-11f, 0.08f, 0f),
            size = Vector3(6f, 0.16f, 240f),
            color = Color(0xFF8E8D8A),
            topColor = Color(0xFFA8A7A3)
        )
        staticMesh.addBox(
            center = Vector3(11f, 0.08f, 0f),
            size = Vector3(6f, 0.16f, 240f),
            color = Color(0xFF8E8D8A),
            topColor = Color(0xFFA8A7A3)
        )

        // Road Lane Center Stripes (Yellow)
        for (z in -100..100 step 12) {
            staticMesh.addBox(
                center = Vector3(0f, 0.02f, z.toFloat()),
                size = Vector3(0.4f, 0.02f, 6f),
                color = Color(0xFFFFD100)
            )
        }

        // School Pedestrian Crossing (Passadeira de Peões at Z = -15)
        for (x in -6..6 step 2) {
            staticMesh.addBox(
                center = Vector3(x.toFloat(), 0.02f, -15f),
                size = Vector3(1.2f, 0.02f, 4f),
                color = Color(0xFFFFFFFF)
            )
        }

        // --- 2. Buildings (East Side: Commercial & Colonial) ---
        val buildingColors = listOf(
            Color(0xFFE2725B), // Terracotta
            Color(0xFFF4D03F), // Warm Ochre
            Color(0xFF5DADE2), // Colonial Blue
            Color(0xFFECF0F1), // Modern White
            Color(0xFFD35400)  // Deep Rust
        )

        for (i in 0 until 8) {
            val zPos = -100f + i * 28f
            val height = 12f + (i % 3) * 6f
            val col = buildingColors[i % buildingColors.size]
            staticMesh.addBox(
                center = Vector3(22f, height * 0.5f, zPos),
                size = Vector3(12f, height, 22f),
                color = col,
                topColor = Color(0xFF4A4A5A),
                sideColor = col
            )
        }

        // --- 3. Buildings (West Side: Market & School & Super Mineiro Skyscraper) ---
        // School Complex (at Z = -35f)
        staticMesh.addBox(
            center = Vector3(-24f, 6f, -30f),
            size = Vector3(16f, 12f, 32f),
            color = Color(0xFFF39C12),
            topColor = Color(0xFF962D00)
        )
        // School Entrance Sign / Wall
        staticMesh.addBox(
            center = Vector3(-13.5f, 1.5f, -15f),
            size = Vector3(0.8f, 3f, 8f),
            color = Color(0xFF2C3E50)
        )

        // Popular Market Area (Z = 10f to 50f on West Side)
        staticMesh.addBox(
            center = Vector3(-24f, 0.1f, 30f),
            size = Vector3(18f, 0.12f, 40f),
            color = Color(0xFFC49A6C) // Sandy open market ground
        )

        // Market Stalls (Bancadas do Mercado)
        for (row in 0 until 4) {
            for (col in 0 until 3) {
                val stallZ = 16f + row * 8f
                val stallX = -17f - col * 5f
                // Wooden table
                staticMesh.addBox(
                    center = Vector3(stallX, 0.6f, stallZ),
                    size = Vector3(2.4f, 1.1f, 1.8f),
                    color = Color(0xFF8B5A2B),
                    topColor = Color(0xFFA06D3B)
                )
                // Colorful canopy awning (Red/Yellow or Green/White striped)
                val awningColor = if ((row + col) % 2 == 0) Color(0xFFC8102E) else Color(0xFFFFD100)
                staticMesh.addBox(
                    center = Vector3(stallX, 2.2f, stallZ),
                    size = Vector3(2.6f, 0.2f, 2.2f),
                    color = awningColor
                )
            }
        }

        // Super Mineiro Corporate Skyscraper (at Z = 85f, West side)
        staticMesh.addBox(
            center = Vector3(-28f, 32f, 85f),
            size = Vector3(22f, 64f, 26f),
            color = Color(0xFF1B2631),
            topColor = Color(0xFFD4AC0D), // Gold roof heli-pad rim
            sideColor = Color(0xFF212F3D)
        )

        // Corporate glass spire on top of skyscraper
        staticMesh.addBox(
            center = Vector3(-28f, 68f, 85f),
            size = Vector3(4f, 8f, 4f),
            color = Color(0xFFFFD700)
        )

        // --- 4. Palm Trees along the avenue ---
        for (z in -90..90 step 25) {
            addPalmTree(staticMesh, Vector3(-13.5f, 0f, z.toFloat()))
            addPalmTree(staticMesh, Vector3(13.5f, 0f, z + 12f))
        }
    }

    private fun addPalmTree(mesh: Mesh3D, root: Vector3) {
        // Trunk
        mesh.addBox(
            center = Vector3(root.x, root.y + 2.5f, root.z),
            size = Vector3(0.5f, 5f, 0.5f),
            color = Color(0xFF6E4720),
            topColor = Color(0xFF5A3918)
        )
        // Fronds / Palm Crown (overlapping triangular fan planes)
        for (i in 0 until 6) {
            val angle = i * (PI.toFloat() / 3f)
            val dx = cos(angle) * 2.2f
            val dz = sin(angle) * 2.2f
            mesh.addTriangle(
                p0 = Vector3(root.x, root.y + 5f, root.z),
                p1 = Vector3(root.x + dx, root.y + 4.2f, root.z + dz),
                p2 = Vector3(root.x + dx * 1.3f, root.y + 3.4f, root.z + dz * 1.3f),
                color = Color(0xFF1E824C)
            )
        }
    }

    private fun buildBossPenthouse() {
        // Penthouse Office: Luxury executive room at the top of the skyscraper
        // Dark polished marble floor
        bossRoomMesh.addBox(
            center = Vector3(0f, -0.1f, 0f),
            size = Vector3(36f, 0.2f, 36f),
            color = Color(0xFF111116),
            topColor = Color(0xFF1C1C24)
        )

        // Executive mahogany desk of Super Mineiro
        bossRoomMesh.addBox(
            center = Vector3(0f, 0.8f, 8f),
            size = Vector3(7f, 1.5f, 3f),
            color = Color(0xFF4A1512),
            topColor = Color(0xFF5E1B17)
        )

        // Gold executive chair behind desk
        bossRoomMesh.addBox(
            center = Vector3(0f, 1.4f, 10.5f),
            size = Vector3(2f, 2.8f, 1.8f),
            color = Color(0xFFD4AF37)
        )

        // Steel Safe Vaults in the corners
        bossRoomMesh.addBox(
            center = Vector3(-12f, 2.5f, 12f),
            size = Vector3(3f, 5f, 3f),
            color = Color(0xFF34495E),
            sideColor = Color(0xFF2C3E50)
        )
        bossRoomMesh.addBox(
            center = Vector3(12f, 2.5f, 12f),
            size = Vector3(3f, 5f, 3f),
            color = Color(0xFF34495E),
            sideColor = Color(0xFF2C3E50)
        )

        // Luxury Tech Hologram Pillars / Shield Emitters
        bossRoomMesh.addBox(
            center = Vector3(-8f, 3f, 0f),
            size = Vector3(1.4f, 6f, 1.4f),
            color = Color(0xFF16A085),
            topColor = Color(0xFF1ABC9C)
        )
        bossRoomMesh.addBox(
            center = Vector3(8f, 3f, 0f),
            size = Vector3(1.4f, 6f, 1.4f),
            color = Color(0xFF16A085),
            topColor = Color(0xFF1ABC9C)
        )

        // Panoramic Glass Wall (Back overlooking city lights)
        bossRoomMesh.addBox(
            center = Vector3(0f, 6f, 17.5f),
            size = Vector3(36f, 12f, 0.5f),
            color = Color(0x3300D2FF) // Translucent blue tint glass
        )
    }
}
