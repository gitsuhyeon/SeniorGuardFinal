package com.example.seniorguard.ui.guardian

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun GuardianBottomNav(
    current: String,
    onChange: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = current == "home",
            onClick = { onChange("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "홈") },
            label = { Text("홈") }
        )
        NavigationBarItem(
            selected = current == "notification",
            onClick = { onChange("notification") },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "알림") },
            label = { Text("알림") }
        )
        NavigationBarItem(
            selected = current == "settings",
            onClick = { onChange("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "설정") },
            label = { Text("설정") }
        )
    }
}
