package com.example.seniorguard.ui.guardian

import android.text.format.DateUtils
import android.util.Log
//import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
//import androidx.paging.map
//import androidx.preference.isNotEmpty
import com.example.seniorguard.data.model.FallEvent
import com.example.seniorguard.data.model.SeniorData
import com.example.seniorguard.data.repository.GuardianRepository
import com.example.seniorguard.data.repository.TokenRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import javax.inject.Inject
import java.util.Date
import java.util.Locale
import kotlin.text.format


//  2. 화면에 필요한 모든 데이터를 담을 UI 상태(State) 데이터 클래스 정의
data class GuardianUiState(
    val connectionStatusText: String = "연결 상태 확인 중...",
    val lastCheckedText: String = "데이터 수신 대기 중...",
    val activityLevel: String = "분석 중", // 활동 수준 (양호, 주의, 위험 등)
    val todayFallCount: Int = 0,
    val recentAlerts: List<FallEvent> = emptyList()
)
@HiltViewModel
class GuardianViewModel @Inject constructor(
    private val repository: GuardianRepository
    ) : ViewModel() {


    //  3. 모든 UI 상태를 하나의 StateFlow로 통합하여 관리
    val uiState: StateFlow<GuardianUiState> = repository.getFallHistory()
        .map { events -> // `events`는 DB에 저장된 모든 낙상 기록 리스트입니다.
            // 1. 오늘의 낙상 기록만 필터링합니다.
            val todayEvents = events.filter { DateUtils.isToday(it.timestamp) }

            // 2. 오늘의 낙상 횟수를 계산합니다.
            val todayFallCount = todayEvents.size

            // 3. 오늘의 활동 수준을 결정합니다. (규칙은 여기서 정의)
            val currentActivityLevel = when {
                todayFallCount > 0 -> "위험" // 오늘 낙상이 1건이라도 있으면 '위험'
                events.isNotEmpty() -> "양호" // 낙상이 없으면 '양호'
                else -> "기록 없음" // 데이터가 아예 없으면 '기록 없음'
            }

            // 4. 마지막 확인 시간을 계산합니다. (가장 최근 이벤트 기준)
            val lastEventTime = events.maxOfOrNull { it.timestamp }

            GuardianUiState(
                // TODO: 실제 서버 연결 상태를 확인하는 로직 추가 필요
                connectionStatusText = "모니터링 기기 연결됨",
                lastCheckedText = if (lastEventTime != null) {
                    "마지막 확인: ${formatTimestamp(lastEventTime)}"
                } else {
                    "최근 활동 기록 없음"
                },
                activityLevel = currentActivityLevel,
                todayFallCount = todayFallCount,
                // 최근 알림은 최신순으로 정렬하여 UI에 전달
                recentAlerts = events.sortedByDescending { it.timestamp }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            // 앱 시작 시 보여줄 초기 상태
            initialValue = GuardianUiState()
        )


    //  repository.getSelectedSenior()를 호출하여 어르신 정보를 가져옵니다.
    //    DB 구축 전까지는 Impl에 하드코딩된 더미 데이터가 표시됩니다.
    val seniorData: StateFlow<SeniorData> = repository.getSelectedSenior()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SeniorData("로딩 중...", 0, "") // 초기값
        )

    // repository.getFallHistory()를 호출하여 실제 Room DB의 낙상 기록을 가져옵니다.
    //    Flow이므로 DB에 데이터가 추가/변경되면 UI가 자동으로 업데이트됩니다.
    val fallHistory: StateFlow<List<FallEvent>> = repository.getFallHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // 초기값은 빈 리스트
        )

    // ---  상세 화면을 위한 로직 추가 ---

    // 1. 선택된 이벤트를 저장할 private MutableStateFlow 생성
    private val _selectedEvent = MutableStateFlow<FallEvent?>(null)
    // 2. 외부(UI)에 노출될 읽기 전용 StateFlow
    val selectedEvent: StateFlow<FallEvent?> = _selectedEvent.asStateFlow()

    /**
     * 3. 사용자가 리스트에서 이벤트를 클릭했을 때 호출될 함수.
     * eventId를 받아 DB에서 해당 이벤트를 조회하고 _selectedEvent를 업데이트한다.
     */
    fun selectEvent(eventId: String) {
        viewModelScope.launch {
            repository.getFallEventById(eventId).collect { event ->
                _selectedEvent.value = event
            }
        }
    }
    /**
     * 4. 상세 화면에서 뒤로가기 할 때, 선택된 이벤트를 초기화하는 함수.
     */
    fun clearSelectedEvent() {
        _selectedEvent.value = null
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}