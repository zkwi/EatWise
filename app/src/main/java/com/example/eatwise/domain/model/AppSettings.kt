package com.example.eatwise.domain.model

data class AppSettings(
    val baseUrl: String = "https://openrouter.ai/api/v1",
    val modelName: String = "",
    val apiKey: String = "",
    val userGoal: String = "我想保持饮食均衡，尽量吃得健康一些。",
)
