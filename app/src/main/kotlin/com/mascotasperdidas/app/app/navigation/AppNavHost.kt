package com.mascotasperdidas.app.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mascotasperdidas.app.app.ui.screens.feed.FeedScreen
import com.mascotasperdidas.app.app.ui.screens.feed.FeedViewModel
import com.mascotasperdidas.app.app.ui.screens.otp.OtpScreen
import com.mascotasperdidas.app.app.ui.screens.otp.OtpViewModel
import com.mascotasperdidas.app.app.ui.screens.permissions.PermissionsScreen
import com.mascotasperdidas.app.app.ui.screens.profile.ProfileScreen
import com.mascotasperdidas.app.app.ui.screens.profile.ProfileViewModel
import com.mascotasperdidas.app.app.ui.screens.settings.SettingsScreen
import com.mascotasperdidas.app.app.ui.screens.settings.SettingsViewModel
import com.mascotasperdidas.app.app.ui.screens.splash.SplashScreen
import com.mascotasperdidas.app.app.ui.screens.splash.SplashUiEvent
import com.mascotasperdidas.app.app.ui.screens.splash.SplashViewModel

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route,
    ) {
        // ── Splash ──────────────────────────────────────────────────
        composable(Routes.Splash.route) {
            val viewModel: SplashViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            SplashScreen(
                state = state,
                onEvent = { event ->
                    viewModel.onEvent(event)
                    if (event is SplashUiEvent.ContinueWithGoogle) {
                        navController.navigate(Routes.Profile.route)
                    }
                },
            )
        }

        // ── Profile ─────────────────────────────────────────────────
        composable(Routes.Profile.route) {
            val viewModel: ProfileViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            ProfileScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateToOtp = { navController.navigate(Routes.Otp.route) },
            )
        }

        // ── OTP ─────────────────────────────────────────────────────
        composable(Routes.Otp.route) {
            val viewModel: OtpViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.isVerified) {
                if (state.isVerified) {
                    navController.navigate(Routes.Permissions.route) {
                        popUpTo(Routes.Profile.route)
                    }
                }
            }

            OtpScreen(
                state = state,
                onEvent = viewModel::onEvent,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // ── Permissions ─────────────────────────────────────────────
        composable(Routes.Permissions.route) {
            PermissionsScreen(
                onContinueToFeed = {
                    navController.navigate(Routes.Feed.route) {
                        popUpTo(Routes.Splash.route) { inclusive = false }
                    }
                },
            )
        }

        // ── Feed ────────────────────────────────────────────────────
        composable(Routes.Feed.route) {
            val viewModel: FeedViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            FeedScreen(
                state = state,
                onEvent = viewModel::onEvent,
            )
        }

        // ── Settings ────────────────────────────────────────────────
        composable(Routes.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.isSignedOut) {
                if (state.isSignedOut) {
                    navController.navigate(Routes.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            LaunchedEffect(state.isAccountDeleted) {
                if (state.isAccountDeleted) {
                    navController.navigate(Routes.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            SettingsScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateToPermissions = {
                    navController.navigate(Routes.Permissions.route)
                },
            )
        }
    }
}
