package com.example.tasknight.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasknight.data.preferences.PreferencesManager
import com.example.tasknight.domain.repository.AuthRepository
import com.example.tasknight.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val userName: String = "Guest User",
    val email: String? = null,
    val isGuest: Boolean = true,
    val isDarkMode: Boolean = true,
    val daysLogged: Int = 9,
    val tasksDone: Int = 25,
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            authRepository?.getCurrentUser()?.collectLatest { user ->
                if (user != null) {
                    _state.update { it.copy(
                        userName = user.displayName ?: "User",
                        email = user.email,
                        isGuest = user.isGuest
                    ) }
                    loadStats(user.id)
                } else {
                    _state.update { it.copy(userName = "Guest User", email = null, isGuest = true) }
                }
            }
        }

        viewModelScope.launch {
            preferencesManager.isDarkMode().collect { isDark ->
                _state.update { it.copy(isDarkMode = isDark) }
            }
        }
    }

    private fun loadStats(userId: String) {
        viewModelScope.launch {
            taskRepository.getAllTasks(userId).collectLatest { tasks ->
                val totalDone = tasks.count { it.isCompleted }
                val daysLogged = tasks.groupBy { it.targetDate }.size
                _state.update {
                    it.copy(
                        daysLogged = daysLogged,
                        tasksDone = totalDone
                    )
                }
            }
        }
    }

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            val user = authRepository?.getCurrentUser()?.first() ?: return@launch
            authRepository.updateUserName(user.id, newName)
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDarkMode(isDark)
            val user = authRepository?.getCurrentUser()?.first()
            if (user != null && !user.isGuest) {
                authRepository.updateUserDarkMode(user.id, isDark)
            }
            _state.update { it.copy(isDarkMode = isDark) }
        }
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val userId = authRepository?.getCurrentUser()?.first()?.id
            if (userId != null) {
                taskRepository.clearAllUserData(userId)
            }
            _state.update { it.copy(isLoading = false) }
            onComplete()
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                val user = authRepository?.getCurrentUser()?.first()
                val userId = user?.id

                if (userId != null) {
                    // 1. Delete all user-generated data (Tasks, Reflections)
                    taskRepository.clearAllUserData(userId)

                    // 2. Delete the user from Auth and Firestore
                    authRepository?.deleteAccount()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _state.update { it.copy(isLoading = false) }
                onComplete()
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authRepository?.signOut()
            _state.update { it.copy(isLoading = false) }
            onComplete()
        }
    }
}