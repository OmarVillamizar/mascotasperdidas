package com.mascotasperdidas.app.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mascotasperdidas.app.R

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route,
    ) {
        composable(Routes.Splash.route) {
            NavPlaceholder(
                label = "Splash → Profile",
                nextRoute = Routes.Profile.route,
                navController = navController,
            )
        }
        composable(Routes.Profile.route) {
            NavPlaceholder(
                label = "Profile → OTP",
                nextRoute = Routes.Otp.route,
                navController = navController,
            )
        }
        composable(Routes.Otp.route) {
            NavPlaceholder(
                label = "OTP → Permissions",
                nextRoute = Routes.Permissions.route,
                navController = navController,
            )
        }
        composable(Routes.Permissions.route) {
            NavPlaceholder(
                label = "Permissions → Feed",
                nextRoute = Routes.Feed.route,
                navController = navController,
            )
        }
        composable(Routes.Feed.route) {
            NavPlaceholder(
                label = "Feed (drawer → Settings)",
                nextRoute = Routes.Settings.route,
                navController = navController,
            )
        }
        composable(Routes.Settings.route) {
            NavPlaceholder(
                label = "Settings → Splash",
                nextRoute = Routes.Splash.route,
                navController = navController,
            )
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
            Text(stringResource(R.string.permissions_btn_continue))
        }
    }
}
