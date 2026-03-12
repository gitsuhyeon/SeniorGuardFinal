package com.example.seniorguard.data.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.seniorguard.data.model.FallEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface FallEventDao {

    //  Flow를 사용해 데이터 변경을 실시간으로 감지합니다. 최신순으로 정렬합니다.
    @Query("SELECT * FROM fall_events ORDER BY timestamp DESC")
    fun getAllFallEvents(): Flow<List<FallEvent>>

    //  새로운 낙상 이벤트를 DB에 삽입합니다. id가 겹치면 덮어씁니다.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFallEvent(event: FallEvent)

    @Query("SELECT * FROM fall_events WHERE id = :id")
    fun getFallEventById(id: String): Flow<FallEvent?>

    // 낙상영상url
    @Query("UPDATE fall_events SET videoUrl = :videoUrl WHERE id = :eventId")
    suspend fun updateVideoUrl(eventId: String, videoUrl: String)


    //  ID로 특정 이벤트를 삭제하는 함수 (선택 사항)
    @Query("DELETE FROM fall_events WHERE id = :id")
    suspend fun deleteFallEventById(id: String)

    //  모든 이벤트를 삭제하는 함수 (선택 사항)
    @Query("DELETE FROM fall_events")
    suspend fun clearAll()
}
