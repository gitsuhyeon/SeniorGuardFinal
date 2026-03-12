package com.example.seniorguard.ui.guardian.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TodayStatisticsCard(
    activityLevel: String,
    fallCount: Int
) {
    // 1. 'activityLevel' 문자열 값에 따라 색상을 결정합니다.
    val (activitySurfaceColor, activityTextColor) = when (activityLevel) {
        "양호" -> Pair(Color(0xFFEFF6FF), Color(0xFF1E40AF)) // Blue
        "주의" -> Pair(Color(0xFFFFFBEB), Color(0xFFB45309)) // Amber
        "위험" -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C)) // Red
        else -> Pair(Color(0xFFF3F4F6), Color(0xFF4B5563)) // Gray for "기록 없음" or "분석 중"
    }

    // 2. 'fallCount' 값에 따라 색상을 결정합니다.
    val (fallSurfaceColor, fallTextColor) = if (fallCount > 0) {
        Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C)) // Red
    } else {
        Pair(Color(0xFFDCFCE7), Color(0xFF166534)) // Green
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ShowChart,
                    contentDescription = "오늘의 통계",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("오늘의 통계", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 활동 수준 카드
                Surface(
                    modifier = Modifier.weight(1f),
                    color = activitySurfaceColor,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = activityLevel, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = activityTextColor)
                        Text(text = "활동 수준", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                // 낙상 횟수 카드
                Surface(
                    modifier = Modifier.weight(1f),
                    color = fallSurfaceColor,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "${fallCount}건", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = fallTextColor)
                        Text(text = "낙상 감지", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}
