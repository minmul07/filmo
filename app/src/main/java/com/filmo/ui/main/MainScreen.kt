package com.filmo.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.filmo.ui.ScreenDestination
import com.filmo.ui.home.HomeScreen
import com.filmo.ui.setting.SettingScreen
import com.filmo.ui.test.TestScreen

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    startDestination: ScreenDestination = ScreenDestination.Home,
) {
    val mainUiState by mainViewModel.mainUiState.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(startDestination)
    val navigateToSetting = {
        if (backStack.lastOrNull() != ScreenDestination.Setting) {
            backStack.add(ScreenDestination.Setting)
        }
    }
    val navigateToTest = {
        if (backStack.lastOrNull() != ScreenDestination.Test) {
            backStack.add(ScreenDestination.Test)
        }
    }
    val navigateBack = {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
    val resetToHome: () -> Unit = {
        backStack.clear()
        backStack.add(ScreenDestination.Home)
        // Unit // `or : () -> Unit`
    }

    NavDisplay(
        backStack = backStack,
        modifier = Modifier.fillMaxSize(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = navigateBack,
        entryProvider = entryProvider {
            entry<ScreenDestination.Home> {
                HomeScreen(
                    mainUiState = mainUiState,
                    onNavigateToSetting = navigateToSetting,
                    onPingClick = mainViewModel::pingServer
                )
            }

            entry<ScreenDestination.Setting> {
                SettingScreen(
                    mainUiState = mainUiState,
                    onNavigateToTest = navigateToTest,
                    onBack = navigateBack
                )
            }

            entry<ScreenDestination.Test> {
                TestScreen(
                    onResetToHome = resetToHome,
                    onBack = navigateBack
                )
            }
        }
    )
}
