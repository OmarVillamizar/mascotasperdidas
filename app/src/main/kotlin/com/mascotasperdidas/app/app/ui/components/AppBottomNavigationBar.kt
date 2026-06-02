package com.mascotasperdidas.app.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.navigation.Routes
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme

@Composable
fun AppBottomNavigationBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_feed)) },
            selected = currentRoute == Routes.Feed.route,
            onClick = { onTabSelected(Routes.Feed.route) },
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Map, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_map)) },
            selected = currentRoute == Routes.Map.route,
            onClick = { onTabSelected(Routes.Map.route) },
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_notifications)) },
            selected = currentRoute == Routes.Notifications.route,
            onClick = { onTabSelected(Routes.Notifications.route) },
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_profile)) },
            selected = currentRoute == Routes.ProfileTab.route,
            onClick = { onTabSelected(Routes.ProfileTab.route) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppBottomNavigationBarPreview() {
    MascotasPerdidasTheme {
        AppBottomNavigationBar(
            currentRoute = Routes.Feed.route,
            onTabSelected = {},
        )
    }
}
