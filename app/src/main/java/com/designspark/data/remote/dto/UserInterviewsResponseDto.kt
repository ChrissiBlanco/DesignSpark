package com.designspark.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserInterviewsResponseDto(
    @SerializedName("interviews") val interviews: List<UserInterviewItemDto>
)

data class UserInterviewItemDto(
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String,
    @SerializedName("sentiment") val sentiment: String,
    @SerializedName("reaction") val reaction: String,
    @SerializedName("quote") val quote: String
)
