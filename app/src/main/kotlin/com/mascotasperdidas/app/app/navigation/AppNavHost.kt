package com.mascotasperdidas.app.app.navigation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.mascotasperdidas.app.app.ui.components.AppDrawerContent
import com.mascotasperdidas.app.app.ui.components.DrawerShell
import com.mascotasperdidas.app.app.ui.screens.main.MainScaffold
import com.mascotasperdidas.app.app.ui.screens.main.MainViewModel
import com.mascotasperdidas.app.app.ui.screens.myreports.MyReportsScreen
import com.mascotasperdidas.app.app.ui.screens.myreports.MyReportsViewModel
import com.mascotasperdidas.app.app.ui.screens.otp.OtpScreen
import com.mascotasperdidas.app.app.ui.screens.otp.OtpViewModel
import com.mascotasperdidas.app.app.ui.screens.permissions.PermissionsScreen
import com.mascotasperdidas.app.app.ui.screens.profile.ProfileScreen
import com.mascotasperdidas.app.app.ui.screens.profile.ProfileViewModel
import com.mascotasperdidas.app.app.ui.screens.report.confirmed.ReportConfirmedScreen
import com.mascotasperdidas.app.app.ui.screens.report.creation.FoundSubTypeScreen
import com.mascotasperdidas.app.app.ui.screens.report.creation.InCareReportFormScreen
import com.mascotasperdidas.app.app.ui.screens.report.creation.InCareReportFormViewModel
import com.mascotasperdidas.app.app.ui.screens.report.creation.LostReportFormScreen
import com.mascotasperdidas.app.app.ui.screens.report.creation.LostReportFormViewModel
import com.mascotasperdidas.app.app.ui.screens.report.creation.NewReportTypeScreen
import com.mascotasperdidas.app.app.ui.screens.report.creation.SightingReportFormScreen
import com.mascotasperdidas.app.app.ui.screens.report.creation.SightingReportFormViewModel
import com.mascotasperdidas.app.app.ui.screens.report.detail.ReportDetailScreen
import com.mascotasperdidas.app.app.ui.screens.report.detail.ReportDetailViewModel
import com.mascotasperdidas.app.app.ui.screens.settings.SettingsScreen
import com.mascotasperdidas.app.app.ui.screens.settings.SettingsUiEvent
import com.mascotasperdidas.app.app.ui.screens.settings.SettingsViewModel
import com.mascotasperdidas.app.app.ui.screens.sightings.SightingsForPetScreen
import com.mascotasperdidas.app.app.ui.screens.sightings.SightingsForPetViewModel
import com.mascotasperdidas.app.app.ui.screens.splash.SplashScreen
import com.mascotasperdidas.app.app.ui.screens.splash.SplashUiEvent
import com.mascotasperdidas.app.app.ui.screens.splash.SplashViewModel
import kotlinx.coroutines.launch

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
                // Phone is a plain editable field (OTP disabled): onboarding goes
                // straight to Permissions. Re-route through Routes.Otp when real
                // OTP ships.
                onContinue = {
                    navController.navigate(Routes.Permissions.route) {
                        popUpTo(Routes.Profile.route) { inclusive = true }
                    }
                },
            )
        }

        // ── OTP ─────────────────────────────────────────────────────────
        // DISABLED: real Phone OTP is a future feature. Destination kept wired
        // (FirebaseAuthRepository.requestPhoneOtp is ready) but nothing navigates
        // here while phone is a plain field. To re-enable, route the Profile
        // action back to Routes.Otp.route.
        composable(Routes.Otp.route) {
            val viewModel: OtpViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.isVerified) {
                if (state.isVerified) {
                    navController.navigate(Routes.Permissions.route) {
                        popUpTo(Routes.Profile.route) { inclusive = true }
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
            DrawerShell(
                drawerContent = { closeDrawer ->
                    AppDrawerContent(
                        onFeedClick = {
                            closeDrawer()
                            navController.navigate(Routes.Main.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onProfileClick = {
                            closeDrawer()
                            navController.navigate(Routes.Main.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onSettingsClick = {
                            closeDrawer()
                            navController.navigate(Routes.Settings.route)
                        },
                        onPermissionsClick = { closeDrawer() },
                        onSignOutClick = {
                            closeDrawer()
                            navController.navigate(Routes.Splash.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                    )
                },
            ) {
                PermissionsScreen(
                    onContinueToFeed = {
                        navController.navigate(Routes.Main.route) {
                            popUpTo(Routes.Splash.route) { inclusive = false }
                        }
                    },
                )
            }
        }

        // ── Main (bottom nav shell) ─────────────────────────────────────
        composable(Routes.Main.route) {
            val viewModel: MainViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.isSignedOut) {
                if (state.isSignedOut) {
                    navController.navigate(Routes.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

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
                onNavigateToPermissions = { navController.navigate(Routes.Permissions.route) },
                onSignOut = viewModel::onSignOut,
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

            DrawerShell(
                drawerContent = { closeDrawer ->
                    AppDrawerContent(
                        onFeedClick = {
                            closeDrawer()
                            navController.navigate(Routes.Main.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onProfileClick = {
                            closeDrawer()
                            navController.navigate(Routes.Main.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onSettingsClick = { closeDrawer() },
                        onPermissionsClick = {
                            closeDrawer()
                            navController.navigate(Routes.Permissions.route)
                        },
                        onSignOutClick = {
                            closeDrawer()
                            viewModel.onEvent(SettingsUiEvent.SignOut)
                        },
                    )
                },
            ) {
                SettingsScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onNavigateToPermissions = { navController.navigate(Routes.Permissions.route) },
                )
            }
        }

        // ── My Reports (impl in Phase 2D-5) ─────────────────────────────
        composable(Routes.MyReports.route) {
            val viewModel: MyReportsViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            MyReportsScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReportDetail = { id, type ->
                    navController.navigate(Routes.ReportDetail.route(id, type))
                },
            )
        }

        // ── Creation wizard ───────────────────────────────────────────────
        composable(Routes.NewReport.route) {
            NewReportTypeScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLostForm = { navController.navigate(Routes.LostReportForm.route) },
                onNavigateToFoundSubType = { navController.navigate(Routes.FoundSubType.route) },
            )
        }
        composable(Routes.FoundSubType.route) {
            FoundSubTypeScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSightingForm = { navController.navigate(Routes.SightingReportForm.route) },
                onNavigateToInCareForm = { navController.navigate(Routes.InCareReportForm.route) },
            )
        }
        composable(Routes.LostReportForm.route) {
            val viewModel: LostReportFormViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            LaunchedEffect(viewModel) {
                launch { viewModel.navigateBack.collect { navController.popBackStack() } }
                launch {
                    viewModel.navigateToConfirmed.collect {
                        navController.navigate(Routes.ReportConfirmed.route("new")) {
                            popUpTo(Routes.Main.route) { inclusive = false }
                        }
                    }
                }
            }
            LostReportFormScreen(state = state, onEvent = viewModel::onEvent)
        }
        composable(Routes.SightingReportForm.route) {
            val viewModel: SightingReportFormViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            LaunchedEffect(viewModel) {
                launch { viewModel.navigateBack.collect { navController.popBackStack() } }
                launch {
                    viewModel.navigateToConfirmed.collect {
                        navController.navigate(Routes.ReportConfirmed.route("new")) {
                            popUpTo(Routes.Main.route) { inclusive = false }
                        }
                    }
                }
            }
            SightingReportFormScreen(state = state, onEvent = viewModel::onEvent)
        }
        composable(Routes.InCareReportForm.route) {
            val viewModel: InCareReportFormViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            LaunchedEffect(viewModel) {
                launch { viewModel.navigateBack.collect { navController.popBackStack() } }
                launch {
                    viewModel.navigateToConfirmed.collect {
                        navController.navigate(Routes.ReportConfirmed.route("new")) {
                            popUpTo(Routes.Main.route) { inclusive = false }
                        }
                    }
                }
            }
            InCareReportFormScreen(state = state, onEvent = viewModel::onEvent)
        }

        // ── Report Detail ────────────────────────────────────────────────
        composable(
            route = Routes.ReportDetail.route,
            arguments = listOf(
                navArgument(Routes.ReportDetail.ARG_REPORT_ID) { type = NavType.StringType },
                navArgument(Routes.ReportDetail.ARG_REPORT_TYPE) { type = NavType.StringType },
            ),
        ) {
            val viewModel: ReportDetailViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.navigateBack.collect { navController.popBackStack() }
            }

            ReportDetailScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Report Confirmed ─────────────────────────────────────────────
        composable(
            route = Routes.ReportConfirmed.route,
            arguments = listOf(
                navArgument(Routes.ReportConfirmed.ARG_REPORT_ID) { type = NavType.StringType },
            ),
        ) {
            ReportConfirmedScreen(
                onNavigateToFeed = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Main.route) { inclusive = false }
                    }
                },
            )
        }

        // ── Sightings For Pet ──────────────────────────────────────────
        composable(
            route = Routes.SightingsForPet.route,
            arguments = listOf(
                navArgument(Routes.SightingsForPet.ARG_PET_REPORT_ID) { type = NavType.StringType },
            ),
        ) {
            val viewModel: SightingsForPetViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            SightingsForPetScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMap = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Main.route) { inclusive = false }
                    }
                },
                onNavigateToReportDetail = { id, type ->
                    navController.navigate(Routes.ReportDetail.route(id, type))
                },
            )
        }
    }
}
