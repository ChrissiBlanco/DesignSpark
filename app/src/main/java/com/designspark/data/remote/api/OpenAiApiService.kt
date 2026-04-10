package com.designspark.data.remote.api

import com.designspark.data.remote.dto.OpenAiChatCompletionRequestDto
import com.designspark.data.remote.dto.OpenAiChatCompletionResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAiApiService {
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Body request: OpenAiChatCompletionRequestDto
    ): OpenAiChatCompletionResponseDto
}
