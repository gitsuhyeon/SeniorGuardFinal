package com.example.seniorguard.ui.mode

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest // 아마 이미 있거나 Android Studio가 자동 추가해 줄 것입니다.
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ModeSelectScreen(
    onSelectGuardian: () -> Unit,
    onSelectMonitoring: () -> Unit
) {
    // Android 13 (TIRAMISU) 이상에서만 알림 권한 상태를 관리합니다.
    val postNotificationPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // 13 미만 버전에서는 권한이 항상 허용된 것으로 간주합니다.
            rememberPermissionState(permission = "DummyPermission") { true }
        }

    // 컴포지션이 처음 시작될 때 권한을 요청합니다.
    LaunchedEffect(Unit) {
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2F855A), Color(0xFF276749))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("SeniorGuard", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text("낙상 감지 시스템", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier.widthIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ModeButton(
                    title = "보호자 모드",
                    subtitle = "낙상 발생시 알림",
                    icon = Icons.Default.PhoneAndroid,
                    onClick = onSelectGuardian
                )
                ModeButton(
                    title = "모니터링 모드",
                    subtitle = "실시간 낙상 분석",
                    icon = Icons.Default.Videocam,
                    onClick = onSelectMonitoring
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModeSelectScreenPreview() {
    // 미리보기에서는 실제 navigation 대신 빈 람다 사용
    ModeSelectScreen(
        onSelectGuardian = {},
        onSelectMonitoring = {}
    )
}

