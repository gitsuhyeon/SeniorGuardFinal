package com.example.seniorguard.data.repository

import com.example.seniorguard.data.model.FallEvent
import com.example.seniorguard.data.model.SeniorData
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date

// 실제 GuardianRepository 인터페이스를 구현한다고 가정
interface GuardianRepository {
    fun getSelectedSenior(): Flow<SeniorData>

    /**
     * 모든 낙상 이벤트 기록을 최신순으로 가져온다.
     * Room DB의 데이터 변경을 실시간으로 감지하기 위해 Flow를 사용한다.
     */
    fun getFallHistory(): Flow<List<FallEvent>>

    /**
     * 새로운 낙상 이벤트를 데이터 소스(DB)에 저장한다.
     */
    suspend fun saveFallEvent(event: FallEvent)

    fun getFallEventById(id: String): Flow<FallEvent?>

    suspend fun updateFallEventWithVideo(eventId: String, videoUrl: String)

    }