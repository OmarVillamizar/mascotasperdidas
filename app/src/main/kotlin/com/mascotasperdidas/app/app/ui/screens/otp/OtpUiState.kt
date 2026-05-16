package com.mascotasperdidas.app.app.ui.screens.otp

data class OtpUiState(
    val phoneInput: String = "",
    val digits: List<String> = List(6) { "" },
    val verificationId: String? = null,
    val isVerifying: Boolean = false,
    val isVerified: Boolean = false,
    val error: String? = null,
) {
    val isPhoneStep: Boolean get() = verificationId == null
    val canSendCode: Boolean get() = phoneInput.filter { it.isDigit() }.length >= 10
    val canConfirm: Boolean get() = digits.all { it.isNotEmpty() }
}
