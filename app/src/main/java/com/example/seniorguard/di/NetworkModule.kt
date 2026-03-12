package com.example.seniorguard.di

import com.example.seniorguard.network.api.SkeletonApi
import com.example.seniorguard.network.api.TokenApi
import com.google.android.datatransport.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = BuildConfig.SERVER_URL //  서버 주소

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    // 1. OkHttpClient를 생성하는 Provider 함수를 새로 추가합니다.
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃 60초
            .readTimeout(60, TimeUnit.SECONDS)    // 읽기 타임아웃 60초
            .writeTimeout(60, TimeUnit.SECONDS)
            // 모든 요청에 'Connection: close' 헤더를 자동으로 추가하는 인터셉터
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Connection", "close")
                    .build()
                chain.proceed(request)
            }
            .build()
    }


    //  2. Retrofit Provider가 OkHttpClient를 파라미터로 받도록 수정합니다.
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient) // 생성된 OkHttp 클라이언트를 Retrofit에 연결
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideSkeletonApi(retrofit: Retrofit): SkeletonApi =
        retrofit.create(SkeletonApi::class.java)

    @Provides
    @Singleton
    fun provideTokenApi(retrofit: Retrofit): TokenApi {
        return retrofit.create(TokenApi::class.java)
    }
}
