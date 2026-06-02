package com.mascotasperdidas.app.app.ui.components

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

// ── CompositionLocals para drawer ──────────────────────────────────
val LocalDrawerOpener = compositionLocalOf<(() -> Unit)?> { null }
val LocalDrawerCloser = compositionLocalOf<(() -> Unit)?> { null }

@Composable
fun DrawerShell(
    drawerContent: @Composable (closeDrawer: () -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(
        LocalDrawerOpener provides { scope.launch { drawerState.open() } },
        LocalDrawerCloser provides { scope.launch { drawerState.close() } },
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                drawerContent { scope.launch { drawerState.close() } }
            },
        ) {
            content()
        }
    }
}
