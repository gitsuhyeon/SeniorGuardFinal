package com.example.seniorguard.ui.monitoring

import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.seniorguard.token.TokenViewModel
import com.example.seniorguard.ui.monitoring.components.SkeletonOverlay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MonitoringScreen(
    viewModel: MonitoringViewModel= hiltViewModel(),
    tokenViewModel: TokenViewModel = hiltViewModel(),

            // (선택) 낙상 감지 후 화면 전환을 위한 콜백
    onFallDetectedAndConfirmed: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    //  1. ViewModel의 isFallDetected 상태 구독
    val isFallDetected by viewModel.isFallDetected.collectAsState()

    // 화면에서 벗어날 때 카메라 리소스를 정리하기 위한 Hook
    DisposableEffect(lifecycleOwner) {
        onDispose {
            viewModel.shutdownCamera()
            viewModel.releaseMediaPipe() // MediaPipe 리소스도 함께 정리
        }
    }

    // 컴포지션이 시작될 때 권한 상태를 확인하고, 없으면 요청
    LaunchedEffect(Unit) {
        // FCM 토큰 등록
        tokenViewModel.registerDeviceToken("monitoring")

        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        //  2. isFallDetected 상태에 따라 UI 분기
        if (isFallDetected) {
            // --- 낙상이 감지되었을 때 ---
            // 사용자에게 상황을 알리는 대화상자를 표시
            AlertDialog(
                onDismissRequest = {
                    // 대화상자 바깥을 클릭해도 닫히지 않도록 아무것도 하지 않음
                },
                title = { Text("🚨 낙상 감지됨") },
                text = { Text("보호자에게 알림을 전송했습니다. 실시간 분석을 중단합니다.") },
                confirmButton = {
                    Button(onClick = {
                        //  ViewModel을 통해 상태를 초기화하고, NavGraph로 받은 콜백을 호출합니다.
                        viewModel.resetFallDetectionState()
                        onFallDetectedAndConfirmed()
                    }) {
                        Text("확인")
                }}
            )
        } else {
            // --- 정상적인 모니터링 상태일 때 ---
            // 카메라 권한 상태에 따라 UI 분기
            if (cameraPermissionState.status.isGranted) {
                // --- 권한이 허용되었을 때 ---
                // 카메라 프리뷰와 스켈레톤 오버레이를 표시
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx)
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { previewView ->
                        // AndroidView가 준비되면 카메라 시작
                        viewModel.startCamera(context, lifecycleOwner, previewView)
                    }
                )

                val skeleton by viewModel.skeletonState.collectAsState()
                SkeletonOverlay(skeleton = skeleton)

            } else {
                // --- 권한이 거부되었을 때 ---
                // 사용자에게 권한이 필요하다는 안내 메시지를 표시
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("카메라 권한이 필요합니다.")
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("권한 요청")
                    }
                }
            }
        }
    }
}
