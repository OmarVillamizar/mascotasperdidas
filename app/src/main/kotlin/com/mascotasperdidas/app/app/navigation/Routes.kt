package com.mascotasperdidas.app.app.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Profile : Routes("profile")
    object Otp : Routes("otp")
    object Permissions : Routes("permissions")
    object Feed : Routes("feed")
    object Settings : Routes("settings")
}
