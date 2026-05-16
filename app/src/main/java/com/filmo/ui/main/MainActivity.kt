package com.filmo.ui.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.filmo.ui.AppDestination
import com.filmo.ui.ScreenDestination
import com.filmo.ui.replaceRoot
import com.filmo.ui.replaceWithMain
import com.filmo.ui.setup.InitialSetupScreen
import com.filmo.ui.setup.InitialSetupUiState
import com.filmo.ui.setup.SplashScreen
import com.filmo.ui.theme.FilmoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateRequestedOrientation()
        enableEdgeToEdge()

        setContent {
            FilmoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val isInitialSetupFinished by mainViewModel.isInitialSetupFinished.collectAsState()
                    val initialSetupUiState by mainViewModel.initialSetupUiState.collectAsState()
                    FilmoAppNavHost(
                        isInitialSetupFinished = isInitialSetupFinished,
                        initialSetupUiState = initialSetupUiState,
                        onAutoNicknameClick = mainViewModel::startAutomaticNicknameSetup,
                        onManualNicknameClick = mainViewModel::startManualNicknameSetup,
                        onRegenerateNicknameClick = mainViewModel::startAutomaticNicknameSetup,
                        onBackToEntryChoice = mainViewModel::backToInitialSetupEntryChoice,
                        onNicknameChange = mainViewModel::updateInitialSetupNickname,
                        onSaveClick = mainViewModel::saveInitialSetupNickname
                    )
                }
            }
        }
    }

    private fun updateRequestedOrientation() {
        requestedOrientation = if (resources.configuration.smallestScreenWidthDp < 600) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_FULL_USER
        }
    }
}

@Composable
private fun FilmoAppNavHost(
    isInitialSetupFinished: Boolean?,
    initialSetupUiState: InitialSetupUiState,
    onAutoNicknameClick: () -> Unit,
    onManualNicknameClick: () -> Unit,
    onRegenerateNicknameClick: () -> Unit,
    onBackToEntryChoice: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    val rootBackStack = rememberNavBackStack(AppDestination.Splash)

    LaunchedEffect(isInitialSetupFinished) {
        rootBackStack.replaceRoot(
            when (isInitialSetupFinished) {
                null -> AppDestination.Splash
                true -> AppDestination.Main
                false -> AppDestination.InitialSetup
            }
        )
    }

    NavDisplay(
        backStack = rootBackStack,
        modifier = Modifier.fillMaxSize(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<AppDestination.Splash> {
                SplashScreen()
            }

            entry<AppDestination.InitialSetup> {
                InitialSetupScreen(
                    uiState = initialSetupUiState,
                    onAutoNicknameClick = onAutoNicknameClick,
                    onManualNicknameClick = onManualNicknameClick,
                    onRegenerateNicknameClick = onRegenerateNicknameClick,
                    onBackToEntryChoice = onBackToEntryChoice,
                    onNicknameChange = onNicknameChange,
                    onSaveClick = onSaveClick,
                    onFinished = rootBackStack::replaceWithMain
                )
            }

            entry<AppDestination.Main> {
                MainScreen(
                    startDestination = ScreenDestination.Feed
                )
            }
        }
    )
}
