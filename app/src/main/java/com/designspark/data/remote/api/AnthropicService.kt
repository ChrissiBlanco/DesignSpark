package com.designspark.data.remote.api

import com.designspark.data.remote.dto.AnthropicRequestDto
import com.designspark.data.remote.dto.AnthropicResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AnthropicService {
    @POST("v1/messages")
    suspend fun generateInsights(@Body request: AnthropicRequestDto): AnthropicResponseDto
}
