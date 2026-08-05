package com.example.tasknight.presentation.auth
import com.example.tasknight.domain.models.User
data class AuthState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isSignInMode: Boolean = true,
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,
    val isGuestUpgrade: Boolean = false
)