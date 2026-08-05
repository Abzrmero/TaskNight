package com.example.tasknight.domain.usecases

import com.example.tasknight.domain.models.AuthResult
import com.example.tasknight.domain.repository.AuthRepository

class UpgradeGuestToUserUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String, name: String): AuthResult {
        return repository.upgradeGuestToUser(email, password, name)
    }
}
