package com.mascotasperdidas.app.app.ui.screens.otp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme

@Composable
fun OtpScreen(
    state: OtpUiState,
    onEvent: (OtpUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.otp_title),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 64.dp, bottom = 12.dp),
        )

        if (state.isPhoneStep) {
            PhoneStep(
                phoneInput = state.phoneInput,
                isVerifying = state.isVerifying,
                canSendCode = state.canSendCode,
                onPhoneChanged = { onEvent(OtpUiEvent.PhoneChanged(it)) },
                onSendCode = { onEvent(OtpUiEvent.SendCode) },
            )
        } else {
            CodeStep(
                digits = state.digits,
                isVerifying = state.isVerifying,
                canConfirm = state.canConfirm,
                onDigitChanged = { i, v -> onEvent(OtpUiEvent.DigitChanged(i, v)) },
                onConfirm = { onEvent(OtpUiEvent.Confirm) },
            )
        }

        // ── Error ────────────────────────────────────────────────────
        state.error?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun PhoneStep(
    phoneInput: String,
    isVerifying: Boolean,
    canSendCode: Boolean,
    onPhoneChanged: (String) -> Unit,
    onSendCode: () -> Unit,
) {
    Text(
        text = "Ingresa tu número de teléfono",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 24.dp),
    )

    OutlinedTextField(
        value = phoneInput,
        onValueChange = { newValue ->
            // Solo dígitos y algunos caracteres de formato
            val filtered = newValue.filter { it.isDigit() || it in "+() -" }
            onPhoneChanged(filtered)
        },
        label = { Text("Teléfono") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        enabled = !isVerifying,
        modifier = Modifier.fillMaxWidth(),
    )
    Button(
        onClick = onSendCode,
        enabled = canSendCode && !isVerifying,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
    ) {
        Text("Enviar código")
    }
}

@Composable
private fun CodeStep(
    digits: List<String>,
    isVerifying: Boolean,
    canConfirm: Boolean,
    onDigitChanged: (Int, String) -> Unit,
    onConfirm: () -> Unit,
) {
    Text(
        text = stringResource(R.string.otp_subtitle),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 32.dp),
    )

    val focusRequesters = remember { List(6) { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    // Focus first field on compose
    LaunchedEffect(Unit) {
        focusRequesters.first().requestFocus()
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        digits.forEachIndexed { index, digit ->
            OutlinedTextField(
                value = digit,
                onValueChange = { value ->
                    val digitOnly = value.take(1).filter { it.isDigit() }
                    onDigitChanged(index, digitOnly)
                    if (digitOnly.isNotEmpty() && index < 5) {
                        focusRequesters[index + 1].requestFocus()
                    }
                    if (digitOnly.isEmpty() && index > 0) {
                        focusRequesters[index - 1].requestFocus()
                    }
                },
                modifier = Modifier
                    .width(48.dp)
                    .height(56.dp)
                    .focusRequester(focusRequesters[index]),
                singleLine = true,
                textStyle = TextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = if (index < 5) ImeAction.Next else ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (index < 5) focusRequesters[index + 1].requestFocus()
                    },
                    onDone = {
                        focusManager.clearFocus()
                        if (canConfirm) onConfirm()
                    },
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                enabled = !isVerifying,
            )
        }
    }

    Button(
        onClick = onConfirm,
        enabled = canConfirm && !isVerifying,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
    ) {
        Text(stringResource(R.string.otp_btn_confirm))
    }
}

@Preview(showBackground = true)
@Composable
private fun OtpPhoneStepPreview() {
    MascotasPerdidasTheme {
        OtpScreen(
            state = OtpUiState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OtpCodeStepPreview() {
    MascotasPerdidasTheme {
        OtpScreen(
            state = OtpUiState(
                verificationId = "fake-vid",
                digits = listOf("1", "2", "", "", "", ""),
            ),
            onEvent = {},
        )
    }
}
