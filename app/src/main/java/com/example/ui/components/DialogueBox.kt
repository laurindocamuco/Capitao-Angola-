package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.DialogueLine
import com.example.ui.theme.HeroBlack
import com.example.ui.theme.HeroCardDark
import com.example.ui.theme.HeroRed
import com.example.ui.theme.HeroYellow

@Composable
fun DialogueBox(
    dialogue: DialogueLine?,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (dialogue == null) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { onAdvance() }
            .testTag("dialogue_screen")
    ) {
        // Cinematic Top & Bottom Letterbox Bars
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(Color.Black)
                .align(Alignment.TopCenter)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(Color.Black)
                .align(Alignment.BottomCenter)
        )

        // Dialogue Box (Positioned at bottom center)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 44.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(18.dp))
                .background(HeroBlack.copy(alpha = 0.94f))
                .border(1.5.dp, if (dialogue.isVillain) Color(0xFF00E5FF) else HeroYellow, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarIcon = when {
                        dialogue.speaker.contains("SUPER") -> Icons.Default.Business
                        dialogue.speaker.contains("CRIANÇA") -> Icons.Default.ChildCare
                        else -> Icons.Default.Shield
                    }
                    val badgeColor = when {
                        dialogue.speaker.contains("SUPER") -> Color(0xFF00E5FF)
                        dialogue.speaker.contains("CRIANÇA") -> Color(0xFF4FC3F7)
                        else -> HeroYellow
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (dialogue.isVillain) Color(0xFF1B2631) else HeroRed)
                            .border(1.5.dp, badgeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = avatarIcon,
                            contentDescription = dialogue.speaker,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = dialogue.speaker,
                        color = badgeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = dialogue.text,
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Toque para continuar",
                        color = HeroYellow.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Avançar",
                        tint = HeroYellow.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
