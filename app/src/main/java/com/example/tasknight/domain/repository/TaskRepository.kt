package com.example.tasknight.domain.repository
import com.example.tasknight.domain.models.DailyReflection
import com.example.tasknight.domain.models.Task
import kotlinx.coroutines.flow.Flow
interface TaskRepository {
    fun getTasksForDate(userId: String, date: String): Flow<List<Task>>
    fun getAllTasks(userId: String): Flow<List<Task>>
    suspend fun saveTasks(tasks: List<Task>)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: String)
    fun getDailyReflection(userId: String, date: String): Flow<DailyReflection?>
    suspend fun saveDailyReflection(reflection: DailyReflection)
    suspend fun clearAllUserData(userId: String)
}
