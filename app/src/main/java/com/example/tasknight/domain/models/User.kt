package com.example.tasknight.domain.models
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String? = null,
    val isGuest: Boolean = false,
    val isDarkMode: Boolean = true,
    val maxTasks: Int = 5
)

@Serializable
data class AuthResult(
    val user: User?,
    val error: String?
)