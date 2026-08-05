package com.example.tasknight.data.repository

import com.example.tasknight.data.preferences.PreferencesManager
import com.example.tasknight.domain.models.AuthResult
import com.example.tasknight.domain.models.User
import com.example.tasknight.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                // Get user data from Firestore
                val userDoc = usersCollection.document(firebaseUser.uid).get().await()
                val displayName = userDoc.getString("displayName") ?: firebaseUser.displayName ?: email.substringBefore("@")
                val isDarkMode = userDoc.getBoolean("isDarkMode") ?: true
                val maxTasks = userDoc.getLong("maxTasks")?.toInt() ?: 5

                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = displayName,
                    isGuest = false,
                    isDarkMode = isDarkMode,
                    maxTasks = maxTasks
                )
                preferencesManager.saveLoginState(user.id, user.email, user.displayName, false)
                preferencesManager.setDarkMode(isDarkMode)
                preferencesManager.setMaxTasks(maxTasks)
                AuthResult(user, null)
            } else {
                AuthResult(null, "Login failed: User is null")
            }
        } catch (e: Exception) {
            AuthResult(null, e.localizedMessage ?: "An error occurred during sign in")
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String, name: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                // Update Firebase profile with the name
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                firebaseUser.updateProfile(profileUpdates).await()

                val isDarkMode = preferencesManager.isDarkMode().first()
                val maxTasks = preferencesManager.getMaxTasks().first()

                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = name,
                    isGuest = false,
                    isDarkMode = isDarkMode,
                    maxTasks = maxTasks
                )

                // Save to Firestore users collection
                usersCollection.document(user.id).set(
                    mapOf(
                        "id" to user.id,
                        "email" to user.email,
                        "displayName" to user.displayName,
                        "isDarkMode" to user.isDarkMode,
                        "maxTasks" to user.maxTasks,
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()

                preferencesManager.saveLoginState(user.id, user.email, user.displayName, false)
                AuthResult(user, null)
            } else {
                AuthResult(null, "Sign up failed")
            }
        } catch (e: Exception) {
            AuthResult(null, e.localizedMessage ?: "An error occurred during sign up")
        }
    }

    override suspend fun signInAsGuest(): AuthResult {
        return try {
            val guestId = "guest_${UUID.randomUUID()}"
            val user = User(
                id = guestId,
                email = null,
                displayName = "Guest User",
                isGuest = true
            )
            preferencesManager.saveLoginState(user.id, user.email, user.displayName, true)
            AuthResult(user, null)
        } catch (e: Exception) {
            AuthResult(null, e.localizedMessage ?: "An error occurred during guest sign in")
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        preferencesManager.clearLoginState()
    }

    override suspend fun fullLogout() {
        firebaseAuth.signOut()
        preferencesManager.logoutAndClearAll()
    }

    override fun isUserLoggedIn(): Flow<Boolean> = preferencesManager.isLoggedIn()

    override fun isGuestUser(): Flow<Boolean> = preferencesManager.isGuest()

    override fun getCurrentUser(): Flow<User?> {
        return combine(
            preferencesManager.isLoggedIn(),
            preferencesManager.getUserId(),
            preferencesManager.getUserName(),
            preferencesManager.getUserEmail(),
            preferencesManager.isGuest(),
            preferencesManager.isDarkMode(),
            preferencesManager.getMaxTasks()
        ) { args: Array<Any?> ->
            val isLoggedIn = args[0] as Boolean
            val userId = args[1] as? String
            val userName = args[2] as? String
            val userEmail = args[3] as? String
            val isGuest = args[4] as Boolean
            val isDarkMode = args[5] as Boolean
            val maxTasks = args[6] as Int

            if (isLoggedIn && userId != null) {
                User(
                    id = userId,
                    email = userEmail,
                    displayName = userName,
                    isGuest = isGuest,
                    isDarkMode = isDarkMode,
                    maxTasks = maxTasks
                )
            } else {
                null
            }
        }
    }

    override suspend fun setFirstTimeLaunchComplete() {
        // Keep for backward compatibility - now sets onboarding completed
        preferencesManager.setOnboardingCompleted()
    }

    override suspend fun upgradeGuestToUser(email: String, password: String, name: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                // Update Firebase profile with the name
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                firebaseUser.updateProfile(profileUpdates).await()

                val isDarkMode = preferencesManager.isDarkMode().first()
                val maxTasks = preferencesManager.getMaxTasks().first()

                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = name,
                    isGuest = false,
                    isDarkMode = isDarkMode,
                    maxTasks = maxTasks
                )

                // Save to Firestore users collection
                usersCollection.document(user.id).set(
                    mapOf(
                        "id" to user.id,
                        "email" to user.email,
                        "displayName" to user.displayName,
                        "isDarkMode" to user.isDarkMode,
                        "maxTasks" to user.maxTasks,
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()

                // MIGRATE GUEST DATA
                val guestTasksJson = preferencesManager.getGuestTasks().first()
                if (guestTasksJson != null) {
                    val jsonArray = JSONArray(guestTasksJson)
                    val batch = firestore.batch()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        // Create Task object and update userId to the new real userId
                        val taskDocRef = firestore.collection("tasks").document()
                        val taskMap = mutableMapOf<String, Any>(
                            "id" to taskDocRef.id,
                            "userId" to user.id,
                            "title" to obj.getString("title"),
                            "why" to obj.getString("why"),
                            "priority" to obj.getString("priority"),
                            "isCompleted" to obj.getBoolean("isCompleted"),
                            "reflection" to obj.getString("reflection"),
                            "targetDate" to obj.getString("targetDate")
                        )
                        if (!obj.isNull("completedAt")) {
                            taskMap["completedAt"] = obj.getString("completedAt")
                        }
                        batch.set(taskDocRef, taskMap)
                    }
                    batch.commit().await()
                }

                preferencesManager.saveLoginState(user.id, user.email, user.displayName, false)
                // Clear guest tasks after migration
                preferencesManager.saveGuestTasks("[]")
                
                AuthResult(user, null)
            } else {
                AuthResult(null, "Upgrade failed")
            }
        } catch (e: Exception) {
            AuthResult(null, e.localizedMessage ?: "An error occurred during upgrade")
        }
    }

    override suspend fun updateUserName(userId: String, newName: String) {
        try {
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null && firebaseUser.uid == userId) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = newName
                }
                firebaseUser.updateProfile(profileUpdates).await()

                // Update Firestore
                usersCollection.document(userId).update("displayName", newName).await()

                // Also update preferences
                val currentEmail = preferencesManager.getUserEmail().first()
                val isGuest = preferencesManager.isGuest().first()
                preferencesManager.saveLoginState(userId, currentEmail, newName, isGuest)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateUserDarkMode(userId: String, isDarkMode: Boolean) {
        preferencesManager.setDarkMode(isDarkMode)
        try {
            val isGuest = preferencesManager.isGuest().first()
            if (!isGuest) {
                usersCollection.document(userId).update("isDarkMode", isDarkMode).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateMaxTasks(userId: String, maxTasks: Int) {
        preferencesManager.setMaxTasks(maxTasks)
        try {
            val isGuest = preferencesManager.isGuest().first()
            if (!isGuest) {
                usersCollection.document(userId).update("maxTasks", maxTasks).await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteAccount() {
        try {
            val firebaseUser = firebaseAuth.currentUser
            val userId = firebaseUser?.uid
            
            if (userId != null) {
                // Delete user document from Firestore
                usersCollection.document(userId).delete().await()
                
                // Delete the Firebase Auth user
                firebaseUser.delete().await()
            }
            
            // Clear local preferences
            preferencesManager.logoutAndClearAll()
            // Ensure sign out
            firebaseAuth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
