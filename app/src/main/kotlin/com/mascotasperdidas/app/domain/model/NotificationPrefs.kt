package com.mascotasperdidas.app.domain.model

data class NotificationPrefs(
    val lostPetsNearby: Boolean = false,
    val foundPetsNearby: Boolean = true,
    val sightingsOnMyReports: Boolean = true,
)
