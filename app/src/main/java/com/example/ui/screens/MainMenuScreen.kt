package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Achievement
import com.example.game.GameEngine
import com.example.game.GameLevelId
import com.example.ui.components.AchievementsDialog
import com.example.ui.theme.HeroBlack
import com.example.ui.theme.HeroCardDark
import com.example.ui.theme.HeroGoldAura
import com.example.ui.theme.HeroRed
import com.example.ui.theme.HeroSurfaceDark
import com.example.ui.theme.HeroYellow

@Composable
fun MainMenuScreen(
    engine: GameEngine,
    achievements: List<Achievement>,
    onStartLevel: (GameLevelId) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLevelSelector by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(engine.soundEngine.isMuted) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HeroBlack)
            .testTag("main_menu_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Hero Rank & Audio Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = HeroYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${engine.heroRankTitle} • ${engine.totalHeroismPoints} PTS",
                        color = HeroYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            engine.soundEngine.isMuted = isMuted
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(HeroSurfaceDark)
                            .border(1.dp, HeroBorder(), CircleShape)
                            .testTag("mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = "Áudio",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Hero Key Art Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, HeroYellow.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_banner),
                    contentDescription = "Capitão Angola Key Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for text
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, HeroBlack.copy(alpha = 0.85f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CAPITÃO ANGOLA",
                        color = HeroYellow,
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "CORAGEM PARA SERVIR",
                        color = HeroRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(HeroSurfaceDark)
                    .border(1.dp, HeroBorder(), RoundedCornerShape(14.dp))
                    .padding(vertical = 8.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MenuStatItem("Pessoas Ajudadas", "${engine.peopleHelped}")
                MenuStatItem("Crimes Impedidos", "${engine.crimesStopped}")
                MenuStatItem("Reputação", "${engine.reputation} ★")
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onStartLevel(GameLevelId.LEVEL_1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_campaign_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = HeroRed),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INICIAR CAMPANHA",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showLevelSelector = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("select_level_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = HeroSurfaceDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Fases", color = HeroYellow, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showAchievements = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("achievements_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = HeroSurfaceDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = HeroGoldAura, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Conquistas", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Level Selector Modal
        if (showLevelSelector) {
            LevelSelectorDialog(
                onSelectLevel = { lvl ->
                    showLevelSelector = false
                    onStartLevel(lvl)
                },
                onDismiss = { showLevelSelector = false }
            )
        }

        // Achievements Modal
        if (showAchievements) {
            AchievementsDialog(
                engine = engine,
                achievements = achievements,
                onDismiss = { showAchievements = false }
            )
        }
    }
}

@Composable
private fun MenuStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = HeroYellow, fontWeight = FontWeight.Black, fontSize = 15.sp)
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}

@Composable
private fun LevelSelectorDialog(
    onSelectLevel: (GameLevelId) -> Unit,
    onDismiss: () -> Unit
) {
    val levels = listOf(
        GameLevelId.LEVEL_1,
        GameLevelId.LEVEL_2,
        GameLevelId.LEVEL_3,
        GameLevelId.LEVEL_4,
        GameLevelId.LEVEL_5,
        GameLevelId.LEVEL_6,
        GameLevelId.FREE_ROAM
    )

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("level_selector_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = HeroSurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Text(
                    text = "SELECIONAR MISSÃO",
                    color = HeroYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.height(320.dp)) {
                    items(levels) { lvl ->
                        LevelRowItem(level = lvl, onClick = { onSelectLevel(lvl) })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HeroCardDark)
                ) {
                    Text(text = "Voltar", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun LevelRowItem(level: GameLevelId, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HeroCardDark)
            .clickable { onClick() }
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${level.levelNumber}. ${level.title}",
                color = HeroYellow,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = level.subtitle,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(HeroRed.copy(alpha = 0.25f))
                .border(1.dp, HeroRed, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = level.difficulty,
                color = HeroYellow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HeroBorder() = Color(0xFF333347)
