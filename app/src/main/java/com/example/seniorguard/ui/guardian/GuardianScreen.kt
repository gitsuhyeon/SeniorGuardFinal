package com.example.seniorguard.ui.guardian

import android.R.attr.enabled
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.seniorguard.data.model.FallEvent
import com.example.seniorguard.token.TokenViewModel
import com.example.seniorguard.ui.guardian.components.SeniorStatusCard
import com.example.seniorguard.ui.guardian.components.TodayStatisticsCard
import com.example.seniorguard.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardianScreen(
    navController: NavHostController,
    viewModel: GuardianViewModel = hiltViewModel(),
    tokenViewModel: TokenViewModel = hiltViewModel() //TokenViewModel을 Hilt로부터 주입받습니다.

) {
    //  1. ViewModel의 `uiState` 하나만 구독합니다. (가장 큰 변화)
    val uiState by viewModel.uiState.collectAsState()

    //GuardianScreen이 처음 렌더링될 때 한번만 토큰 등록을 시도합니다.
    LaunchedEffect(key1 = true) {
        tokenViewModel.registerDeviceToken("guardian")
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("보호자 모드", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        enabled = uiState.recentAlerts.isNotEmpty(),
                        onClick = {
                        // 1. 테스트용 하드코딩된 영상 URL
                        // (실제로는 FallEvent 객체에서 가져와야 함)
                            val latestEvent = uiState.recentAlerts.first()
                            val videoUrl = latestEvent.videoUrl ?: "http://192.168.0.19:8000/videos/fall-video-1764471502847.mp4"

                            // 4. PlayerScreen으로 이동
                            if (videoUrl.isNotBlank()) {
                                navController.navigate(Screen.Player.createRoute(videoUrl))
                            }
                    }) {
                        Icon(Icons.Default.Videocam,
                            contentDescription = "최근 알림 영상 보기",
                            tint = if (uiState.recentAlerts.isNotEmpty()) Color.White else Color.Gray
                            )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        // 데이터 로딩 중일 때 로딩 인디케이터 표시
        if (uiState.activityLevel == "분석 중" && uiState.recentAlerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // 데이터 로딩 완료 시 UI 표시
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                //  2. 피보호자 상태 카드 (동적 데이터 연결)
                SeniorStatusCard(
                    name = "김철수", // TODO: 실제 피보호자 데이터 연결
                    age = 80,      // TODO: 실제 피보호자 데이터 연결
                    gender = "남성",
                    diseases = listOf("고혈압(심혈관계)", "퇴행성 관절염"),
                    isConnected = true,
                    lastCheck = uiState.lastCheckedText // ViewModel의 마지막 확인 시간 사용
                )

                //  3. 오늘의 통계 카드 (동적 데이터 연결)
                TodayStatisticsCard(
                    activityLevel = uiState.activityLevel, // ViewModel의 활동 수준 사용
                    fallCount = uiState.todayFallCount     // ViewModel의 오늘 낙상 횟수 사용
                )

                //  4. 최근 알림 카드 (동적 데이터 연결)
                RecentNotificationsCard(
                    fallHistory = uiState.recentAlerts, // ViewModel의 최근 알림 목록 사용
                    onItemClick = { eventId ->
                        // 클릭 시 ViewModel의 함수를 호출하고 화면을 이동합니다.
                        viewModel.selectEvent(eventId)
                        navController.navigate(Screen.FallDetail.createRoute(eventId))
                    }
                )
            }
        }
    }
}

@Composable
private fun RecentNotificationsCard(
    fallHistory: List<FallEvent>,
    onItemClick: (String) -> Unit // NavController 대신 eventId를 받는 콜백으로 변경
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "최근 알림",
                    tint = Color(0xFFEA580C)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("최근 알림", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (fallHistory.isEmpty()) {
                Text(
                    "최근 알림이 없습니다.",
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 16.dp),
                    color = Color.Gray
                )
            } else {
                //  5. take(5)로 최근 5개만 표시하고, 클릭 시 콜백 호출
                fallHistory.take(4).forEachIndexed { index, event ->
                    NotificationItem(event = event) {
                        onItemClick(event.id)
                    }
                    // 마지막 아이템 밑에는 구분선 X
                    if (index < fallHistory.take(2).lastIndex) {
                        Divider(color = Color.Gray.copy(alpha = 0.2f), thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItem(event: FallEvent, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(Color(0xFFFEE2E2))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "긴급",
                        color = Color(0xFFB91C1C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatTimestamp(event.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("낙상 감지됨", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "상세보기",
            tint = Color.Gray.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
