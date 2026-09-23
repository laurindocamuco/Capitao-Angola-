package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.GameViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.theme.HeroBlack
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    MAIN_MENU,
    GAMEPLAY
}

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = HeroBlack
                ) {
                    CapitaoAngolaApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveCurrentProgress()
        viewModel.soundEngine.stopAmbientRhythm()
    }

    override fun onResume() {
        super.onResume()
        viewModel.soundEngine.startAmbientRhythm(isBoss = viewModel.engine.isBossLevel)
    }
}

@Composable
fun CapitaoAngolaApp(viewModel: GameViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_MENU) }
    val achievements by viewModel.achievements.collectAsState()

    BackHandler(enabled = currentScreen == AppScreen.GAMEPLAY) {
        viewModel.saveCurrentProgress()
        currentScreen = AppScreen.MAIN_MENU
    }

    when (currentScreen) {
        AppScreen.MAIN_MENU -> {
            MainMenuScreen(
                engine = viewModel.engine,
                achievements = achievements,
                onStartLevel = { level ->
                    viewModel.startLevel(level)
                    currentScreen = AppScreen.GAMEPLAY
                }
            )
        }
        AppScreen.GAMEPLAY -> {
            GameScreen(
                engine = viewModel.engine,
                onOpenMenu = {
                    viewModel.saveCurrentProgress()
                    currentScreen = AppScreen.MAIN_MENU
                },
                onLevelFinished = {
                    viewModel.saveCurrentProgress()
                }
            )
        }
    }
}
