package com.example.seniorguard.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "fall_events")
data class FallEvent(
    @PrimaryKey val id: String, //서버가 보낸 event_id
    val timestamp: Long = System.currentTimeMillis(), // 서버에서 받은 timestamp


    val fallConfidence: Float,   // 낙상 확신도 (0.0 ~ 1.0)

    val impactLocation: String?,  // 충돌 부위 (예: "머리", "엉덩이")

    //val impactConfidence: Float?,// 충돌 부위 확신도

    val llmAnalysis: String?,     // LLM의 종합 분석 및 대응 지침

    val isRead: Boolean = false, // 보호자가 확인했는지 여부
    val videoUrl: String? = null
)
