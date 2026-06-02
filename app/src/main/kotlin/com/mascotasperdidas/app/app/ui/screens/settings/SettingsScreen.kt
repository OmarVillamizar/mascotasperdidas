package com.mascotasperdidas.app.app.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.AppTopBar
import com.mascotasperdidas.app.app.ui.components.LocalDrawerOpener

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val openDrawer = LocalDrawerOpener.current
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings_title),
                onMenuClick = openDrawer ?: {},
                userInitial = "?",
                photoUrl = null,
                onAvatarClick = { /* Fase 13 */ },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Sección: Notificaciones ──────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_notifications))

            CheckboxItem(
                text = stringResource(R.string.settings_notif_lost_nearby),
                checked = state.lostPetsNearby,
                onToggle = { onEvent(SettingsUiEvent.ToggleLostNearby) },
            )
            CheckboxItem(
                text = stringResource(R.string.settings_notif_found_nearby),
                checked = state.foundPetsNearby,
                onToggle = { onEvent(SettingsUiEvent.ToggleFoundNearby) },
            )
            CheckboxItem(
                text = stringResource(R.string.settings_notif_sightings),
                checked = state.sightingsOnMyReports,
                onToggle = { onEvent(SettingsUiEvent.ToggleSightings) },
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(16.dp))

            // ── Sección: Permisos ────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_permissions))

            Button(
                onClick = onNavigateToPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Text(stringResource(R.string.settings_btn_manage_permissions))
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp))
            Spacer(Modifier.height(8.dp))

            // ── Sección: Mi cuenta ───────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_account))

            Button(
                onClick = { onEvent(SettingsUiEvent.SignOut) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Text(stringResource(R.string.settings_btn_sign_out))
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = { onEvent(SettingsUiEvent.ShowDeleteDialog) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_btn_delete_account),
                    color = MaterialTheme.colorScheme.error,
                )
            }

            // ── Error ───────────────────────────────────────────────
            state.error?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ── AlertDialog: Eliminar cuenta ─────────────────────────────────
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onEvent(SettingsUiEvent.DismissDeleteDialog) },
            title = { Text(stringResource(R.string.settings_dialog_delete_title)) },
            text = { Text(stringResource(R.string.settings_dialog_delete_msg)) },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(SettingsUiEvent.DeleteAccountConfirmed) },
                ) {
                    Text(
                        text = stringResource(R.string.settings_dialog_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onEvent(SettingsUiEvent.DismissDeleteDialog) },
                ) {
                    Text(stringResource(R.string.settings_dialog_cancel))
                }
            },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun CheckboxItem(
    text: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    MascotasPerdidasTheme {
        SettingsScreen(
            state = SettingsUiState(
                lostPetsNearby = false,
                foundPetsNearby = true,
                sightingsOnMyReports = true,
            ),
            onEvent = {},
            onNavigateToPermissions = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenDeleteDialogPreview() {
    MascotasPerdidasTheme {
        SettingsScreen(
            state = SettingsUiState(showDeleteDialog = true),
            onEvent = {},
            onNavigateToPermissions = {},
        )
    }
}
