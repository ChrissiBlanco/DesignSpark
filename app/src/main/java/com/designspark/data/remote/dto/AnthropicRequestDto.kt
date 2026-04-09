package com.designspark.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AnthropicRequestDto(
    @SerializedName("model") val model: String,
    @SerializedName("max_tokens") val maxTokens: Int,
    @SerializedName("system") val system: String,
    @SerializedName("messages") val messages: List<MessageDto>
)

data class MessageDto(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)
