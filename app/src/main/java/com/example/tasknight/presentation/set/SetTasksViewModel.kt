package com.example.tasknight.presentation.set

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasknight.domain.models.Task
import com.example.tasknight.domain.repository.AuthRepository
import com.example.tasknight.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.collect
import com.example.tasknight.domain.models.User
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import javax.inject.Inject

data class TaskItem(
    val id: String,
    val title: String,
    val why: String,
    val priority: Priority,
    val isSaved: Boolean = false,
    val isModified: Boolean = false
)

data class SetTasksState(
    val tasks: List<TaskItem> = emptyList(),
    val savedCount: Int = 0,
    val maxTasks: Int = 5,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class SetTasksViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SetTasksState())
    val state: StateFlow<SetTasksState> = _state

    private var currentUserId: String? = null
    private val tomorrowDate = LocalDate.now().plusDays(1).toString()

    init {
        observeTasks()
    }

    private var lastSavedTasks: List<Task> = emptyList()

    private fun observeTasks() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user: User? ->
                currentUserId = user?.id
                if (user != null) {
                    _state.update { it.copy(maxTasks = user.maxTasks) }
                    taskRepository.getTasksForDate(user.id, tomorrowDate).collect { savedTasks ->
                        lastSavedTasks = savedTasks
                        _state.update { state: SetTasksState ->
                            // Update savedCount from DB
                            val newSavedCount = savedTasks.size

                            // Merge saved tasks into local state
                            val localTasks = state.tasks.toMutableList()

                            // 1. Remove tasks that are marked as saved but not in the DB anymore
                            localTasks.removeAll { local ->
                                local.isSaved && savedTasks.none { it.id == local.id }
                            }

                            // 2. Update or add tasks from DB
                            savedTasks.forEach { saved ->
                                val index = localTasks.indexOfFirst { it.id == saved.id }
                                val taskItem = TaskItem(
                                    id = saved.id,
                                    title = saved.title,
                                    why = saved.why,
                                    priority = saved.priority,
                                    isSaved = true,
                                    isModified = false
                                )
                                if (index != -1) {
                                    if (!localTasks[index].isModified) {
                                        localTasks[index] = taskItem
                                    }
                                } else {
                                    localTasks.add(taskItem)
                                }
                            }

                            // If everything is empty, add one initial task
                            if (localTasks.isEmpty() && newSavedCount == 0) {
                                localTasks.add(createEmptyTask())
                            }

                            state.copy(
                                tasks = localTasks.toList(),
                                savedCount = newSavedCount
                            )
                        }
                    }
                }
            }
        }
    }

    private fun createEmptyTask() = TaskItem(
        id = UUID.randomUUID().toString(),
        title = "",
        why = "",
        priority = Priority.MEDIUM,
        isSaved = false,
        isModified = false
    )

    fun addTask() {
        if (_state.value.tasks.size < _state.value.maxTasks) {
            _state.update { it.copy(tasks = it.tasks + createEmptyTask()) }
        }
    }

    fun confirmTask(taskId: String) {
        val taskItem = _state.value.tasks.find { it.id == taskId } ?: return
        if (taskItem.title.isBlank()) return

        val userId = currentUserId ?: return

        viewModelScope.launch {
            val task = Task(
                id = taskItem.id,
                userId = userId,
                title = taskItem.title,
                why = taskItem.why,
                priority = taskItem.priority,
                isCompleted = false,
                targetDate = tomorrowDate
            )

            if (taskItem.isSaved) {
                taskRepository.updateTask(task)
            } else {
                taskRepository.saveTasks(listOf(task))
            }

            // Local state will be updated via observeTasks collector
            _state.update { state ->
                state.copy(
                    tasks = state.tasks.map {
                        if (it.id == taskId) it.copy(isModified = false, isSaved = true) else it
                    }
                )
            }
        }
    }

    fun cancelEdit(taskId: String) {
        _state.update { state ->
            val task = state.tasks.find { it.id == taskId }
            if (task != null && !task.isSaved) {
                // If it was never saved, remove it
                state.copy(tasks = state.tasks.filter { it.id != taskId })
            } else {
                // Revert to database state
                val originalTask = lastSavedTasks.find { it.id == taskId }
                state.copy(
                    tasks = state.tasks.map {
                        if (it.id == taskId && originalTask != null) {
                            it.copy(
                                title = originalTask.title,
                                why = originalTask.why,
                                priority = originalTask.priority,
                                isModified = false
                            )
                        } else if (it.id == taskId) {
                            it.copy(isModified = false)
                        } else it
                    }
                )
            }
        }
    }

    fun deleteTask(taskId: String) {
        val taskItem = _state.value.tasks.find { it.id == taskId } ?: return

        if (taskItem.isSaved) {
            viewModelScope.launch {
                taskRepository.deleteTask(taskId)
            }
        }

        _state.update { state ->
            state.copy(tasks = state.tasks.filter { it.id != taskId })
        }
    }

    fun updateTaskTitle(taskId: String, title: String) {
        _state.update { state ->
            state.copy(
                tasks = state.tasks.map { task ->
                    if (task.id == taskId) task.copy(title = title, isModified = true) else task
                }
            )
        }
    }

    fun updateTaskWhy(taskId: String, why: String) {
        _state.update { state ->
            state.copy(
                tasks = state.tasks.map { task ->
                    if (task.id == taskId) task.copy(why = why, isModified = true) else task
                }
            )
        }
    }

    fun updateTaskPriority(taskId: String, priority: Priority) {
        _state.update { state ->
            state.copy(
                tasks = state.tasks.map { task ->
                    if (task.id == taskId) task.copy(priority = priority, isModified = true) else task
                }
            )
        }
    }

    fun updateMaxTasks(newMax: Int) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            authRepository.updateMaxTasks(userId, newMax)
            _state.update { it.copy(maxTasks = newMax) }
        }
    }

    fun moveTask(from: Int, to: Int) {
        if (from < 0 || from >= _state.value.tasks.size ||
            to < 0 || to >= _state.value.tasks.size) {
            return
        }

        _state.update { state ->
            val mutableList = state.tasks.toMutableList()
            val item = mutableList.removeAt(from)
            mutableList.add(to, item)
            state.copy(tasks = mutableList)
        }
    }

    fun saveTasks(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            val userId = currentUserId ?: return@launch
            val modifiedTasks = _state.value.tasks.filter { it.isModified && it.title.isNotBlank() }

            if (modifiedTasks.isNotEmpty()) {
                val tasksToSave = modifiedTasks.map { item ->
                    Task(
                        id = item.id,
                        userId = userId,
                        title = item.title,
                        why = item.why,
                        priority = item.priority,
                        isCompleted = false,
                        targetDate = tomorrowDate
                    )
                }
                taskRepository.saveTasks(tasksToSave)

                _state.update { state ->
                    state.copy(
                        tasks = state.tasks.map { task ->
                            if (task.isModified && task.title.isNotBlank()) {
                                task.copy(isModified = false, isSaved = true)
                            } else task
                        },
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } else {
                if (_state.value.tasks.any { it.title.isNotBlank() }) {
                    _state.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    _state.update { it.copy(isSaving = false) }
                }
            }
            onComplete?.invoke()
        }
    }

    fun resetSaveSuccess() {
        _state.update { it.copy(saveSuccess = false) }
    }
}

class ReorderableLazyListState(
    val lazyListState: androidx.compose.foundation.lazy.LazyListState,
    val onMove: (ItemPosition, ItemPosition) -> Unit
) {
    val draggableState = androidx.compose.foundation.gestures.DraggableState { }
    val interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource()
    var isDragging by mutableStateOf(false)
        private set
}

data class ItemPosition(val index: Int, val key: Any)