package com.mascotasperdidas.app.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mascotasperdidas.app.app.ui.components.AppDrawerContent
import com.mascotasperdidas.app.app.ui.components.DrawerShell
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
        // ── Splash (sin drawer) ─────────────────────────────────────
        composable(Routes.Splash.route) {
            val viewModel: SplashViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.navigateTo) {
                when (state.navigateTo) {
                    "feed" -> navController.navigate(Routes.Feed.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                    "profile" -> navController.navigate(Routes.Profile.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            }

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

        // ── Profile (con drawer) ────────────────────────────────────
        composable(Routes.Profile.route) {
            DrawerShell(
                drawerContent = { closeDrawer ->
                    AppDrawerContent(
                        onFeedClick = { navController.navigate(Routes.Feed.route) },
                        onProfileClick = { closeDrawer() },
                        onSettingsClick = { navController.navigate(Routes.Settings.route) },
                        onPermissionsClick = { navController.navigate(Routes.Permissions.route) },
                        onSignOutClick = {
                            navController.navigate(Routes.Splash.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                    )
                },
            ) {
                val viewModel: ProfileViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()

                ProfileScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onNavigateToOtp = { navController.navigate(Routes.Otp.route) },
                )
            }
        }

        // ── OTP (sin drawer) ────────────────────────────────────────
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

        // ── Permissions (con drawer) ────────────────────────────────
        composable(Routes.Permissions.route) {
            DrawerShell(
                drawerContent = { closeDrawer ->
                    AppDrawerContent(
                        onFeedClick = { navController.navigate(Routes.Feed.route) },
                        onProfileClick = { navController.navigate(Routes.Profile.route) },
                        onSettingsClick = { navController.navigate(Routes.Settings.route) },
                        onPermissionsClick = { closeDrawer() },
                        onSignOutClick = {
                            navController.navigate(Routes.Splash.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                    )
                },
            ) {
                PermissionsScreen(
                    onContinueToFeed = {
                        navController.navigate(Routes.Feed.route) {
                            popUpTo(Routes.Splash.route) { inclusive = false }
                        }
                    },
                )
            }
        }

        // ── Feed (con drawer) ───────────────────────────────────────
        composable(Routes.Feed.route) {
            DrawerShell(
                drawerContent = { closeDrawer ->
                    AppDrawerContent(
                        onFeedClick = { closeDrawer() },
                        onProfileClick = { navController.navigate(Routes.Profile.route) },
                        onSettingsClick = { navController.navigate(Routes.Settings.route) },
                        onPermissionsClick = { navController.navigate(Routes.Permissions.route) },
                        onSignOutClick = {
                            navController.navigate(Routes.Splash.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                    )
                },
            ) {
                val viewModel: FeedViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()

                FeedScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                )
            }
        }

        // ── Settings (con drawer) ───────────────────────────────────
        composable(Routes.Settings.route) {
            DrawerShell(
                drawerContent = { closeDrawer ->
                    AppDrawerContent(
                        onFeedClick = { navController.navigate(Routes.Feed.route) },
                        onProfileClick = { navController.navigate(Routes.Profile.route) },
                        onSettingsClick = { closeDrawer() },
                        onPermissionsClick = { navController.navigate(Routes.Permissions.route) },
                        onSignOutClick = {
                            navController.navigate(Routes.Splash.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                    )
                },
            ) {
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
}
