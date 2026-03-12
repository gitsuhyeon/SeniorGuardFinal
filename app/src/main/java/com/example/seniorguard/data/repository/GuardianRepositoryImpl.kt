package com.example.seniorguard.data.repository

import com.example.seniorguard.data.dao.FallEventDao
import com.example.seniorguard.data.model.FallEvent
import com.example.seniorguard.data.model.SeniorData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GuardianRepositoryImpl @Inject constructor(
    private val fallEventDao: FallEventDao
) : GuardianRepository {


    // 새로운 낙상 이벤트를 DB에 저장하는 함수 추가
    override suspend fun saveFallEvent(event: FallEvent) {
        fallEventDao.insertFallEvent(event)
    }

    override fun getSelectedSenior(): Flow<SeniorData> {
        // TODO: 이 부분도 나중에 DB나 API에서 가져오도록 구현해야 합니다.
        return flowOf(
            SeniorData(
                name = "김철수",
                age = 80,
                lastCheck = "2025-11-21 19:45"
            )
        )
    }

    override fun getFallHistory(): Flow<List<FallEvent>> {
        //  DAO를 통해 실제 데이터베이스에서 데이터를 가져옵니다.
        return fallEventDao.getAllFallEvents()
    }

    override fun getFallEventById(id: String): Flow<FallEvent?> {
        return fallEventDao.getFallEventById(id)
    }

    override suspend fun updateFallEventWithVideo(eventId: String, videoUrl: String) {
        fallEventDao.updateVideoUrl(eventId, videoUrl)
    }
}
