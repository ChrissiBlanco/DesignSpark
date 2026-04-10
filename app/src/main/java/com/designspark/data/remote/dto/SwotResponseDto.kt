package com.designspark.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SwotResponseDto(
    @SerializedName("swot") val swot: SwotBlockDto
)

data class SwotBlockDto(
    @SerializedName("strengths") val strengths: List<String>,
    @SerializedName("weaknesses") val weaknesses: List<String>,
    @SerializedName("opportunities") val opportunities: List<String>,
    @SerializedName("threats") val threats: List<String>
)
