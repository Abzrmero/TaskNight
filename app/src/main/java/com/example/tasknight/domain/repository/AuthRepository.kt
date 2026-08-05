package com.example.tasknight.domain.repository
import com.example.tasknight.domain.models.AuthResult
import com.example.tasknight.domain.models.User
import kotlinx.coroutines.flow.Flow
interface AuthRepository {
    suspend fun signInWithEmail(email: String, password: String): AuthResult
    suspend fun signUpWithEmail(email: String, password: String, name: String): AuthResult
    suspend fun signInAsGuest(): AuthResult
    suspend fun signOut()
    suspend fun fullLogout()
    fun isUserLoggedIn(): Flow<Boolean>
    fun isGuestUser(): Flow<Boolean>
    fun getCurrentUser(): Flow<User?>
    suspend fun setFirstTimeLaunchComplete()  
    suspend fun upgradeGuestToUser(email: String, password: String, name: String): AuthResult
    suspend fun updateUserName(userId: String, newName: String)
    suspend fun updateUserDarkMode(userId: String, isDarkMode: Boolean)
    suspend fun updateMaxTasks(userId: String, maxTasks: Int)
    suspend fun deleteAccount()
}
