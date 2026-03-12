package com.example.seniorguard.ui.falldetail


import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seniorguard.data.model.FallEvent
import com.example.seniorguard.data.repository.GuardianRepository
import com.example.seniorguard.network.api.TokenApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

//  1. UI 상태를 나타내는 sealed interface 추가
sealed interface ResumeUiState {
    object Idle : ResumeUiState // 초기 상태
    object Loading : ResumeUiState // 로딩 중
    object Success : ResumeUiState // 성공
    object Error : ResumeUiState // 실패
}
@HiltViewModel
class FallDetailViewModel @Inject constructor(
    repository: GuardianRepository,
    savedStateHandle: SavedStateHandle, // 내비게이션 인자를 받기 위해 필요
    private val tokenApi: TokenApi //서버 동결 해제 위해
) : ViewModel() {

    // savedStateHandle에서 "eventId" 키로 값을 가져옵니다.
    private val eventId: String = checkNotNull(savedStateHandle["eventId"])

    // eventId를 사용해 DB에서 해당 이벤트를 가져옵니다.
    val fallEvent: StateFlow<FallEvent> = repository.getFallEventById(eventId)
        .filterNotNull() // DB에서 값을 찾을 때까지 기다립니다 (null이 아닐 때만 방출).
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            // 초기값은 stateIn이 시작되기 전까지 잠시 사용될 값입니다.
            // filterNotNull() 때문에 실제 UI에서는 거의 보이지 않습니다.
            initialValue = FallEvent(
                id = "", timestamp = 0L, fallConfidence = 0f,
                impactLocation = "로딩 중...", llmAnalysis = ""
            )
        )

    //  2. 서버 재개 요청 상태를 관리할 StateFlow 추가
    private val _resumeUiState = MutableStateFlow<ResumeUiState>(ResumeUiState.Idle)
    val resumeUiState: StateFlow<ResumeUiState> = _resumeUiState.asStateFlow()

    //  3. '상황 해제' 버튼이 호출할 함수 추가
    fun resumeFallDetection() {
        // 이미 로딩 중이면 중복 실행 방지
        if (_resumeUiState.value == ResumeUiState.Loading) return

        viewModelScope.launch {
            _resumeUiState.value = ResumeUiState.Loading // 상태를 '로딩 중'으로 변경
            try {
                val response = tokenApi.resumeServer()
                if (response.isSuccessful) {
                    _resumeUiState.value = ResumeUiState.Success // 성공
                    Log.d("FallDetailViewModel", " 서버 동결 해제 요청 성공")
                } else {
                    _resumeUiState.value = ResumeUiState.Error // 실패
                    Log.e("FallDetailViewModel", " 서버 동결 해제 요청 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                _resumeUiState.value = ResumeUiState.Error // 실패 (네트워크 오류 등)
                Log.e("FallDetailViewModel", " 서버 동결 해제 요청 중 예외 발생", e)
            }
        }
    }

    //  4. UI 상태를 다시 초기 상태로 되돌리는 함수 (필요 시 사용)
    fun resetResumeState() {
        _resumeUiState.value = ResumeUiState.Idle
    }
}