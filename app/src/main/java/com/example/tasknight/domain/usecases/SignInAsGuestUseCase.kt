package com.example.tasknight.domain.usecases
import com.example.tasknight.domain.models.AuthResult
import com.example.tasknight.domain.repository.AuthRepository
class SignInAsGuestUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthResult {
        return authRepository.signInAsGuest()
    }
}