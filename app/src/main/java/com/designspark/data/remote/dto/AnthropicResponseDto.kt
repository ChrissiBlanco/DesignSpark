package com.designspark.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AnthropicResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: List<ContentDto>,
    @SerializedName("model") val model: String,
    @SerializedName("stop_reason") val stopReason: String?
)

data class ContentDto(
    @SerializedName("type") val type: String,
    @SerializedName("text") val text: String
)
