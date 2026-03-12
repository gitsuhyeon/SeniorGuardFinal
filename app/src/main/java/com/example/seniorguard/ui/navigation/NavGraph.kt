package com.example.seniorguard.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.seniorguard.ui.mode.ModeSelectScreen
import com.example.seniorguard.ui.monitoring.MonitoringScreen
import com.example.seniorguard.ui.monitoring.MonitoringViewModel

import androidx.navigation.navDeepLink //알림 누를때 Intent로 화면 이동시 필요
import com.example.seniorguard.ui.falldetail.FallDetailScreen
import com.example.seniorguard.ui.guardian.GuardianScreen
import com.example.seniorguard.ui.player.PlayerScreen
import java.net.URLDecoder

@Composable
fun NavGraph (
    navController: NavHostController,
    startDestination: String = Screen.ModeSelect.route
){
    NavHost(navController, startDestination) {
        composable(Screen.ModeSelect.route) {
            Log.d("NavGraph", "startDestination = $startDestination")
            ModeSelectScreen(
                onSelectGuardian = { navController.navigate(Screen.Guardian.route) },
                onSelectMonitoring = { navController.navigate(Screen.Monitoring.route) }
            )
        }

        composable(
            route = Screen.Guardian.route,

        ) {
            GuardianScreen(navController = navController)
        }

        composable(
            route = Screen.FallDetail.route,
            // 딥 링크 설정을 deepLinks 파라미터에 리스트 형태로 추가합니다.
            deepLinks = listOf(
                navDeepLink { uriPattern = "senior-guard://falldetail/{eventId}" }
            ),
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })

        ) {
            FallDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        /*
        composable(Screen.NotificationDetail.route) {
            NotificationDetailScreen(navController)
        }
         */
        composable(Screen.Monitoring.route) {
            //  1. 여기서 hiltViewModel()을 호출하여 ViewModel 인스턴스를 생성합니다.
            // NavHost의 composable 스코프 내에서 생성되므로, 이 ViewModel은 화면이 재구성되어도 유지됩니다.
            val viewModel: MonitoringViewModel = hiltViewModel()

            //  MonitoringScreen에 콜백을 추가하고, 그 안에서 내비게이션 로직을 구현합니다.
            MonitoringScreen(
                viewModel = viewModel,
                onFallDetectedAndConfirmed = {
                    // 현재 화면(Monitoring)을 스택에서 제거하고,
                    // 시작 화면(ModeSelect)으로 이동합니다.
                    navController.popBackStack(
                        route = Screen.ModeSelect.route,
                        inclusive = false // ModeSelect 화면은 스택에 남겨둡니다.
                    )
                }
            )
        }
        /*
        composable(Screen.CameraSetup.route) {
            CameraSetupScreen(navController)
        }

         */

        composable(
            route = Screen.Player.route, // 인자 받도록 수정
            arguments = listOf(
                navArgument("videoUrl") {
                    type = NavType.StringType
                    //nullable = false
                }
            ),
            //  PlayerScreen으로 오는 딥링크 정의
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "senior-guard://player/{videoUrl}" // 딥링크 패턴 정의
                }
            )
        ) { backStackEntry ->
            // URL 인코딩된 videoUrl을 디코딩하여 PlayerScreen에 전달
            val encodedVideoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            val decodedVideoUrl = URLDecoder.decode(encodedVideoUrl, "UTF-8")
            PlayerScreen(
                navController = navController,
                videoUrl = decodedVideoUrl)
        }
    }
}

