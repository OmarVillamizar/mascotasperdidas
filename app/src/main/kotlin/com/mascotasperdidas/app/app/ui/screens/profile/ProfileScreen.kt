package com.mascotasperdidas.app.app.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.AppTopBar
import com.mascotasperdidas.app.app.ui.components.LocalDrawerOpener

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEvent: (ProfileUiEvent) -> Unit,
    onNavigateToOtp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val openDrawer = LocalDrawerOpener.current
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.profile_title),
                onMenuClick = openDrawer ?: {},
                userInitial = state.userInitial,
                photoUrl = state.photoUrl,
                onAvatarClick = { /* Fase 13 */ },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Card: Mis datos ──────────────────────────────────────
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.profile_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { onEvent(ProfileUiEvent.NameChanged(it)) },
                        label = { Text(stringResource(R.string.profile_label_name)) },
                        singleLine = true,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { onEvent(ProfileUiEvent.SaveName) },
                        enabled = state.name.isNotBlank() && !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(stringResource(R.string.profile_btn_save))
                    }

                    Spacer(Modifier.height(24.dp))

                    OutlinedTextField(
                        value = state.phone,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.profile_label_phone)) },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onNavigateToOtp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.profile_btn_change_phone))
                    }
                }
            }

            // ── Error ───────────────────────────────────────────────
            state.error?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    MascotasPerdidasTheme {
        ProfileScreen(
            state = ProfileUiState(
                name = "Usuario Demo",
                phone = "+57 300 123 4567",
                email = "demo@mascotasperdidas.app",
            ),
            onEvent = {},
            onNavigateToOtp = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenEmptyPreview() {
    MascotasPerdidasTheme {
        ProfileScreen(
            state = ProfileUiState(),
            onEvent = {},
            onNavigateToOtp = {},
        )
    }
}
