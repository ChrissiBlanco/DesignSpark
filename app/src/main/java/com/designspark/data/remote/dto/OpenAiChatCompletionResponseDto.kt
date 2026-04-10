package com.designspark.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenAiChatCompletionResponseDto(
    @SerializedName("choices") val choices: List<OpenAiChatChoiceDto>
)

data class OpenAiChatChoiceDto(
    @SerializedName("message") val message: OpenAiChatAssistantMessageDto
)

data class OpenAiChatAssistantMessageDto(
    @SerializedName("role") val role: String?,
    @SerializedName("content") val content: String?
)
