package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.game.GameEngine
import com.example.game.GameLevelId
import com.example.ui.theme.HeroBlack
import com.example.ui.theme.HeroCardDark
import com.example.ui.theme.HeroGoldAura
import com.example.ui.theme.HeroRed
import com.example.ui.theme.HeroSurfaceDark
import com.example.ui.theme.HeroYellow

@Composable
fun LevelVictoryOverlay(
    engine: GameEngine,
    onNextLevel: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HeroBlack.copy(alpha = 0.92f))
            .padding(20.dp)
            .testTag("victory_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = HeroSurfaceDark),
            border = androidx.compose.foundation.BorderStroke(2.dp, HeroYellow)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = HeroYellow,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "MISSÃO CONCLUÍDA!",
                    color = HeroYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )

                Text(
                    text = "+500 PONTOS DE HEROÍSMO SEM DANOS A CIVIS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(HeroCardDark)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Patente: ${engine.heroRankTitle}",
                        color = HeroYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Pontuação Total: ${engine.totalHeroismPoints} PTS",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Reputação com a Comunidade: ${engine.reputation} ★",
                        color = HeroGoldAura,
                        fontSize = 13.sp
                    )
                    if (engine.fullSuitUnlocked) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ Uniforme Completo do Capitão Angola Desbloqueado!",
                            color = HeroYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onMainMenu,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("menu_return_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = HeroCardDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Menu", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Menu", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("next_level_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = HeroRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Avançar", color = Color.White, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Avançar", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun FinalGameEndingOverlay(
    engine: GameEngine,
    onFreeRoam: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HeroBlack)
            .testTag("final_ending_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Ending Banner Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(2.dp, HeroYellow, RoundedCornerShape(18.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ending_banner),
                    contentDescription = "O Capitão e a Criança",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CAPITÃO ANGOLA",
                    color = HeroYellow,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "“CORAGEM PARA SERVIR.”",
                    color = HeroRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "“Um verdadeiro herói não é aquele que possui mais poder.\nÉ aquele que usa o seu poder para ajudar os outros.”",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(HeroCardDark)
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "CAPITÃO ANGOLA — UMA NOVA AMEAÇA SURGE...",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            // Action Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onFreeRoam,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("free_roam_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = HeroYellow),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = HeroBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENTRAR NO MODO PATRULHA LIVRE",
                        color = HeroBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onMainMenu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("final_menu_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = HeroSurfaceDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "Voltar ao Menu Principal", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
