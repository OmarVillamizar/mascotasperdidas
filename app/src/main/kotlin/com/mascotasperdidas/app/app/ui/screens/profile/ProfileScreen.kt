package com.mascotasperdidas.app.app.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
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
    // Onboarding-only: shows a "Continuar" button that advances to Permissions.
    // Null on the Profile tab (no forward step).
    onContinue: (() -> Unit)? = null,
    onNavigateToMyReports: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSignOut: () -> Unit = {},
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

                    // ── Phone: editable plain field (no OTP) ─────────────
                    // Used for WhatsApp / call contact features. Stored as E.164.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.height(56.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.otp_country_code),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = { onEvent(ProfileUiEvent.PhoneChanged(it)) },
                            label = { Text(stringResource(R.string.profile_label_phone)) },
                            singleLine = true,
                            isError = state.showPhoneError,
                            enabled = !state.isSaving,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (state.showPhoneError) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.otp_phone_invalid),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    FilledTonalButton(
                        onClick = { onEvent(ProfileUiEvent.SavePhone) },
                        enabled = state.canSavePhone && !state.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.profile_btn_save_phone))
                    }

                    // ── OTP verification — DISABLED for now ──────────────
                    // Future feature: real Phone OTP. When enabled, re-add:
                    //  - the verified/unverified badge driven by state.phoneVerified
                    //  - an OutlinedButton "Verificar teléfono" -> onNavigateToOtp
                    // The OTP screen/flow code still exists (see OtpScreen,
                    // OtpViewModel, FirebaseAuthRepository.requestPhoneOtp).
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

            Spacer(Modifier.height(16.dp))

            // ── Ver mis reportes ─────────────────────────────────────
            OutlinedButton(
                onClick = onNavigateToMyReports,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.ver_mis_reportes))
            }

            // ── Onboarding: continue to Permissions ──────────────────
            if (onContinue != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        onEvent(ProfileUiEvent.SavePhone)
                        onContinue()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.continuar))
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Cerrar Sesión ────────────────────────────────────────
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(stringResource(R.string.drawer_sign_out))
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
                phone = "3001234567",
                email = "demo@mascotasperdidas.app",
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenOnboardingPreview() {
    MascotasPerdidasTheme {
        ProfileScreen(
            state = ProfileUiState(),
            onEvent = {},
            onContinue = {},
        )
    }
}
