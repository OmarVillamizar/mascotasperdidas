package com.mascotasperdidas.app.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mascotasperdidas.app.app.ui.screens.otp.OtpScreen
import com.mascotasperdidas.app.app.ui.screens.otp.OtpViewModel
import com.mascotasperdidas.app.app.ui.screens.permissions.PermissionsScreen
import com.mascotasperdidas.app.app.ui.screens.permissions.PermissionsViewModel
import com.mascotasperdidas.app.app.ui.screens.profile.ProfileScreen
import com.mascotasperdidas.app.app.ui.screens.profile.ProfileViewModel
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
            // PermissionsViewModel se inyecta via hiltViewModel pero no se usa state
            hiltViewModel<PermissionsViewModel>()

            PermissionsScreen(
                onContinueToFeed = {
                    navController.navigate(Routes.Feed.route) {
                        popUpTo(Routes.Splash.route) { inclusive = false }
                    }
                },
            )
        }

        // ── Placeholders (Fase 10–11) ───────────────────────────────
        composable(Routes.Feed.route) {
            NavPlaceholder("Feed → Settings", Routes.Settings.route, navController)
        }
        composable(Routes.Settings.route) {
            NavPlaceholder("Settings → Splash", Routes.Splash.route, navController)
        }
    }
}

@Composable
private fun NavPlaceholder(
    label: String,
    nextRoute: String,
    navController: NavHostController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Button(
            onClick = { navController.navigate(nextRoute) },
            modifier = Modifier.padding(top = 24.dp),
        ) {
            Text("Continuar")
        }
    }
}
