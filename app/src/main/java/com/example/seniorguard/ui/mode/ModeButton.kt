package com.example.seniorguard.ui.mode

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * 모드 선택 버튼 Composable
 * - title: 버튼 제목
 * - subtitle: 부제목 (nullable)
 * - icon: 아이콘 이미지
 * - onClick: 클릭 시 실행할 람다
 */
@Composable
fun ModeButton(
    title: String,
    subtitle: String?,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // 클릭 이벤트 처리
        shape = RoundedCornerShape(20.dp), // 카드 모서리 둥글게
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // 그림자 효과
        colors = CardDefaults.cardColors(containerColor = Color.White) // 카드 배경색
    ) {
        // Column 대신 Row를 사용하여 가로로 나열
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            verticalAlignment = Alignment.CenterVertically,
            // ✨ Row 내부 콘텐츠를 수평 중앙에 배치
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2F855A),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp)) // 아이콘과 텍스트 사이 간격

            // 텍스트를 담는 Column
            Column(
                horizontalAlignment = Alignment.Start // 텍스트는 좌측 정렬 유지
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2F855A)
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }


        /*
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {

            // 아이콘 표시
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2F855A),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 제목 텍스트
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2F855A)
            )

            // 부제목이 있을 경우 표시
            subtitle?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

    }
}
}
*/
}