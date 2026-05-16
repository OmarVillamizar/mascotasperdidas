package com.mascotasperdidas.app.domain.model

data class User(
    val uid: String,
    val displayName: String,
    val email: String,
    val phoneNumber: String,
    val phoneVerified: Boolean,
    val photoUrl: String?,
    val notificationPrefs: NotificationPrefs,
    val createdAtEpochMs: Long,
)
