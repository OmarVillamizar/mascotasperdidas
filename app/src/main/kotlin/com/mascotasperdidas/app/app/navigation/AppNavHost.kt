package com.mascotasperdidas.app.app.navigation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.ui.screens.main.MainScaffold
import com.mascotasperdidas.app.app.ui.screens.otp.OtpScreen
import com.mascotasperdidas.app.app.ui.screens.otp.OtpViewModel
import com.mascotasperdidas.app.app.ui.screens.permissions.PermissionsScreen
import com.mascotasperdidas.app.app.ui.screens.profile.ProfileScreen
import com.mascotasperdidas.app.app.ui.screens.profile.ProfileViewModel
import com.mascotasperdidas.app.app.ui.screens.settings.SettingsScreen
import com.mascotasperdidas.app.app.ui.screens.settings.SettingsUiEvent
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

        // ── Splash ──────────────────────────────────────────────────────
        composable(Routes.Splash.route) {
            val viewModel: SplashViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            val context = LocalContext.current

            val signInOptions = remember {
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            }
            val googleSignInClient = remember { GoogleSignIn.getClient(context, signInOptions) }

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult(),
            ) { result ->
                if (result.resultCode != Activity.RESULT_OK) {
                    viewModel.onEvent(SplashUiEvent.ContinueWithGoogle)
                    return@rememberLauncherForActivityResult
                }
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account.idToken ?: return@rememberLauncherForActivityResult
                    viewModel.onGoogleSignInResult(idToken)
                } catch (_: ApiException) {
                    viewModel.onEvent(SplashUiEvent.ContinueWithGoogle)
                }
            }

            LaunchedEffect(state.navigateTo) {
                when (state.navigateTo) {
                    "feed" -> navController.navigate(Routes.Main.route) {
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
                    when (event) {
                        SplashUiEvent.ContinueWithGoogle -> {
                            viewModel.onEvent(event)
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    }
                },
            )
        }

        // ── Profile setup (first-time) ──────────────────────────────────
        composable(Routes.Profile.route) {
            val viewModel: ProfileViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            ProfileScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateToOtp = { navController.navigate(Routes.Otp.route) },
            )
        }

        // ── OTP ─────────────────────────────────────────────────────────
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

        // ── Permissions ─────────────────────────────────────────────────
        composable(Routes.Permissions.route) {
            PermissionsScreen(
                onContinueToFeed = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Splash.route) { inclusive = false }
                    }
                },
            )
        }

        // ── Main (bottom nav shell) ─────────────────────────────────────
        composable(Routes.Main.route) {
            MainScaffold(
                onNavigateToSettings = { navController.navigate(Routes.Settings.route) },
                onNavigateToNewReport = { navController.navigate(Routes.NewReport.route) },
                onNavigateToReportDetail = { id, type ->
                    navController.navigate(Routes.ReportDetail.route(id, type))
                },
                onNavigateToMyReports = { navController.navigate(Routes.MyReports.route) },
                onNavigateToSightingsForPet = { id ->
                    navController.navigate(Routes.SightingsForPet.route(id))
                },
                onNavigateToOtp = { navController.navigate(Routes.Otp.route) },
            )
        }

        // ── Settings ────────────────────────────────────────────────────
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
                onNavigateToPermissions = { navController.navigate(Routes.Permissions.route) },
            )
        }

        // ── My Reports (impl in Phase 2D-5) ─────────────────────────────
        composable(Routes.MyReports.route) {
            PlaceholderScreen(label = "Mis Reportes")
        }

        // ── Creation wizard (impl in Phase 2D-2) ─────────────────────────
        composable(Routes.NewReport.route) {
            PlaceholderScreen(label = "Nuevo Reporte")
        }
        composable(Routes.FoundSubType.route) {
            PlaceholderScreen(label = "Tipo de Avistamiento")
        }
        composable(Routes.LostReportForm.route) {
            PlaceholderScreen(label = "Formulario Perdido")
        }
        composable(Routes.SightingReportForm.route) {
            PlaceholderScreen(label = "Formulario Avistamiento")
        }
        composable(Routes.InCareReportForm.route) {
            PlaceholderScreen(label = "Formulario Bajo Cuidado")
        }

        // ── Report Detail (impl in Phase 2D-1) ──────────────────────────
        composable(
            route = Routes.ReportDetail.route,
            arguments = listOf(
                navArgument(Routes.ReportDetail.ARG_REPORT_ID) { type = NavType.StringType },
                navArgument(Routes.ReportDetail.ARG_REPORT_TYPE) { type = NavType.StringType },
            ),
        ) {
            PlaceholderScreen(label = "Detalle de Reporte")
        }

        // ── Report Confirmed (impl in Phase 2D-3) ────────────────────────
        composable(
            route = Routes.ReportConfirmed.route,
            arguments = listOf(
                navArgument(Routes.ReportConfirmed.ARG_REPORT_ID) { type = NavType.StringType },
            ),
        ) {
            PlaceholderScreen(label = "Publicación Confirmada")
        }

        // ── Sightings For Pet (impl in Phase 2D-6) ──────────────────────
        composable(
            route = Routes.SightingsForPet.route,
            arguments = listOf(
                navArgument(Routes.SightingsForPet.ARG_PET_REPORT_ID) { type = NavType.StringType },
            ),
        ) {
            PlaceholderScreen(label = "Avistamientos")
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label, style = MaterialTheme.typography.headlineSmall)
    }
}
