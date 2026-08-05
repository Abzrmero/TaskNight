package com.example.tasknight.presentation.auth
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasknight.domain.usecases.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val signInAsGuestUseCase: SignInAsGuestUseCase,
    private val upgradeGuestToUserUseCase: UpgradeGuestToUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state
    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> {
                _state.update { it.copy(
                    email = event.email,
                    emailError = validateEmail(event.email)
                ) }
            }
            is AuthEvent.PasswordChanged -> {
                _state.update { it.copy(
                    password = event.password,
                    passwordError = validatePassword(event.password)
                ) }
            }
            is AuthEvent.NameChanged -> {
                _state.update { it.copy(
                    name = event.name,
                    nameError = validateName(event.name)
                ) }
            }
            AuthEvent.ToggleMode -> {
                _state.update { it.copy(
                    isSignInMode = !it.isSignInMode,
                    error = null,
                    emailError = null,
                    passwordError = null,
                    nameError = null
                ) }
            }
            AuthEvent.SignInWithEmail -> signInWithEmail()
            AuthEvent.SignUpWithEmail -> {
                if (_state.value.isGuestUpgrade) {
                    upgradeGuestToUser()
                } else {
                    signUpWithEmail()
                }
            }
            AuthEvent.SignInAsGuest -> signInAsGuest()
            is AuthEvent.SetUpgradeMode -> {
                _state.update { it.copy(isGuestUpgrade = event.isUpgrade) }
            }
            AuthEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
    private fun validateName(name: String): String? {
        return when {
            _state.value.isSignInMode -> null
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
    }
    private fun signInWithEmail() {
        viewModelScope.launch {
            val currentState = _state.value
            val emailError = validateEmail(currentState.email)
            val passwordError = validatePassword(currentState.password)
            if (emailError != null || passwordError != null) {
                _state.update { it.copy(
                    emailError = emailError,
                    passwordError = passwordError
                ) }
                return@launch
            }
            _state.update { it.copy(isLoading = true, error = null) }
            val result = signInWithEmailUseCase(currentState.email, currentState.password)
            _state.update {
                it.copy(
                    isLoading = false,
                    user = result.user,
                    error = result.error
                )
            }
        }
    }
    private fun signUpWithEmail() {
        viewModelScope.launch {
            val currentState = _state.value
            val emailError = validateEmail(currentState.email)
            val passwordError = validatePassword(currentState.password)
            val nameError = validateName(currentState.name)
            if (emailError != null || passwordError != null || nameError != null) {
                _state.update { it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    nameError = nameError
                ) }
                return@launch
            }
            _state.update { it.copy(isLoading = true, error = null) }
            val result = signUpWithEmailUseCase(
                currentState.email,
                currentState.password,
                currentState.name
            )
            _state.update {
                it.copy(
                    isLoading = false,
                    user = result.user,
                    error = result.error
                )
            }
        }
    }
    private fun signInAsGuest() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = signInAsGuestUseCase()
            _state.update {
                it.copy(
                    isLoading = false,
                    user = result.user,
                    error = result.error
                )
            }
        }
    }

    private fun upgradeGuestToUser() {
        viewModelScope.launch {
            val currentState = _state.value
            val emailError = validateEmail(currentState.email)
            val passwordError = validatePassword(currentState.password)
            val nameError = validateName(currentState.name)
            if (emailError != null || passwordError != null || nameError != null) {
                _state.update { it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    nameError = nameError
                ) }
                return@launch
            }
            _state.update { it.copy(isLoading = true, error = null) }
            val result = upgradeGuestToUserUseCase(
                currentState.email,
                currentState.password,
                currentState.name
            )
            _state.update {
                it.copy(
                    isLoading = false,
                    user = result.user,
                    error = result.error
                )
            }
        }
    }
}