package com.mascotasperdidas.app.app.ui.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mascotasperdidas.app.app.navigation.Routes
import com.mascotasperdidas.app.app.ui.components.AppBottomNavigationBar
import com.mascotasperdidas.app.app.ui.screens.feed.FeedScreen
import com.mascotasperdidas.app.app.ui.screens.feed.FeedViewModel
import com.mascotasperdidas.app.app.ui.screens.map.MapScreen
import com.mascotasperdidas.app.app.ui.screens.map.MapViewModel
import com.mascotasperdidas.app.app.ui.screens.notifications.NotificationsPlaceholderScreen
import com.mascotasperdidas.app.app.ui.screens.profile.ProfileScreen
import com.mascotasperdidas.app.app.ui.screens.profile.ProfileViewModel

@Composable
fun MainScaffold(
    onNavigateToSettings: () -> Unit,
    onNavigateToNewReport: () -> Unit,
    onNavigateToReportDetail: (reportId: String, reportType: String) -> Unit,
    onNavigateToMyReports: () -> Unit,
    onNavigateToSightingsForPet: (petReportId: String) -> Unit,
    onNavigateToOtp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            AppBottomNavigationBar(
                currentRoute = currentRoute,
                onTabSelected = { route ->
                    bottomNavController.navigate(route) {
                        popUpTo(bottomNavController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Routes.Feed.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(Routes.Feed.route) {
                val viewModel: FeedViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()
                FeedScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onNavigateToNewReport = onNavigateToNewReport,
                    onNavigateToReportDetail = onNavigateToReportDetail,
                    onNavigateToSettings = onNavigateToSettings,
                )
            }
            composable(Routes.Map.route) {
                val viewModel: MapViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()
                MapScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onNavigateToNewReport = onNavigateToNewReport,
                    onNavigateToReportDetail = onNavigateToReportDetail,
                )
            }
            composable(Routes.Notifications.route) {
                NotificationsPlaceholderScreen()
            }
            composable(Routes.ProfileTab.route) {
                val viewModel: ProfileViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()
                ProfileScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onNavigateToOtp = onNavigateToOtp,
                    onNavigateToMyReports = onNavigateToMyReports,
                    onNavigateToSettings = onNavigateToSettings,
                )
            }
        }
    }
}
