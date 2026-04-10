package com.designspark.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CompetitorScanResponseDto(
    @SerializedName("competitors") val competitors: List<CompetitorScanCompetitorDto>,
    @SerializedName("marketGap") val marketGap: String,
    @SerializedName("painPoints") val painPoints: List<CompetitorScanPainPointDto>
)

data class CompetitorScanCompetitorDto(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("weakness") val weakness: String
)

data class CompetitorScanPainPointDto(
    @SerializedName("painPoint") val painPoint: String,
    @SerializedName("rationale") val rationale: String
)
