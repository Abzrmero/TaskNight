package com.example.tasknight.presentation.auth
sealed class AuthEvent {
    data class EmailChanged(val email: String) : AuthEvent()
    data class PasswordChanged(val password: String) : AuthEvent()
    data class NameChanged(val name: String) : AuthEvent()
    object ToggleMode : AuthEvent()
    object SignInWithEmail : AuthEvent()
    object SignUpWithEmail : AuthEvent()
    object SignInAsGuest : AuthEvent()
    data class SetUpgradeMode(val isUpgrade: Boolean) : AuthEvent()
    object ClearError : AuthEvent()
}