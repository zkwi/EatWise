package com.example.eatwise.domain.model

data class AppSettings(
    val baseUrl: String = DEFAULT_BASE_URL,
    val modelName: String = "",
    val apiKey: String = "",
    val userGoal: String = DEFAULT_USER_GOAL,
) {
    companion object {
        const val DEFAULT_BASE_URL = "https://openrouter.ai/api/v1"
        const val DEFAULT_USER_GOAL = "我想保持饮食均衡，尽量吃得健康一些。"
    }
}
