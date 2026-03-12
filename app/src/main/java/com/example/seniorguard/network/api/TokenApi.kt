package com.example.seniorguard.network.api

import com.example.seniorguard.data.repository.TokenRepository
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TokenApi {
    @POST("/register_token") // 서버에 만들 API 엔드포인트
    suspend fun registerToken(@Body tokenRequest: TokenRepository.TokenRegisterRequest): Response<Unit>

    @GET("resume")
    suspend fun resumeServer(): Response<Unit> // 반환값은 간단히 Unit으로 처리
}
@Serializable
data class TokenRequest(
    val userId: String, // 어떤 사용자의 토큰인지 식별하기 위함 (지금은 Guardianviewmodel에서 임시값 사용)
    val token: String
)