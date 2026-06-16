package com.mascotasperdidas.app.app.ui.screens.otp

data class OtpUiState(
    val phoneInput: String = "",
    val digits: List<String> = List(6) { "" },
    val verificationId: String? = null,
    val isVerifying: Boolean = false,
    val isVerified: Boolean = false,
    val error: String? = null,
) {
    /** National digits only (10 for a Colombian mobile), no country code. */
    val nationalDigits: String get() = phoneInput.filter { it.isDigit() }.take(10)

    val isPhoneStep: Boolean get() = verificationId == null

    /** Colombian mobile: exactly 10 digits starting with 3. */
    val isPhoneValid: Boolean get() = nationalDigits.length == 10 && nationalDigits.startsWith("3")

    /** True once the user typed something but it is not yet a valid CO mobile. */
    val showPhoneError: Boolean get() = nationalDigits.isNotEmpty() && !isPhoneValid

    val canSendCode: Boolean get() = isPhoneValid

    val canConfirm: Boolean get() = digits.all { it.isNotEmpty() }

    /** E.164 number ready for Firebase, e.g. +573001234567. */
    val e164Phone: String get() = "+57$nationalDigits"
}
