package com.example.seniorguard.ui.falldetail

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.seniorguard.data.model.FallEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FallDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: FallDetailViewModel = hiltViewModel()
) {
    val event by viewModel.fallEvent.collectAsState()
    val resumeState by viewModel.resumeUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(resumeState) {
        when (resumeState) {
            is ResumeUiState.Success -> {
                snackbarHostState.showSnackbar(" 감지 시스템이 다시 활성화되었습니다.")
                viewModel.resetResumeState()
            }
            is ResumeUiState.Error -> {
                snackbarHostState.showSnackbar(" 오류: 감지 시스템을 활성화하지 못했습니다.")
                viewModel.resetResumeState()
            }
            else -> {}
        }
    }

    val llmDescription = event.llmAnalysis?.let { analysis ->
        try {
            val jsonElement = Json.parseToJsonElement(analysis)
            if (jsonElement is JsonObject) {
                jsonElement["description"]?.jsonPrimitive?.content ?: "분석 내용이 없습니다."
            } else {
                jsonElement.jsonPrimitive.content
            }
        }catch (e: Exception) {
            Log.e("FallDetailScreen", "LLM 분석 JSON 파싱 실패", e)
            "분석 내용을 파싱하는 데 실패했습니다."
        }
    } ?: "AI 분석 정보가 없습니다."

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("낙상 상세 정보", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF0F2F5))
            )
        },
        //  스낵바 호스트 추가
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF0F2F5)
    ) { paddingValues ->
        //  데이터 로딩 상태에 따라 UI 분기 처리 수정
        if (event.id.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EmergencyBanner(eventTime = formatTimestampToDetail(event.timestamp))
                DetailInfoCard(event)
                AiAnalysisCard(analysisText = llmDescription)
                RecommendedActionsCard()
                Spacer(Modifier.height(8.dp))
                ActionButtons(eventTimestamp = event.timestamp, llmAnalysis = llmDescription)

                //  '상황 해제' 버튼 추가
                Spacer(Modifier.height(4.dp)) // 버튼 사이 간격
                ResumeButton(
                    resumeState = resumeState,
                    onClick = { viewModel.resumeFallDetection() }
                )
            }
        }
    }
}

//  '상황 해제' 버튼 Composable 추가
@Composable
private fun ResumeButton(
    resumeState: ResumeUiState,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = resumeState !is ResumeUiState.Loading, // 로딩 중일 때 비활성화
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        when (resumeState) {
            is ResumeUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            }
            else -> {
                Text(
                    "상황 해제 및 감지 재시작",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}


private fun formatTimestampToDetail(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun EmergencyBanner(eventTime: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "긴급",
                modifier = Modifier.size(32.dp),
                tint = Color(0xFFDC2626)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "긴급 낙상 발생",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFFB91C1C)
                )
                Text(
                    eventTime,
                    fontSize = 14.sp,
                    color = Color(0xFF7F1D1D)
                )
            }
        }
    }
}

@Composable
fun DetailInfoCard(event: FallEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("상세 정보", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            DetailInfoRow(icon = Icons.Default.Info, label = "유형", value = "낙상")
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            DetailInfoRow(icon = Icons.Default.Info, label = "충격 부위", value = event.impactLocation ?: "정보 없음")
        }
    }
}

@Composable
fun DetailInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text("$label:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AiAnalysisCard(analysisText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("AI 분석 리포트", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Text(analysisText, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp)
        }
    }
}

@Composable
fun RecommendedActionsCard() {
    val actions = listOf(
        "즉시 119에 신고하여 상황을 알리세요.",
        "환자를 함부로 움직이지 마세요.",
        "의식과 호흡을 확인하세요.",
        "담요 등으로 체온을 유지시켜 주세요."
    )
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFFEFCE8)),
        border = BorderStroke(1.dp, Color(0xFFFACC15))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = "권장 조치", tint = Color(0xFFCA8A04))
                Spacer(Modifier.width(8.dp))
                Text(
                    "권장 조치",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF854D0E)
                )
            }
            Spacer(Modifier.height(12.dp))
            actions.forEach { action ->
                Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(action, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF333333))
                }
            }
        }
    }
}

@Composable
fun ActionButtons(eventTimestamp: Long, llmAnalysis: String) {
    val context = LocalContext.current
    val shareText = """
        [긴급 낙상 알림]
        - 발생 시각: ${formatTimestampToDetail(eventTimestamp)}
        - AI 분석: $llmAnalysis
        - 확인이 필요합니다.
    """.trimIndent()

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:119") }
                context.startActivity(dialIntent)
            },
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Call, contentDescription = "119 연결")
            Spacer(Modifier.width(8.dp))
            Text("119 신고", fontWeight = FontWeight.Bold)
        }
        OutlinedButton(
            onClick = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "낙상 정보 공유")
                context.startActivity(shareIntent)
            },
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = "공유하기")
            Spacer(Modifier.width(8.dp))
            Text("정보 공유", fontWeight = FontWeight.Bold)
        }
    }
}
