package com.designspark.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenAiChatCompletionRequestDto(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<OpenAiChatMessageDto>,
    @SerializedName("max_tokens") val maxTokens: Int
)

data class OpenAiChatMessageDto(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)
