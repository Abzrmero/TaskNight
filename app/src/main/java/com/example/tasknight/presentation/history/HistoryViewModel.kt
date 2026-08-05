package com.example.tasknight.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.tasknight.domain.models.DailyReflection
import com.example.tasknight.domain.models.Task
import com.example.tasknight.domain.repository.AuthRepository
import com.example.tasknight.domain.repository.TaskRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HistoryState(
    val daysLogged: Int = 0,
    val streakCount: Int = 0,
    val tasksDone: Int = 0,
    val currentYearMonth: YearMonth = YearMonth.now(),
    val completedDates: Set<LocalDate> = emptySet(),
    val partialDates: Set<LocalDate> = emptySet(),
    val missedDates: Set<LocalDate> = emptySet(),
    val selectedDate: LocalDate? = null,
    val tasksForSelectedDate: List<Task> = emptyList(),
    val reflectionForSelectedDate: DailyReflection? = null,
    val isLoading: Boolean = false,
    val isGuest: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val user = authRepository.getCurrentUser().first()
            if (user != null) {
                _state.update { it.copy(isGuest = user.isGuest) }
                if (!user.isGuest) {
                    taskRepository.getAllTasks(user.id).collectLatest { allTasks ->
                        processTasks(allTasks)
                    }
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun processTasks(allTasks: List<Task>) {
        val tasksByDate = allTasks.groupBy { it.targetDate }
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val completedDates = mutableSetOf<LocalDate>()
        val partialDates = mutableSetOf<LocalDate>()
        val missedDates = mutableSetOf<LocalDate>()
        var totalTasksDone = 0
        var daysWithLogs = 0

        // Process all dates that have tasks
        tasksByDate.forEach { (dateStr, tasks) ->
            try {
                val date = LocalDate.parse(dateStr)
                val doneCount = tasks.count { it.isCompleted }
                totalTasksDone += doneCount

                if (tasks.isNotEmpty()) {
                    daysWithLogs++
                    when {
                        doneCount == tasks.size && tasks.isNotEmpty() -> completedDates.add(date)
                        doneCount > 0 -> partialDates.add(date)
                        date.isBefore(today) -> missedDates.add(date)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        _state.update { it.copy(
            daysLogged = daysWithLogs,
            tasksDone = totalTasksDone,
            completedDates = completedDates,
            partialDates = partialDates,
            missedDates = missedDates,
            streakCount = calculateStreak(completedDates)
        ) }


        state.value.selectedDate?.let { date ->
            viewModelScope.launch {
                val user = authRepository.getCurrentUser().firstOrNull()
                if (user != null) {
                    loadDetailsForDate(date, user.id, allTasks)
                }
            }
        }
    }

    private fun calculateStreak(completedDates: Set<LocalDate>): Int {
        if (completedDates.isEmpty()) return 0

        val sortedDates = completedDates.sortedDescending()
        var streak = 0
        var currentDate = LocalDate.now()


        if (!completedDates.contains(currentDate)) {
            currentDate = currentDate.minusDays(1)
        }

        while (completedDates.contains(currentDate)) {
            streak++
            currentDate = currentDate.minusDays(1)
        }

        return streak
    }

    fun selectDate(date: LocalDate) {
        _state.update { it.copy(selectedDate = date) }
        viewModelScope.launch {
            val user = authRepository.getCurrentUser().first()
            if (user != null) {
                val allTasks = taskRepository.getAllTasks(user.id).first()
                loadDetailsForDate(date, user.id, allTasks)
            }
        }
    }

    private suspend fun loadDetailsForDate(date: LocalDate, userId: String, allTasks: List<Task>) {
        val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val tasks = allTasks.filter { it.targetDate == dateStr }


        val reflection = taskRepository.getDailyReflection(userId, dateStr).firstOrNull()

        _state.update { it.copy(
            tasksForSelectedDate = tasks,
            reflectionForSelectedDate = reflection
        ) }
    }

    fun changeMonth(yearMonth: YearMonth) {
        _state.update { it.copy(
            currentYearMonth = yearMonth,
            selectedDate = null,
            tasksForSelectedDate = emptyList()
        ) }
    }
}