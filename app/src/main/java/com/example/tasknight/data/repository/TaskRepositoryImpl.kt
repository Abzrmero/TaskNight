package com.example.tasknight.data.repository
import com.example.tasknight.domain.models.DailyReflection
import com.example.tasknight.domain.models.Task
import com.example.tasknight.domain.repository.TaskRepository
import com.example.tasknight.data.preferences.PreferencesManager
import com.example.tasknight.presentation.set.Priority
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val firestore: FirebaseFirestore
) : TaskRepository {
    private val tasksCollection = firestore.collection("tasks")
    private val reflectionsCollection = firestore.collection("reflections")

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getTasksForDate(userId: String, date: String): Flow<List<Task>> {
        return preferencesManager.isGuest().flatMapLatest { isGuest ->
            if (isGuest) {
                preferencesManager.getGuestTasks().map { tasksJson ->
                    if (!tasksJson.isNullOrEmpty()) {
                        try {
                            val allTasks = json.decodeFromString<List<Task>>(tasksJson)
                            allTasks.filter { it.targetDate == date }
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }
            } else {
                tasksCollection
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("targetDate", date)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(Task::class.java)
                    }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getAllTasks(userId: String): Flow<List<Task>> {
        return preferencesManager.isGuest().flatMapLatest { isGuest ->
            if (isGuest) {
                preferencesManager.getGuestTasks().map { tasksJson ->
                    if (!tasksJson.isNullOrEmpty()) {
                        try {
                            json.decodeFromString<List<Task>>(tasksJson)
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }
            } else {
                tasksCollection
                    .whereEqualTo("userId", userId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(Task::class.java)
                    }
            }
        }
    }

    override suspend fun saveTasks(tasks: List<Task>) {
        val isGuest = preferencesManager.isGuest().first()
        if (isGuest) {
            val existingTasksJson = preferencesManager.getGuestTasks().first()
            val existingTasks = if (!existingTasksJson.isNullOrEmpty()) {
                try {
                    json.decodeFromString<List<Task>>(existingTasksJson).toMutableList()
                } catch (e: Exception) {
                    mutableListOf()
                }
            } else {
                mutableListOf()
            }

            tasks.forEach { task ->
                val index = existingTasks.indexOfFirst { it.id == task.id }
                if (index != -1) {
                    existingTasks[index] = task
                } else {
                    existingTasks.add(task)
                }
            }
            preferencesManager.saveGuestTasks(json.encodeToString(existingTasks))
        } else {
            val batch = firestore.batch()
            tasks.forEach { task ->
                val docRef = tasksCollection.document(task.id.ifEmpty { tasksCollection.document().id })
                val taskToSave = if (task.id.isEmpty()) task.copy(id = docRef.id) else task
                batch.set(docRef, taskToSave)
            }
            batch.commit().await()
        }
    }

    override suspend fun updateTask(task: Task) {
        val isGuest = preferencesManager.isGuest().first()
        if (isGuest) {
            saveTasks(listOf(task))
        } else {
            tasksCollection.document(task.id).set(task).await()
        }
    }

    override suspend fun deleteTask(taskId: String) {
        val isGuest = preferencesManager.isGuest().first()
        if (isGuest) {
            val existingTasksJson = preferencesManager.getGuestTasks().first()
            if (!existingTasksJson.isNullOrEmpty()) {
                try {
                    val existingTasks = json.decodeFromString<List<Task>>(existingTasksJson)
                    val newTasks = existingTasks.filter { it.id != taskId }
                    preferencesManager.saveGuestTasks(json.encodeToString(newTasks))
                } catch (e: Exception) {
                    // Handle error
                }
            }
        } else {
            tasksCollection.document(taskId).delete().await()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getDailyReflection(userId: String, date: String): Flow<DailyReflection?> {
        return preferencesManager.isGuest().flatMapLatest { isGuest ->
            if (isGuest) {
                flowOf(null)
            } else {
                reflectionsCollection
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("date", date)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.toObjects(DailyReflection::class.java).firstOrNull()
                    }
            }
        }
    }

    override suspend fun saveDailyReflection(reflection: DailyReflection) {
        val isGuest = preferencesManager.isGuest().first()
        if (isGuest) {
            // Guests are restricted from Daily Reflections based on recent actions
            return
        }
        val docId = if (reflection.id.isEmpty()) {
            "${reflection.userId}_${reflection.date}"
        } else {
            reflection.id
        }
        reflectionsCollection.document(docId).set(reflection.copy(id = docId)).await()
    }

    override suspend fun clearAllUserData(userId: String) {
        val isGuest = preferencesManager.isGuest().first()
        if (isGuest) {
            preferencesManager.saveGuestTasks("[]")
            preferencesManager.saveGuestStreak(0)
            preferencesManager.saveGuestLastActiveDate("")
        } else {
            try {
                val tasksQuery = tasksCollection.whereEqualTo("userId", userId).get().await()
                val batch = firestore.batch()
                tasksQuery.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                val reflectionsQuery = reflectionsCollection.whereEqualTo("userId", userId).get().await()
                reflectionsQuery.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()
            } catch (e: Exception) {
                e.printStackTrace()
                // Don't rethrow if it's a permission issue
            }
        }
    }
}
