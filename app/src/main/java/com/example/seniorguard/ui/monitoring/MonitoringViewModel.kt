package com.example.seniorguard.ui.monitoring

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seniorguard.camera.CameraManager
import com.example.seniorguard.data.model.SkeletonData
import com.example.seniorguard.data.repository.FallDetectionRepository
import com.example.seniorguard.mediapipe.PoseLandmarkerHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume

@HiltViewModel
class MonitoringViewModel @Inject constructor(
    private val fallDetectionRepository: FallDetectionRepository
) : ViewModel(), PoseLandmarkerHelper.LandmarkerListener {

    private val _skeletonState = MutableStateFlow(SkeletonData.empty())
    val skeletonState: StateFlow<SkeletonData> = _skeletonState.asStateFlow()

    private val _isFallDetected = MutableStateFlow(false)
    val isFallDetected: StateFlow<Boolean> = _isFallDetected.asStateFlow()

    private lateinit var cameraManager: CameraManager
    private lateinit var poseHelper: PoseLandmarkerHelper

    // 녹화/업로드 작업을 관리하기 위한 Job 객체
    private var recordingJob: Job? = null

    // --- 초기화 및 카메라 제어 ---

    fun initializeComponents(context: Context) {
        if (::poseHelper.isInitialized) {
            Log.d("MonitoringViewModel", "컴포넌트가 이미 초기화되었습니다.")
            return
        }
        Log.d("MonitoringViewModel", "새로운 Helper 인스턴스를 생성합니다.")
        poseHelper = PoseLandmarkerHelper(
            runningMode = com.google.mediapipe.tasks.vision.core.RunningMode.LIVE_STREAM,
            context = context,
            poseLandmarkerHelperListener = this
        )
    }

    fun startCamera(context: Context, lifecycleOwner: LifecycleOwner, preview: PreviewView) {
        initializeComponents(context)

        if (::cameraManager.isInitialized && cameraManager.isCameraStarted()) {
            Log.d("MonitoringViewModel", "카메라가 이미 실행 중입니다. startCamera 호출을 무시합니다.")
            return
        }

        val analyzer = ImageAnalysis.Analyzer { imageProxy ->
            if (::poseHelper.isInitialized) {
                poseHelper.detectLiveStream(imageProxy, isFrontCamera = false)
            } else {
                imageProxy.close()
            }
        }

        Log.d("MonitoringViewModel", "새로운 CameraManager 인스턴스를 생성하고 카메라를 시작합니다.")
        cameraManager = CameraManager(
            context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = preview,
            poseLandmarkerHelper = poseHelper,
            frameAnalyzer = analyzer
        )
        cameraManager.startCamera()
    }

    fun shutdownCamera() {
        if (::cameraManager.isInitialized) {
            cameraManager.shutdown()
        }
    }

    fun releaseMediaPipe() {
        if (::poseHelper.isInitialized) {
            poseHelper.clearPoseLandmarker()
        }
    }

    // --- PoseLandmarkerHelper.LandmarkerListener 구현 ---

    override fun onSkeleton(skeleton: SkeletonData) {
        _skeletonState.value = skeleton
    }

    override fun onPoseWindowed(poseWindow: List<SkeletonData>) {
        // 이미 낙상이 감지되어 처리 중일 때는 새로운 데이터를 보내지 않음
        if (_isFallDetected.value) return

        Log.d(
            "MonitoringViewModel",
            "Pose window ready. Sending ${poseWindow.size} frames to server."
        )
        sendSkeletonData(poseWindow)
    }

    override fun onError(error: String, errorCode: Int) {
        Log.e("MonitoringViewModel", "Pose Landmarker error: $error (code: $errorCode)")
    }



    // --- 낙상 감지 및 처리 로직 ---

    private fun sendSkeletonData(skeletonWindow: List<SkeletonData>) {
        // 이미 다른 작업이 실행 중이라면 중복 실행 방지
        if (recordingJob?.isActive == true) return

        viewModelScope.launch {
            try {
                Log.d("API_CALL", "Attempting to send data to server...")
                val response = fallDetectionRepository.sendPoseWindow(skeletonWindow)
                if (response.isSuccessful) {
                    //  실제 데이터는 body() 안에 있습니다.
                    val responseBody = response.body()

                    //  body()가 null이 아닐 때만 처리
                    responseBody?.detection?.let { detection ->
                        //Log.d("API_CALL", "Server response received: isFall=${detection.isFall}")
                        if (detection.isFall) {
                            Log.d("API_RESPONSE", "Received: $responseBody")

                            //  responseBody에서 eventId를 가져옵니다.
                            val eventId = responseBody.eventId
                            if (eventId == null) {
                                Log.e("FALL_DETECT", "서버 응답 본문(body)에 eventId가 없습니다.")
                                return@let
                            }

                            Log.w("FALL_DETECT", "🚨 서버가 낙상을 감지! (eventId: $eventId). 영상 녹화 및 업로드 절차를 시작합니다.")
                            recordingJob?.cancel()
                            recordingJob = recordUploadAndFinalize(eventId)
                        }
                    }
                } else {
                    // 2-B. API 호출이 실패했을 경우 (4xx, 5xx 에러)
                    Log.e("API_CALL", "❌ 서버 응답 실패: Code=${response.code()}, Message=${response.message()}")
                }

            } catch (e: Exception) {
                Log.e("MonitoringViewModel", "데이터 전송 중 오류 발생", e)
            }
        }
    }

    /**
     * 녹화, 업로드, UI 상태 변경의 전체 과정을 순차적으로 관리하는 함수
     */
    private fun recordUploadAndFinalize(eventId: String): Job {
        return viewModelScope.launch {
            // 1. 분석을 중단하여 CPU 리소스를 확보합니다.
            cameraManager.stopImageAnalysis()
            poseHelper.clearPoseLandmarker()
            //skeletonProcessor.reset()
            Log.d("REC_FLOW", "분석 중단. 녹화 및 업로드 절차 시작.")

            // 2. 녹화를 시작하고, 파일이 생성될 때까지 '일시정지(suspend)' 상태로 대기합니다.
            val videoFile = startAndAwaitRecording()

            if (videoFile != null) {
                // 3. 업로드를 시작하고, 완료될 때까지 '일시정지' 상태로 대기합니다.
                uploadVideoAndWait(videoFile, eventId)
                videoFile.delete() // 업로드 후 임시 파일 삭제
            } else {
                Log.e("REC_FLOW", "녹화 실패 또는 파일이 생성되지 않았습니다.")
            }

            // 4. 모든 작업이 끝난 후 UI 상태를 변경하고 카메라를 종료합니다.
            _isFallDetected.value = true
            shutdownCamera()
            Log.d("REC_FLOW", "모든 절차 완료. 대화상자 표시 및 카메라 종료.")
        }
    }

    /**
     * 15초간 녹화를 진행하고, 완료되면 결과 파일을 반환하는 suspend 함수
     */
    private suspend fun startAndAwaitRecording(): File? = suspendCancellableCoroutine { continuation ->
        if (!::cameraManager.isInitialized) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        Log.d("REC_FLOW", "CameraManager.startRecording 호출.")
        cameraManager.startRecording { videoFile ->
            // 녹화가 성공적으로 끝나면, '일시정지'된 코루틴을 파일 결과와 함께 '재개(resume)' 시킴
            if (continuation.isActive) {
                continuation.resume(videoFile)
            }
        }

        // 15초 타이머
        val timerJob = viewModelScope.launch {
            delay(15_000L)
            Log.d("REC_FLOW", "15초 경과. 녹화 중지 요청.")
            cameraManager.stopRecording()
        }

        // 코루틴이 외부에서 취소될 경우(ViewModel 소멸 등) 정리 작업 수행
        continuation.invokeOnCancellation {
            timerJob.cancel()
            cameraManager.stopRecording()
        }
    }

    /**
     * 영상을 업로드하고, 작업이 끝날 때까지 대기하는 suspend 함수
     */
    private suspend fun uploadVideoAndWait(videoFile: File,eventId: String) {
        try {
            Log.i("UPLOAD", "${videoFile.name} 업로드 시도...(eventId: $eventId)")
            val response = fallDetectionRepository.uploadFallVideo(videoFile, eventId)
            if (response.isSuccessful) {
                Log.i("UPLOAD", "✅ 영상 업로드 성공: ${response.body()?.message}")
            } else {
                Log.e("UPLOAD", "❌ 영상 업로드 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("UPLOAD", "❌ 영상 업로드 중 예외 발생", e)
        }
    }

    // --- 기타 유틸리티 함수 ---

    fun resetFallDetectionState() {
        _isFallDetected.value = false
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel이 소멸될 때 진행 중인 모든 작업을 취소하고 리소스를 해제
        recordingJob?.cancel()
        shutdownCamera()
        releaseMediaPipe()
        Log.d("MonitoringViewModel", "ViewModel이 소멸되어 모든 리소스를 해제합니다.")
    }
}
