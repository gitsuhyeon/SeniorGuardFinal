package com.example.seniorguard.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
@Serializable
data class FallDetectionResponse(
    @SerialName("event_id")
    val eventId: String? = null,
    val status: String? = null,
    val detection: Detection? = null,
    @SerialName("llm_analysis")
    val llmAnalysis: String? = null,
    val message: String? = null // 혹시 모를 서버의 에러 메시지를 받기 위해 추가 (권장)
)

@Serializable
data class Detection(
    @SerialName("is_fall")
    val isFall: Boolean = false,

    @SerialName("fall_confidence")
    val fallConfidence: Float? = null,

    @SerialName("impact_location")
    val impactLocation: String? =null,

    @SerialName("impact_confidence")
    val impactConfidence: Float? = null,

    val timestamp: String? = null
)
