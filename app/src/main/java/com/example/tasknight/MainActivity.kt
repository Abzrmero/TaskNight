package com.example.tasknight

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tasknight.data.preferences.PreferencesManager
import com.example.tasknight.domain.repository.AuthRepository
import com.example.tasknight.presentation.auth.AuthViewModel
import com.example.tasknight.presentation.dashboard.DashboardViewModel
import com.example.tasknight.presentation.settings.SettingsViewModel
import com.example.tasknight.ui.screens.auth.AuthScreen
import com.example.tasknight.ui.screens.dashboard.HomeDashboard
import com.example.tasknight.ui.screens.onboarding.OnboardingScreen
import com.example.tasknight.ui.screens.settings.SettingsScreen
import com.example.tasknight.ui.screens.history.HistoryScreen
import com.example.tasknight.ui.screens.splash.SplashScreen
import com.example.tasknight.ui.theme.TaskNightTheme
import com.example.tasknight.presentation.history.HistoryViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.tasknight.ui.screens.set.SetTasksScreen
import com.example.tasknight.presentation.set.SetTasksViewModel
import com.example.tasknight.presentation.log.LogViewModel
import com.example.tasknight.ui.screens.log.LogScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        enableEdgeToEdge()
        setContent {
        // Observe dark mode preference
        var isDarkMode by remember { mutableStateOf<Boolean?>(null) }
        LaunchedEffect(Unit) {
            preferencesManager.isDarkMode().collectLatest { isDark ->
                isDarkMode = isDark
            }
        }

        if (isDarkMode == null) return@setContent

            TaskNightTheme(darkTheme = isDarkMode ?: true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainNavigation(isDarkMode = isDarkMode ?: true)
                }
            }
        }
    }

    @Composable
    fun MainNavigation(isDarkMode: Boolean) {
        var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
        var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
        var pendingToastMessage by remember { mutableStateOf<String?>(null) }

        // Check login status
        LaunchedEffect(Unit) {
            authRepository.isUserLoggedIn().collectLatest { loggedIn ->
                isLoggedIn = loggedIn
            }
        }

        when (currentScreen) {
            Screen.Splash -> {
                SplashScreen(
                    onSplashComplete = {
                        currentScreen = when {
                            isLoggedIn == true -> Screen.Dashboard
                            else -> Screen.Onboarding
                        }
                    }
                )
            }

            Screen.Onboarding -> {
                val scope = rememberCoroutineScope()
                OnboardingScreen(
                    onOnboardingComplete = {
                        scope.launch {
                            preferencesManager.setOnboardingCompleted()
                        }
                        currentScreen = Screen.Auth
                    }
                )
            }

            Screen.Auth -> {
                val authViewModel: AuthViewModel = hiltViewModel()

                AuthScreen(
                    onAuthSuccess = {
                        currentScreen = Screen.Dashboard
                    },
                    isUpgrade = false,
                    viewModel = authViewModel
                )
            }

            Screen.UpgradeAuth -> {
                val authViewModel: AuthViewModel = hiltViewModel()

                AuthScreen(
                    onAuthSuccess = {
                        pendingToastMessage = "Account upgraded! Your data has been migrated."
                        currentScreen = Screen.Dashboard
                    },
                    isUpgrade = true,
                    viewModel = authViewModel
                )
            }

            Screen.Dashboard -> {
                val dashboardViewModel: DashboardViewModel = hiltViewModel()

                HomeDashboard(
                    isDarkMode = isDarkMode,
                    onLogout = {
                        currentScreen = Screen.Auth
                    },
                    onSignOutAndStartFresh = {
                        currentScreen = Screen.Auth
                    },
                    onNavigateToSet = {
                        currentScreen = Screen.Set
                    },
                    onNavigateToLog = {
                        currentScreen = Screen.Log
                    },
                    onNavigateToHistory = {
                        currentScreen = Screen.History
                    },
                    onNavigateToSettings = {
                        currentScreen = Screen.Settings
                    },
                    onNavigateToUpgrade = {
                        currentScreen = Screen.UpgradeAuth
                    },
                    viewModel = dashboardViewModel,
                    pendingToastMessage = pendingToastMessage,
                    onToastShown = { pendingToastMessage = null }
                )
            }

            Screen.History -> {
                val historyViewModel: HistoryViewModel = hiltViewModel()

                HistoryScreen(
                    isDarkMode = isDarkMode,
                    onNavigateToHome = { currentScreen = Screen.Dashboard },
                    onNavigateToSet = { currentScreen = Screen.Set },
                    onNavigateToLog = { currentScreen = Screen.Log },
                    onNavigateToSettings = { currentScreen = Screen.Settings },
                    viewModel = historyViewModel
                )
            }

            Screen.Log -> {
                val logViewModel: LogViewModel = hiltViewModel()

                LogScreen(
                    isDarkMode = isDarkMode,
                    onNavigateToHome = { currentScreen = Screen.Dashboard },
                    onNavigateToSet = { currentScreen = Screen.Set },
                    onNavigateToHistory = { currentScreen = Screen.History },
                    onNavigateToSettings = { currentScreen = Screen.Settings },
                    onSaveComplete = {
                        pendingToastMessage = "Daily logs saved! Progress logged."
                        currentScreen = Screen.Dashboard
                    },
                    viewModel = logViewModel
                )
            }

            Screen.Set -> {
                val setTasksViewModel: SetTasksViewModel = hiltViewModel()

                SetTasksScreen(
                    isDarkMode = isDarkMode,
                    onNavigateToHome = { currentScreen = Screen.Dashboard },
                    onNavigateToLog = { currentScreen = Screen.Log },
                    onNavigateToHistory = { currentScreen = Screen.History },
                    onNavigateToSettings = { currentScreen = Screen.Settings },
                    onSaveComplete = {
                        pendingToastMessage = "Tasks set for tomorrow! See you then."
                        currentScreen = Screen.Dashboard
                    },
                    viewModel = setTasksViewModel
                )
            }

            Screen.Settings -> {
                val settingsViewModel: SettingsViewModel = hiltViewModel()

                SettingsScreen(
                    onNavigateBack = {
                        currentScreen = Screen.Dashboard
                    },
                    onLogout = {
                        currentScreen = Screen.Auth
                    },
                    onAccountDeleted = {
                        currentScreen = Screen.Splash
                    },
                    onNavigateToHome = {
                        currentScreen = Screen.Dashboard
                    },
                    onNavigateToSet = {
                        currentScreen = Screen.Set
                    },
                    onNavigateToLog = {
                        currentScreen = Screen.Log
                    },
                    onNavigateToHistory = {
                        currentScreen = Screen.History
                    },
                    viewModel = settingsViewModel
                )
            }
        }
    }

    enum class Screen {
        Splash, Onboarding, Auth, UpgradeAuth, Dashboard, Settings, History, Set, Log
    }
}

