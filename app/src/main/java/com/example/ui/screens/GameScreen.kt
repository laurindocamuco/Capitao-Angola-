package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.engine3d.SceneRenderer
import com.example.game.GameEngine
import com.example.game.GameLevelId
import com.example.ui.components.DialogueBox
import com.example.ui.components.FinalGameEndingOverlay
import com.example.ui.components.GameHud
import com.example.ui.components.LevelVictoryOverlay

@Composable
fun GameScreen(
    engine: GameEngine,
    onOpenMenu: () -> Unit,
    onLevelFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val renderer = remember(engine) { SceneRenderer(engine) }
    var frameTimeNanos by remember { mutableFloatStateOf(0f) }

    // Real-time 60 FPS Game Loop
    LaunchedEffect(Unit) {
        var lastTime = System.nanoTime()
        while (true) {
            withFrameNanos { now ->
                val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now
                engine.update(dt)
                frameTimeNanos = now.toFloat()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_screen")
    ) {
        // 3D Scene Canvas with Camera Drag Gesture
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Drag to orbit camera around Capitão Angola
                        renderer.camera.rotate(
                            deltaYaw = dragAmount.x * 0.008f,
                            deltaPitch = -dragAmount.y * 0.008f
                        )
                    }
                }
        ) {
            // Read frameTimeNanos to trigger recomposition on every frame
            val currentFrame = frameTimeNanos
            renderer.render(
                drawScope = this,
                width = size.width,
                height = size.height,
                dt = 0.016f
            )
        }

        // HUD Overlay (Only when not in full cutscene)
        if (!engine.isShowingDialogue && !engine.isLevelComplete && !engine.isEndingCutscene) {
            GameHud(
                engine = engine,
                onMenuClick = onOpenMenu
            )
        }

        // Dialogue Box (When cutscenes are active)
        if (engine.isShowingDialogue && engine.currentDialogueIndex in engine.activeDialogue.indices) {
            DialogueBox(
                dialogue = engine.activeDialogue[engine.currentDialogueIndex],
                onAdvance = {
                    val advanced = engine.advanceDialogue()
                    if (!advanced && engine.isLevelComplete) {
                        onLevelFinished()
                    }
                }
            )
        }

        // Normal Level Victory Overlay
        if (engine.isLevelComplete && !engine.isEndingCutscene && engine.currentLevel != GameLevelId.LEVEL_6) {
            LevelVictoryOverlay(
                engine = engine,
                onNextLevel = {
                    val nextLevelNum = engine.currentLevel.levelNumber + 1
                    val nextLvl = GameLevelId.entries.find { it.levelNumber == nextLevelNum } ?: GameLevelId.FREE_ROAM
                    engine.loadLevel(nextLvl)
                },
                onMainMenu = onOpenMenu
            )
        }

        // Final Game Ending Overlay (Capitão Angola vs Super Mineiro aftermath)
        if (engine.isEndingCutscene && !engine.isShowingDialogue) {
            FinalGameEndingOverlay(
                engine = engine,
                onFreeRoam = {
                    engine.loadLevel(GameLevelId.FREE_ROAM)
                },
                onMainMenu = onOpenMenu
            )
        }
    }
}
