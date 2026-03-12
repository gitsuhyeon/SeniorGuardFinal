package com.example.seniorguard.ui.player


import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
@OptIn(ExperimentalMaterial3Api::class) // TopAppBar 사용을 위해 추가
@Composable
fun PlayerScreen(
    navController: NavHostController,
    videoUrl: String) {
    val context = LocalContext.current

    // 1. ExoPlayer 인스턴스를 기억(remember)합니다.
    // Composable이 재구성되어도 플레이어는 유지됩니다.
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // 2. 전달받은 videoUrl로 미디어 아이템을 설정합니다.
            setMediaItem(MediaItem.fromUri(videoUrl))
            // 3. 재생을 준비합니다.
            prepare()
            // 4. 즉시 재생을 시작합니다.
            playWhenReady = true
        }
    }

    // 5. Composable이 화면에서 사라질 때 플레이어를 해제(release)합니다.
    // 메모리 누수를 방지하는 매우 중요한 과정입니다.
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }



    //  화면의 전체적인 레이아웃을 위해 Scaffold를 사용합니다.
    Scaffold(
    topBar = {
        TopAppBar(
            title = { Text("최근 낙상 영상 재생") },
            //  뒤로 가기 버튼 (IconButton)
            navigationIcon = {
                IconButton(onClick = {
                    //  navController를 사용하여 이전 화면으로 이동합니다.
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로 가기"
                    )
                }
            }
        )
    }
    ) { innerPadding -> // Scaffold가 제공하는 패딩 값
        //  AndroidView를 Scaffold의 콘텐츠 영역에 배치하고, 상단 바에 가려지지 않도록 패딩을 적용합니다.
        AndroidView(
            modifier = Modifier.padding(innerPadding),
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                }
            }
        )
    }
}
