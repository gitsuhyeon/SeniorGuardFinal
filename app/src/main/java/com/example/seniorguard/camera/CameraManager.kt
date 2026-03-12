package com.example.seniorguard.camera


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.seniorguard.mediapipe.PoseLandmarkerHelper
import java.io.File
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.ui.semantics.error
//import androidx.privacysandbox.tools.core.generator.build





import java.text.SimpleDateFormat
import java.util.Locale

/**
 * CameraX를 초기화하고 PreviewView에 연결하는 클래스
 * ImageAnalysis.Analyzer 인터페이스를 익명 객체(람다식)구현하여 MediaPipe로 전달 가능
 */
class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val poseLandmarkerHelper: PoseLandmarkerHelper,
    private val frameAnalyzer: ImageAnalysis.Analyzer,
    // 이 객체의 상태는 startCamera/shutdown 함수를 통해서만 관리되어야 합니다.
    private var cameraProvider: ProcessCameraProvider? = null
) {
    //  1. VideoCapture와 관련된 변수들 추가
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private val outputDirectory: File = context.filesDir // 앱 내부 저장소 사용


    private lateinit var cameraExecutor: ExecutorService
    private var imageAnalysis: ImageAnalysis? = null

    /**
     * 카메라를 시작하는 함수
     * - PreviewView에 영상 출력
     * - 프레임 분석기 연결
     */
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            //  낙상영상녹화위해 cameraProvider를 클래스 멤버 변수에 할당
            cameraProvider = cameraProviderFuture.get()

            // 후면 카메라 선택
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val preview = Preview.Builder().build()

            previewView.post {
                preview.setSurfaceProvider(previewView.surfaceProvider)
            }

            // 프레임 분석기 설정
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(getCameraExecutor(), frameAnalyzer)
                }

            //  낙상영상녹화위해 인스턴스 생성
            val recorder = Recorder.Builder().build()
            videoCapture = VideoCapture.withOutput(recorder)


            try {
                // 기존 카메라 세션 제거 후 새로 바인딩
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis,
                    videoCapture // 녹화 기능 바인딩
                )
            } catch (e: Exception) {
                Log.e("CameraManager", "카메라 바인딩 실패: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * 프레임 분석을 위한 Executor 반환
     */
    private fun getCameraExecutor(): ExecutorService {
        if (!::cameraExecutor.isInitialized || cameraExecutor.isShutdown) { //  안전장치 추가
            cameraExecutor = Executors.newSingleThreadExecutor()
        }
        return cameraExecutor
    }

    /**
     *  2. 카메라가 시작되었는지 확인하는 함수 추가
     * cameraProvider 객체가 할당되었는지 여부로 판단합니다.
     */
    fun isCameraStarted(): Boolean {
        return cameraProvider != null
    }

    /**
     *  3. 기존 shutdown 함수 확장
     * 카메라 세션을 안전하게 종료하고, Executor를 종료하며, cameraProvider를 null로 초기화합니다.
     */
    fun shutdown() {
        Log.d("CameraManager", "카메라 세션을 종료합니다.")
        // 카메라 세션 바인딩 해제
        cameraProvider?.unbindAll()
        cameraProvider = null // 상태 초기화

        // Executor 종료
        if (::cameraExecutor.isInitialized && !cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
    }

    fun detectLiveStreamSafely(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        if (!hasCameraPermission()) {
            Log.w("CameraManager", "카메라 권한 없음. 프레임 분석 중단")
            imageProxy.close()
            return
        }
        poseLandmarkerHelper.detectLiveStream(imageProxy, isFrontCamera)
    }

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }


    // 낙상영상전송위해 녹화를 시작하고, 완료되면 콜백을 실행하는 함수
    fun startRecording(onRecordingFinished: (File) -> Unit) {
        val videoCapture = this.videoCapture ?: return

        val videoFile = File(
            outputDirectory,
            "fall-video-${System.currentTimeMillis()}.mp4"
        )

        activeRecording = videoCapture.output
            .prepareRecording(context,
                FileOutputOptions.Builder(videoFile).build())
            .start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Finalize -> {
                        if (!event.hasError()) {
                            Log.i("CameraManager", "녹화 성공: ${videoFile.absolutePath}")
                            onRecordingFinished(videoFile) // 녹화가 끝나면 이 파일을 전달
                        } else {
                            Log.e("CameraManager", "녹화 에러: ${event.error}")
                            activeRecording?.close()
                        }
                    }
                }
            }
    }

    //   녹화 중지 함수
    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    // ImageAnalysis만 unbind하여 포즈 분석을 중단하는 함수
    fun stopImageAnalysis() {
        imageAnalysis?.let{
            cameraProvider?.unbind(it)
            Log.d("CameraManager", "ImageAnalysis를 unbind 했습니다.")
        } ?: run {
            Log.w("CameraManager", "ImageAnalysis가 초기화되지 않아 unbind 할 수 없습니다.")

        }
    }
}
