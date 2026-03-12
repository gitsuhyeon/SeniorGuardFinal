package com.example.seniorguard.ui.navigation

import java.net.URLEncoder

sealed class Screen(val route: String) {
    object ModeSelect : Screen("mode_select")
    object Guardian : Screen("guardian")

    //object NotificationDetail : Screen("notification/{eventId}")
    object FallDetail : Screen("fall_detail/{eventId}") {
        fun createRoute(eventId: String) = "fall_detail/$eventId"
    }

    object Monitoring : Screen("monitoring")

    //object CameraSetup : Screen("camera_setup")
    object Player : Screen("player/{videoUrl}") {
        fun createRoute(videoUrl: String): String {
            // 2. videoUrl을 인코딩하여 안전한 경로 문자열 생성
            val encodedUrl = URLEncoder.encode(videoUrl, "UTF-8")
            return "player/$encodedUrl"

        }
    }
}