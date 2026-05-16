package com.mascotasperdidas.app.data.dto

import com.google.firebase.Timestamp

data class UserDto(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val phoneVerified: Boolean = false,
    val photoUrl: String? = null,
    val notificationPrefs: NotificationPrefsDto = NotificationPrefsDto(),
    val createdAt: Timestamp? = null,
)
