package com.example.seniorguard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.seniorguard.data.model.FallEvent
import com.example.seniorguard.data.repository.GuardianRepository
import com.example.seniorguard.ui.navigation.Screen
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URLEncoder
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCM_SERVICE"

    @Inject
    lateinit var repository: GuardianRepository

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // data 페이로드가 비어있으면 아무것도 하지 않음
        remoteMessage.data.takeIf { it.isNotEmpty() }?.let { data ->
            val eventType = data["event_type"]
            Log.d(TAG, "Received event type: $eventType")

            //  eventType에 따라 분기 처리
            when (eventType) {
                // 첫 번째 알림: 분석 결과 수신
                "FALL_DETECTED" -> handleFallAnalysis(data)
                // 두 번째 알림: 영상 URL 수신
                "FALL_VIDEO_READY" -> handleVideoReady(data)
            }
        }
    }

    /**
     *   첫 번째 알림(FALL_ANALYSIS_READY)을 처리하는 함수
     * - FallEvent 객체를 파싱하여 Room DB에 저장합니다.
     * - 사용자에게 분석 결과 알림을 표시합니다.
     */
    private fun handleFallAnalysis(data: Map<String, String>) {
        val eventId = data["event_id"]
        val title = data["title"] ?: "🚨 긴급: 낙상 감지!"
        val body = data["body"] ?: "낙상이 감지되었습니다."

        if (eventId.isNullOrBlank()) {
            Log.e(TAG, "FALL_ANALYSIS_READY에 eventId가 없어 처리를 중단합니다.")
            return
        }

        // 백그라운드에서 DB 저장
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // FallEvent 객체로 파싱
                val event = parseFallEventFromData(data)
                if (event != null) {
                    // Repository를 통해 DB에 저장
                    repository.saveFallEvent(event)
                    Log.d(TAG, "Fall event saved to DB successfully: ${event.id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse or save fall event", e)
            }
        }

        // 사용자에게 알림 표시 (FallDetail 화면으로 가는 딥링크 포함)
        showFallDetailNotification(title, body, eventId)
    }

    /**
     *  두 번째 알림(FALL_VIDEO_READY)을 처리하는 함수
     * - videoUrl을 Room DB에 업데이트합니다.
     * - 사용자에게 영상 확인 알림을 표시합니다.
     */
    private fun handleVideoReady(data: Map<String, String>) {
        val eventId = data["event_id"]
        val videoUrl = data["video_url"]
        val title = data["title"] ?: "🎬 영상 준비 완료"
        val body = data["body"] ?: "감지된 낙상 영상을 지금 확인할 수 있습니다."

        if (eventId.isNullOrBlank() || videoUrl.isNullOrBlank()) {
            Log.e(TAG, "FALL_VIDEO_READY에 video_url이 없어 처리를 중단합니다.")
            return
        }

        // 백그라운드에서 DB 업데이트
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.updateFallEventWithVideo(eventId, videoUrl)
                Log.d(TAG, "DB 업데이트 성공: Event $eventId 에 videoUrl 추가 완료.")
            } catch (e: Exception) {
                Log.e(TAG, "DB 업데이트 실패", e)
            }
        }

        // 사용자에게 알림 표시 (Player 화면으로 가는 Intent 포함)
        showVideoReadyNotification(title, body, videoUrl)
    }

    /**
     * FallEvent 객체를 파싱하는 함수 (기존 코드와 거의 동일)
     */
    private fun parseFallEventFromData(data: Map<String, String>): FallEvent? {
        return try {
            val eventId = data["event_id"] ?: return null
            val llmAnalysisJson = data["llm_analysis_details"]
            val llmAnalysis = if (llmAnalysisJson.isNullOrBlank()) {
                """{"description":"AI 분석 정보가 없습니다."}"""
            } else {
                llmAnalysisJson
            }
            val detectionJson = data["detection_details"] ?: "{}"
            var confidence = 0f
            var impactLocation = "알 수 없음"

            if (detectionJson.isNotBlank() && detectionJson != "{}") {
                val detectionObject = Json.parseToJsonElement(detectionJson).jsonObject
                confidence = detectionObject["fall_confidence"]?.jsonPrimitive?.content?.toFloatOrNull() ?: 0f
                impactLocation = detectionObject["impact_location"]?.jsonPrimitive?.content ?: "알 수 없음"
            } else {
                Log.w(TAG, "detection_details가 비어있어 기본값을 사용합니다.")
            }

            FallEvent(
                id = eventId,
                timestamp = System.currentTimeMillis(),
                fallConfidence = confidence,
                impactLocation = impactLocation,
                llmAnalysis = llmAnalysis
                // videoUrl 필드는 나중에 업데이트되므로 여기서는 null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing event data", e)
            null
        }
    }


    /**
     *  첫 번째 알림(분석 결과)을 위한 알림 생성 함수
     * - 딥링크를 사용하여 FallDetail 화면으로 이동시킵니다.
     */
    private fun showFallDetailNotification(title: String, body: String, eventId: String) {
        val deepLinkUri = Uri.parse("senior-guard://falldetail/$eventId")
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            `package` = packageName
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        //  PendingIntent를 여기서 생성
        val pendingIntent = PendingIntent.getActivity(
            this,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // 알림 ID를 eventId의 해시코드로 설정하여 각 알림이 고유하도록 함
        val notificationId = eventId.hashCode()
        val detectionChannelId = "fall_detection_channel"
        sendNotification(title, body, detectionChannelId, pendingIntent, notificationId)
    }

    /**
     *   두 번째 알림(영상 준비)을 위한 알림 생성 함수
     * - MainActivity를 통해 Player 화면으로 videoUrl을 전달합니다.
     */
    private fun showVideoReadyNotification(title: String, body: String, videoUrl: String) {
        // 1. createRoute가 URL 인코딩을 처리합니다.
        val encodedUrl = URLEncoder.encode(videoUrl, "UTF-8")
        val deepLinkUri = Uri.parse("senior-guard://player/$encodedUrl")

        // 2. FallDetailNotification과 동일하게, 단 하나의 Intent를 만듭니다.
        val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
            `package` = packageName
            // 3. FallDetailNotification과 동일한, 강력한 플래그를 사용합니다.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 4. FallDetailNotification과 동일하게, PendingIntent.getActivity()를 사용합니다.
        val pendingIntent = PendingIntent.getActivity(
            this,
            videoUrl.hashCode(),
            intent, // 단일 Intent
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        // 알림 ID를 videoUrl의 해시코드로 설정하여 다른 알림과 겹치지 않게 함
        val notificationId = videoUrl.hashCode()
        val videoChannelId = "fall_video_channel"
        sendNotification(title, body, videoChannelId, pendingIntent, notificationId)
    }

    /**
     *  실제 시스템 알림을 생성하고 표시하는 공통 함수
     */
    private fun sendNotification(
        title: String,
        body: String,
        channelId: String,
        pendingIntent: PendingIntent,
        notificationId: Int) {

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = if (channelId == "fall_video_channel") {
                "낙상 영상 알림"
            } else {
                "낙상 감지 알림"
            }
            val descriptionText = if (channelId == "fall_video_channel") {
                "분석된 낙상 영상을 확인할 수 있을 때 알림을 받습니다."
            } else {
                "낙상 감지 시 긴급 알림을 받습니다."
            }

            val channel = NotificationChannel(
                channelId,
                name,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "낙상 감지 시 긴급 알림을 받습니다."
                // 추가적인 설정: 불빛, 진동 등
                // enableLights(true)
                // lightColor = Color.RED
                // enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
