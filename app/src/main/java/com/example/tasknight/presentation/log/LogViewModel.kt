package com.example.tasknight.presentation.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasknight.domain.models.DailyReflection
import com.example.tasknight.domain.models.Mood
import com.example.tasknight.domain.models.Task
import com.example.tasknight.domain.repository.AuthRepository
import com.example.tasknight.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject


data class TaskLogItem(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val reflection: String = "",
    val completedAt: String? = null,
    val originalTask: Task? = null
)

data class LogState(
    val tasks: List<TaskLogItem> = emptyList(),
    val totalTasks: Int = 0,
    val completedCount: Int = 0,
    val maxTasks: Int = 5,
    val selectedMood: Mood? = null,
    val rating: Int = 0,
    val dailyNote: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val isGuest: Boolean = false
)

@HiltViewModel
class LogViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LogState())
    val state: StateFlow<LogState> = _state

    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    init {
        observeTasks()
        observeDailyReflection()
        observeGuestStatus()
    }

    private fun observeGuestStatus() {
        viewModelScope.launch {
            authRepository.isGuestUser().collect { isGuest ->
                _state.update { it.copy(isGuest = isGuest) }
            }
        }
    }

    private fun observeTasks() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user ->
                if (user == null) {
                    _state.update { it.copy(tasks = emptyList(), isLoading = false) }
                    return@collectLatest
                }

                val today = LocalDate.now().toString()
                _state.update { it.copy(isLoading = true) }

                taskRepository.getTasksForDate(user.id, today).collect { tasks ->
                    val logItems = tasks.map { task ->
                        TaskLogItem(
                            id = task.id,
                            title = task.title,
                            description = task.why,
                            isCompleted = task.isCompleted,
                            reflection = task.reflection,
                            completedAt = task.completedAt,
                            originalTask = task
                        )
                    }

                _state.update {
                    it.copy(
                        tasks = logItems,
                        totalTasks = logItems.size,
                        completedCount = logItems.count { item -> item.isCompleted },
                        maxTasks = user.maxTasks,
                        isLoading = false
                    )
                }
                }
            }
        }
    }

    private fun observeDailyReflection() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser().firstOrNull() ?: return@launch
            val today = LocalDate.now().toString()

            taskRepository.getDailyReflection(currentUser.id, today).collectLatest { reflection ->
                if (reflection != null) {
                    _state.update {
                        it.copy(
                            selectedMood = reflection.mood,
                            rating = reflection.rating,
                            dailyNote = reflection.dailyNote
                        )
                    }
                }
            }
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        val taskItem = _state.value.tasks.find { it.id == taskId } ?: return
        val originalTask = taskItem.originalTask ?: return

        viewModelScope.launch {
            val newCompletedState = !taskItem.isCompleted
            val updatedTask = originalTask.copy(
                isCompleted = newCompletedState,
                completedAt = if (newCompletedState) {
                    LocalDateTime.now().format(timeFormatter)
                } else {
                    null
                }
            )
            taskRepository.updateTask(updatedTask)
        }
    }

    fun updateTaskReflection(taskId: String, reflection: String) {
        _state.update { state ->
            state.copy(
                tasks = state.tasks.map {
                    if (it.id == taskId) it.copy(reflection = reflection) else it
                }
            )
        }
    }

    fun confirmTaskReflection(taskId: String) {
        val taskItem = _state.value.tasks.find { it.id == taskId } ?: return
        val originalTask = taskItem.originalTask ?: return

        viewModelScope.launch {
            val updatedTask = originalTask.copy(reflection = taskItem.reflection)
            taskRepository.updateTask(updatedTask)
        }
    }

    fun cancelTaskReflection(taskId: String) {
        val taskItem = _state.value.tasks.find { it.id == taskId } ?: return
        val originalReflection = taskItem.originalTask?.reflection ?: ""

        _state.update { state ->
            state.copy(
                tasks = state.tasks.map {
                    if (it.id == taskId) it.copy(reflection = originalReflection) else it
                }
            )
        }
    }

    fun selectMood(mood: Mood) {
        _state.update { it.copy(selectedMood = mood) }
    }

    fun selectRating(rating: Int) {
        _state.update { it.copy(rating = rating) }
    }

    fun updateDailyNote(note: String) {
        _state.update { it.copy(dailyNote = note) }
    }

    fun resetSaveSuccess() {
        _state.update { it.copy(saveSuccess = false) }
    }

    fun saveDailyReflection(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser().firstOrNull() ?: return@launch
            val today = LocalDate.now().toString()

            _state.update { it.copy(isSaving = true) }

            val reflection = DailyReflection(
                userId = currentUser.id,
                date = today,
                mood = _state.value.selectedMood,
                rating = _state.value.rating,
                dailyNote = _state.value.dailyNote
            )

            taskRepository.saveDailyReflection(reflection)
            _state.update { it.copy(isSaving = false, saveSuccess = true) }
            onComplete?.invoke()
        }
    }
}