package com.designspark.data.remote.dto

import com.google.gson.annotations.SerializedName

data class InsightResponseDto(
    @SerializedName("personas") val personas: List<PersonaDto>,
    @SerializedName("methodCards") val methodCards: List<MethodCardDto>,
    @SerializedName("assumptionsToTest") val assumptionsToTest: List<AssumptionDto>,
    @SerializedName("recruitBrief") val recruitBrief: RecruitBriefDto
)

data class PersonaDto(
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: Int,
    @SerializedName("role") val role: String,
    @SerializedName("goal") val goal: String,
    @SerializedName("frustration") val frustration: String
)

data class MethodCardDto(
    @SerializedName("method") val method: String,
    @SerializedName("whyThisFits") val whyThisFits: String,
    @SerializedName("estimatedTime") val estimatedTime: String
)

data class AssumptionDto(
    @SerializedName("assumption") val assumption: String,
    @SerializedName("risk") val risk: String,
    @SerializedName("rationale") val rationale: String
)

data class RecruitBriefDto(
    @SerializedName("whoToFind") val whoToFind: String,
    @SerializedName("screenFor") val screenFor: String,
    @SerializedName("exclude") val exclude: String
)
