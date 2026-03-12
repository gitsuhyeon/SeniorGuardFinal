package com.example.seniorguard.data.repository
import android.util.Log
import com.example.seniorguard.network.api.TokenApi
import com.example.seniorguard.network.api.TokenRequest
import kotlinx.serialization.Serializable
import javax.inject.Inject

class TokenRepository @Inject constructor(
    private val tokenApi: TokenApi
) {
    /*
    suspend fun sendTokenToServer(userId: String, token: String) {
        val request = TokenRequest(userId = userId, token = token)
        tokenApi.registerToken(request)
    }

     */

    suspend fun registerToken(userId: String, token: String, deviceType: String) {
        // 서버로 보낼 요청 본문(Body) 생성
        val request = TokenRegisterRequest(userId, token, deviceType)
        try {
            // API 호출
            val response = tokenApi.registerToken(request)

            if (response.isSuccessful) {
                // 'AuthRepository' 대신 현재 클래스 이름인 'TokenRepository'로 로그 태그 수정
                Log.d("TokenRepository", "토큰 등록 성공: $deviceType")
            } else {
                Log.e("TokenRepository", "토큰 등록 실패: ${response.code()} ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("TokenRepository", "토큰 등록 중 네트워크 오류", e)
        }
    }

    @Serializable
    data class TokenRegisterRequest(
        val userId: String,
        val token: String,
        val device_type: String // `deviceType`이 아니라 `device_type`
    )
}



