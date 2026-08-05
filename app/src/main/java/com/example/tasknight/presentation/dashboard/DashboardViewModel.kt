package com.example.tasknight.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasknight.domain.models.Task
import com.example.tasknight.domain.repository.AuthRepository
import com.example.tasknight.domain.repository.TaskRepository
import com.example.tasknight.domain.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class DashboardState(
    val userName: String = "User",
    val isGuest: Boolean = false,
    val streakCount: Int = 0,
    val todayProgress: Float = 0f,
    val todayTasksDone: Int = 0,
    val todayTasksTotal: Int = 0,
    val tomorrowTasksTotal: Int = 0,
    val weeklyCompletionRate: Float = 0f,
    val lifetimeCompletedTasks: Int = 0,
    val hasAnyTasks: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collectLatest { user ->
                if (user == null) {
                    _state.update { DashboardState() }
                    return@collectLatest
                }

                _state.update {
                    it.copy(
                        userName = user.displayName?.split(" ")?.firstOrNull() ?: "User",
                        isGuest = user.isGuest
                    )
                }

                val userId = user.id
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)


                launch {
                    val todayStr = dateFormat.format(Calendar.getInstance().time)
                    taskRepository.getTasksForDate(userId, todayStr).collectLatest { tasks ->
                        val total = tasks.size
                        val done = tasks.count { it.isCompleted }
                        val progress = if (total > 0) done.toFloat() / total else 0f

                        _state.update {
                            it.copy(
                                todayTasksTotal = total,
                                todayTasksDone = done,
                                todayProgress = progress
                            )
                        }
                    }
                }


                launch {
                    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time
                    val tomorrowStr = dateFormat.format(tomorrow)
                    taskRepository.getTasksForDate(userId, tomorrowStr).collectLatest { tasks ->
                        _state.update {
                            it.copy(tomorrowTasksTotal = tasks.size)
                        }
                    }
                }


                launch {
                    taskRepository.getAllTasks(userId).collectLatest { allTasks ->
                        val lifetimeDone = allTasks.count { it.isCompleted }

                        val last7Days = (0..6).map { i ->
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.DAY_OF_YEAR, -i)
                            dateFormat.format(cal.time)
                        }

                        val weeklyTasks = allTasks.filter { it.targetDate in last7Days }
                        val weeklyTotal = weeklyTasks.size
                        val weeklyDone = weeklyTasks.count { it.isCompleted }
                        val weeklyRate = if (weeklyTotal > 0) weeklyDone.toFloat() / weeklyTotal else 0f

                        val streak = calculateStreak(allTasks, dateFormat)

                        _state.update {
                            it.copy(
                                lifetimeCompletedTasks = lifetimeDone,
                                weeklyCompletionRate = weeklyRate,
                                streakCount = streak,
                                hasAnyTasks = allTasks.isNotEmpty()
                            )
                        }
                    }
                }
            }
        }
    }

    private fun calculateStreak(allTasks: List<Task>, dateFormat: SimpleDateFormat): Int {
        val tasksByDate = allTasks.groupBy { it.targetDate }
        val checkCal = Calendar.getInstance()
        var streak = 0
        
        var dateStr = dateFormat.format(checkCal.time)
        var dayTasks = tasksByDate[dateStr] ?: emptyList()

        if (dayTasks.isNotEmpty()) {
            if (dayTasks.all { it.isCompleted }) {
                streak++
            } else {

            }
        }

        // Check previous days
        checkCal.add(Calendar.DAY_OF_YEAR, -1)
        while (true) {
            dateStr = dateFormat.format(checkCal.time)
            dayTasks = tasksByDate[dateStr] ?: emptyList()

            if (dayTasks.isNotEmpty()) {
                if (dayTasks.all { it.isCompleted }) {
                    streak++
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break // Streak broken by incomplete tasks
                }
            } else {
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
                
                if (streak > 3650) break
                
                if (streak > 0 && tasksByDate.keys.none {
                    val d = LocalDate.parse(it)
                    val checkD = LocalDate.parse(dateStr)
                    d.isBefore(checkD) 
                }) break
            }
            
            if (checkCal.get(Calendar.YEAR) < 2024) break
        }

        return streak
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authRepository.signOut()
            _state.update { it.copy(isLoading = false) }
            onLogoutComplete()
        }
    }

    fun fullLogout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authRepository.fullLogout()
            _state.update { it.copy(isLoading = false) }
            onLogoutComplete()
        }
    }
}
