package com.example.seniorguard.data.repository

import com.example.seniorguard.data.model.FallDetectionResponse
import com.example.seniorguard.data.model.SkeletonData
import com.example.seniorguard.network.api.SkeletonApi //  API 인터페이스 Import
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import retrofit2.Response


class FallDetectionRepository @Inject constructor(
    private val skeletonApi: SkeletonApi
) {
    /**
     * 분석 로직 (임시)
     */
    fun analyzeSkeleton(data: SkeletonData): Boolean {
        return false
    }

    /**
     * 90프레임의 뼈대 데이터 묶음을 서버로 전송합니다.
     * @param poseWindow 서버로 보낼 List<SkeletonData>
     */
    /* 백스텝
    suspend fun sendPoseWindow(poseWindow: List<SkeletonData>) {
        //  주입받은 skeletonApi 객체를 통해 직접 API 함수 호출
        // (네트워크 오류 처리는 ViewModel/UseCase 등 상위 레이어에서 하는 것이 일반적입니다)
        skeletonApi.sendPoseData(poseWindow)
    }
     */
    suspend fun sendPoseWindow(poseWindow: List<SkeletonData>): Response<FallDetectionResponse> { // ✅ 2. 반환 타입 추가
        // Repository가 내부적으로 Api를 호출합니다.
        // ViewModel은 skeletonApi의 존재를 알 필요가 없습니다.
        return skeletonApi.sendPoseData(poseWindow) //  API 응답을 그대로 반환
    }

    //  영상 파일 하나를 업로드하는 함수
    suspend fun uploadFallVideo(file: File,eventId: String): Response<FallDetectionResponse> {
        val requestFile = file.asRequestBody("video/mp4".toMediaTypeOrNull())
        val videoPart = MultipartBody.Part.createFormData(
            "video", //  서버와 약속된 파라미터 이름
            file.name,
            requestFile
        )

        // 서버에서 낙상 url만 받는것보다 eventid도 받아야 관리가 수월하기에 추가함.
        // 2. eventId를 텍스트 RequestBody로 변환
        val eventIdRequestBody = eventId.toRequestBody("text/plain".toMediaTypeOrNull())

        // 3. "event_id"라는 키와 함께 Map에 담음
        val dataMap = mapOf(
            "event_id" to eventIdRequestBody
        )
        return skeletonApi.uploadFallVideo(videoPart,dataMap)
    }
}
