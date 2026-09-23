package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameEngine
import com.example.game.NPCType
import com.example.ui.theme.HeroBlack
import com.example.ui.theme.HeroCardDark
import com.example.ui.theme.HeroElectricBlue
import com.example.ui.theme.HeroGoldAura
import com.example.ui.theme.HeroRed
import com.example.ui.theme.HeroSurfaceDark
import com.example.ui.theme.HeroYellow
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun GameHud(
    engine: GameEngine,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notifications by engine.notifications.collectAsState()
    var isSprintActive by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().padding(12.dp)) {
        // --- TOP BAR (Stats, Mission, Points & Combo) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Top-Left: Player Portrait & Health/Energy Bars
                PlayerStatusCard(engine = engine)

                // Top-Right: Points, Reputation & Combo Counter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ScoreAndComboCard(engine = engine)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(HeroSurfaceDark.copy(alpha = 0.85f))
                            .border(1.dp, HeroYellow.copy(alpha = 0.5f), CircleShape)
                            .testTag("menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu do Jogo",
                            tint = HeroYellow
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Top-Center: Current Mission Objective Card
            CurrentMissionBanner(engine = engine)
        }

        // --- FLOATING NOTIFICATIONS (Center Screen) ---
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            notifications.takeLast(2).forEach { notif ->
                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(HeroBlack.copy(alpha = 0.88f), HeroSurfaceDark.copy(alpha = 0.92f))
                            )
                        )
                        .border(1.5.dp, notif.color, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = notif.text,
                        color = notif.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // --- BOTTOM CONTROLS & RADAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Bottom-Left: Circular Minimap Radar
            MinimapRadar(engine = engine)

            // Bottom-Right: Action Buttons
            ActionButtonsCluster(
                engine = engine,
                isSprintActive = isSprintActive,
                onSprintToggle = {
                    isSprintActive = !isSprintActive
                }
            )
        }

        // Virtual Joystick (Bottom Left-Center area)
        VirtualMovementJoystick(
            onMove = { dx, dz ->
                engine.handleMoveInput(dx, dz, isSprintActive)
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 140.dp, bottom = 12.dp)
        )
    }
}

@Composable
private fun PlayerStatusCard(engine: GameEngine) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(HeroSurfaceDark.copy(alpha = 0.9f))
            .border(1.dp, HeroRed.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Hero Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(HeroRed)
                    .border(2.dp, HeroYellow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Capitão Angola",
                    tint = HeroYellow,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "CAPITÃO ANGOLA",
                    color = HeroYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
                Text(
                    text = "${engine.heroRankTitle} (Nível ${engine.heroRankLevel})",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Health Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "VIDA", color = HeroRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .width(85.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF331111))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(engine.playerHealth / 100f)
                                .height(6.dp)
                                .background(HeroRed)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Energy/Stamina Bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "ENERGIA", color = HeroElectricBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .width(85.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF112233))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(engine.playerEnergy / 100f)
                                .height(6.dp)
                                .background(HeroElectricBlue)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreAndComboCard(engine: GameEngine) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(HeroSurfaceDark.copy(alpha = 0.9f))
            .border(1.dp, HeroYellow.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.End) {
            // Points
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = "Pontos de Heroísmo",
                    tint = HeroYellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${engine.totalHeroismPoints} PTS",
                    color = HeroYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }

            // Reputation
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Reputação",
                    tint = HeroGoldAura,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Reputação: ${engine.reputation}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Combo Multiplier
            if (engine.comboState.count > 0) {
                val comboText = if (engine.comboState.isHeroMode) "MODO HERÓI x5!" else "Combo x${engine.comboState.multiplier}"
                val comboColor = if (engine.comboState.isHeroMode) HeroYellow else HeroRed
                Text(
                    text = comboText,
                    color = comboColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CurrentMissionBanner(engine: GameEngine) {
    val currentObj = engine.objectives.getOrNull(engine.currentObjectiveIndex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HeroBlack.copy(alpha = 0.85f))
            .border(1.dp, HeroYellow.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = "Missão",
                tint = HeroYellow,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "MISSÃO: ${currentObj?.title ?: "Patrulhar e Ajudar a Cidade"}",
                    color = HeroYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = currentObj?.description ?: "Explore os bairros e atenda ao chamado do povo.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MinimapRadar(engine: GameEngine) {
    val radarSize = 105.dp

    Box(
        modifier = Modifier
            .size(radarSize)
            .clip(CircleShape)
            .background(HeroBlack.copy(alpha = 0.85f))
            .border(2.dp, HeroRed.copy(alpha = 0.8f), CircleShape)
            .padding(6.dp)
    ) {
        // Center: Player (Gold dot)
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(HeroYellow)
                .align(Alignment.Center)
        )

        // Objective target dot (Pulsing Amber)
        val currentObj = engine.objectives.getOrNull(engine.currentObjectiveIndex)
        if (currentObj != null) {
            val relX = ((currentObj.targetPos.x - engine.playerPosition.x) / 70f).coerceIn(-0.45f, 0.45f)
            val relZ = ((currentObj.targetPos.z - engine.playerPosition.z) / 70f).coerceIn(-0.45f, 0.45f)

            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3D00))
                    .border(1.dp, Color.White, CircleShape)
                    .align(Alignment.Center)
                    .offset {
                        IntOffset(
                            (relX * 105 * 2).roundToInt(),
                            (relZ * 105 * 2).roundToInt()
                        )
                    }
            )
        }

        Text(
            text = "RADAR",
            color = HeroYellow.copy(alpha = 0.6f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun ActionButtonsCluster(
    engine: GameEngine,
    isSprintActive: Boolean,
    onSprintToggle: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        Row {
            // Sprint Button
            HeroActionButton(
                label = "CORRER",
                icon = Icons.Default.DirectionsRun,
                isActive = isSprintActive,
                activeColor = HeroElectricBlue,
                onClick = onSprintToggle,
                tag = "sprint_button"
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Dodge Button
            HeroActionButton(
                label = "ESQUIVA",
                icon = Icons.Default.Bolt,
                isActive = engine.isDodging,
                activeColor = HeroGoldAura,
                onClick = { engine.handleDodge() },
                tag = "dodge_button"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row {
            // Help / Interact Button [E]
            HeroActionButton(
                label = "AJUDAR [E]",
                icon = Icons.Default.Handshake,
                isActive = false,
                activeColor = HeroYellow,
                onClick = { engine.handleInteract() },
                tag = "interact_button"
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Attack / Strike Button [J]
            HeroActionButton(
                label = "GOLPE [J]",
                icon = Icons.Default.SportsKabaddi,
                isActive = engine.isAttacking,
                activeColor = HeroRed,
                onClick = { engine.handleAttack() },
                tag = "attack_button"
            )
        }
    }
}

@Composable
private fun HeroActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (isActive) activeColor else HeroSurfaceDark.copy(alpha = 0.88f))
            .border(2.dp, activeColor, CircleShape)
            .clickable { onClick() }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) HeroBlack else Color.White,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                color = if (isActive) HeroBlack else HeroYellow,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VirtualMovementJoystick(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = 45f

    Box(
        modifier = modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(HeroBlack.copy(alpha = 0.65f))
            .border(1.5.dp, HeroYellow.copy(alpha = 0.4f), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(50.dp.toPx(), 50.dp.toPx())
                        val diff = offset - center
                        val dist = diff.getDistance()
                        val clamped = if (dist > maxRadius) diff * (maxRadius / dist) else diff
                        thumbOffset = clamped
                        onMove(clamped.x / maxRadius, clamped.y / maxRadius)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = thumbOffset + dragAmount
                        val dist = newOffset.getDistance()
                        val clamped = if (dist > maxRadius) newOffset * (maxRadius / dist) else newOffset
                        thumbOffset = clamped
                        onMove(clamped.x / maxRadius, clamped.y / maxRadius)
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onMove(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Joystick Knob
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.x.roundToInt(), thumbOffset.y.roundToInt()) }
                .size(38.dp)
                .clip(CircleShape)
                .background(HeroRed)
                .border(2.dp, HeroYellow, CircleShape)
        )
    }
}
