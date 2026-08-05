package com.example.tasknight.domain.models
import com.example.tasknight.presentation.set.Priority
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val why: String = "",
    val priority: Priority = Priority.MEDIUM,
    @get:PropertyName("isCompleted")
    val isCompleted: Boolean = false,
    val reflection: String = "",
    val completedAt: String? = null,
    val targetDate: String = "" 
)
