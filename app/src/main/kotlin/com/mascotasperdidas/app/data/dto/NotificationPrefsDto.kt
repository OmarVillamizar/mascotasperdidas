package com.mascotasperdidas.app.data.dto

data class NotificationPrefsDto(
    val lostPetsNearby: Boolean = false,
    val foundPetsNearby: Boolean = true,
    val sightingsOnMyReports: Boolean = true,
)
