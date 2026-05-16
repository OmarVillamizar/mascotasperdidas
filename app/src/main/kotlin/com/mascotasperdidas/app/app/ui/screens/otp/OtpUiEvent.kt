package com.mascotasperdidas.app.app.ui.screens.otp

sealed class OtpUiEvent {
    data class PhoneChanged(val phone: String) : OtpUiEvent()
    object SendCode : OtpUiEvent()
    data class DigitChanged(val index: Int, val value: String) : OtpUiEvent()
    object Confirm : OtpUiEvent()
}
