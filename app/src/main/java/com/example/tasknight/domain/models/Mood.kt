package com.example.tasknight.domain.models
import kotlinx.serialization.Serializable

@Serializable
enum class Mood(val emoji: String) {
    HAPPY("😊"),
    NEUTRAL("😐"),
    SAD("😔"),
    GREAT("🔥"),
    AWFUL("😫")
}