package com.example.seniorguard.ui.guardian.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SeniorStatusCard(
    name: String,
    age: Int,
    gender: String, //  성별 파라미터 추가
    diseases: List<String>, //  질환 목록 파라미터 추가
    isConnected: Boolean, // 이 파라미터는 UI 분기 처리에 사용될 수 있습니다.
    lastCheck: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // 배경색을 테마에 맞게 변경
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 상단: 이름 + 상태
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFFE0F2F1), // 색상 미세 조정 (Teal 50)
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "피보호자 아이콘",
                            tint = Color(0xFF00796B), // 색상 미세 조정 (Teal 700)
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "${name}님",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        //  나이와 성별을 한 줄에 표시
                        Text(
                            text = "${age}세 / $gender",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                // "정상" 상태 태그
                Surface(
                    color = if (isConnected) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "연결 상태 아이콘",
                            tint = if (isConnected) Color(0xFF166534) else Color(0xFFB91C1C),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isConnected) "정상" else "연결 끊김",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isConnected) Color(0xFF166534) else Color(0xFFB91C1C)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.Gray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            //  중앙: 주요 질환 정보 추가
            InfoRow(title = "주요 질환", content = diseases.joinToString(", "))

            Spacer(modifier = Modifier.height(8.dp))

            // 하단: 연결 정보
            InfoRow(
                title = "모니터링 기기",
                content = if (isConnected) "✓ 연결됨" else "✗ 연결 끊김",
                contentColor = if (isConnected) Color(0xFF166534) else Color(0xFFB91C1C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(title = "마지막 확인", content = lastCheck)
        }
    }
}

/**
 * 정보 행을 위한 재사용 가능한 Composable
 */
@Composable
private fun InfoRow(
    title: String,
    content: String,
    contentColor: Color = Color.Black // 기본 텍스트 색상
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}
