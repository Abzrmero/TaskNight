package com.example.tasknight.domain.usecases
import com.example.tasknight.domain.models.AuthResult
import com.example.tasknight.domain.repository.AuthRepository
class SignInWithEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult {
        return authRepository.signInWithEmail(email, password)
    }
}