package com.mascotasperdidas.app.app.ui.screens.profile

data class ProfileUiState(
    val name: String = "",
    /** National digits only (10 for a Colombian mobile) while editing, no country code. */
    val phone: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    // phoneVerified is persisted but NOT surfaced while OTP is disabled.
    // Re-enable the verified/unverified badge when real OTP ships. See OtpScreen.
    val phoneVerified: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
) {
    val userInitial: String get() = name.firstOrNull()?.uppercase() ?: "?"

    val nationalDigits: String get() = phone.filter { it.isDigit() }.take(10)

    /** Colombian mobile: exactly 10 digits starting with 3. Blank is allowed (optional field). */
    val isPhoneValid: Boolean get() = nationalDigits.isEmpty() ||
        (nationalDigits.length == 10 && nationalDigits.startsWith("3"))

    val showPhoneError: Boolean get() = nationalDigits.isNotEmpty() &&
        !(nationalDigits.length == 10 && nationalDigits.startsWith("3"))

    val canSavePhone: Boolean get() = nationalDigits.length == 10 && nationalDigits.startsWith("3")

    /** E.164 number ready for storage, e.g. +573001234567. Empty when no digits. */
    val e164Phone: String get() = if (nationalDigits.isBlank()) "" else "+57$nationalDigits"
}
