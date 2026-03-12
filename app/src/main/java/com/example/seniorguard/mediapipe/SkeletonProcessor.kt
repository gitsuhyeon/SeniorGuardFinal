package com.example.seniorguard.mediapipe

import android.util.Log
import com.example.seniorguard.data.model.SkeletonData
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class SkeletonProcessor(
    private val windowSize: Int = 90,
    private val stepSize: Int = 30,
    private val onWindowReady: (List<SkeletonData>) -> Unit
) {
    private val frameBuffer = mutableListOf<SkeletonData>()
    private var jointFilters: List<Triple<OneEuroFilter, OneEuroFilter, OneEuroFilter>>? = null

    // 필터 초기화 함수 (OneEuroFilter 생성자에 맞게 Float 타입 사용)
    private fun initializeFilters() {
        val minCutoff = 1.5f  // 최소 차단 주파수
        val beta = 0.01f       // 속도에 따른 반응성 계수
        val dCutoff = 1.0f    // 속도 차단 주파수

        jointFilters = List(REQUIRED_JOINT_INDICES.size) {
            Triple(
                OneEuroFilter(minCutoff, beta, dCutoff), // X 필터
                OneEuroFilter(minCutoff, beta, dCutoff), // Y 필터
                OneEuroFilter(minCutoff, beta, dCutoff)  // Z 필터
            )
        }
        Log.d(TAG, "One-Euro 필터가 초기화되었습니다.")
    }

    // PoseLandmarkerHelper로부터 timestamp를 받지 않는 기존 함수 시그니처로 복원
    fun processAndAddToWindow(result: PoseLandmarkerResult) {
        // 이 함수 내부에서 직접 타임스탬프를 생성합니다.
        val currentTimestamp = System.nanoTime()
        val skeletonData = applyFilterAndProcessFrame(result, currentTimestamp)
        frameBuffer.add(skeletonData)

        if (frameBuffer.size >= windowSize) {
            if ((frameBuffer.size - windowSize) % stepSize == 0) {
                Log.d(TAG, "윈도우 생성! 버퍼 크기: ${frameBuffer.size}")
                val windowToSend = frameBuffer.takeLast(windowSize)
                onWindowReady(windowToSend)
            }
        }
    }

    // 필터를 적용하고 SkeletonData를 생성하는 함수 (내부적으로만 사용)
    private fun applyFilterAndProcessFrame(result: PoseLandmarkerResult, timestamp: Long): SkeletonData {
        val allLandmarks = result.landmarks().firstOrNull()

        if (allLandmarks?.isNotEmpty() == true) {
            // 필터가 생성되지 않았다면 초기화
            if (jointFilters == null) {
                initializeFilters()
            }

            val filteredJointList = REQUIRED_JOINT_INDICES.mapIndexed { i, landmarkIndex ->
                val landmark = allLandmarks[landmarkIndex]
                val filters = jointFilters!![i]

                // OneEuroFilter의 filter(t: Long, x: Float) 순서에 정확히 맞춰서 인자 전달
                val filteredX = filters.first.filter(timestamp, landmark.x())
                val filteredY = filters.second.filter(timestamp, landmark.y())
                val filteredZ = filters.third.filter(timestamp, landmark.z())

                SkeletonData.Joint(
                    x = filteredX,
                    y = filteredY,
                    z = filteredZ,
                    visibility = landmark.visibility().orElse(0f)
                )
            }
            return SkeletonData(joints = filteredJointList)
        } else {
            // 사람이 감지되지 않으면 필터 상태를 리셋
            jointFilters = null
            // 0으로 채워진 데이터를 반환
            val zeroJoints = List(REQUIRED_JOINT_INDICES.size) {
                SkeletonData.Joint(x = 0f, y = 0f, z = 0f, visibility = 0f)
            }
            return SkeletonData(joints = zeroJoints)
        }
    }

    fun reset() {
        frameBuffer.clear()
        jointFilters = null // 필터 상태도 함께 리셋
        Log.d(TAG, "SkeletonProcessor와 필터가 리셋되었습니다.")
    }

    // UI 시각화 용도로만 사용되는 정적(static) 함수 추가
    companion object {
        private const val TAG = "SkeletonProcessor"
        private val REQUIRED_JOINT_INDICES = listOf(
            0,  // NOSE
            11, // LEFT_SHOULDER
            12, // RIGHT_SHOULDER
            13, // LEFT_ELBOW
            14, // RIGHT_ELBOW
            23, // LEFT_HIP
            24, // RIGHT_HIP
            25, // LEFT_KNEE
            26, // RIGHT_KNEE
            27, // LEFT_ANKLE
            28, // RIGHT_ANKLE
            30, // RIGHT_HEEL
            29, // LEFT_HEEL (Python 코드 순서에 맞춤)
            31, // LEFT_FOOT_INDEX
            32  // RIGHT_FOOT_INDEX
        )

        /**
         * 필터를 적용하지 않은 원본 데이터를 UI 시각화용으로 간단히 변환하는 함수.
         * 이 함수는 서버 전송 데이터와는 무관합니다.
         */
        fun processSingleFrame(result: PoseLandmarkerResult): SkeletonData {
            val allLandmarks = result.landmarks().firstOrNull()

            if (allLandmarks.isNullOrEmpty()) {
                val zeroJoints = List(REQUIRED_JOINT_INDICES.size) {
                    SkeletonData.Joint(x = 0f, y = 0f, z = 0f, visibility = 0f)
                }
                return SkeletonData(joints = zeroJoints)
            }

            val jointList = REQUIRED_JOINT_INDICES.map { landmarkIndex ->
                val landmark = allLandmarks[landmarkIndex]
                SkeletonData.Joint(
                    x = landmark.x(),
                    y = landmark.y(),
                    z = landmark.z(),
                    visibility = landmark.visibility().orElse(0f)
                )
            }
            return SkeletonData(joints = jointList)
        }
    }
}
