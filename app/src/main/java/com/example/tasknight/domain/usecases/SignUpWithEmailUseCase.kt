package com.example.tasknight.domain.usecases
import com.example.tasknight.domain.models.AuthResult
import com.example.tasknight.domain.repository.AuthRepository
class SignUpWithEmailUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String): AuthResult {
        return authRepository.signUpWithEmail(email, password, name)
    }
}