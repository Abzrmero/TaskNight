package com.example.tasknight.domain.models
import com.example.tasknight.domain.models.Mood
import kotlinx.serialization.Serializable

@Serializable
data class DailyReflection(
    val id: String = "",
    val userId: String = "",
    val date: String = "", 
    val mood: Mood? = null,
    val rating: Int = 0,
    val dailyNote: String = ""
)
