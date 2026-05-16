package com.mascotasperdidas.app.data.mapper

import com.mascotasperdidas.app.data.dto.NotificationPrefsDto
import com.mascotasperdidas.app.data.dto.UserDto
import com.mascotasperdidas.app.domain.model.NotificationPrefs
import com.mascotasperdidas.app.domain.model.User

fun UserDto.toDomain(): User = User(
    uid = uid,
    displayName = displayName,
    email = email,
    phoneNumber = phoneNumber,
    phoneVerified = phoneVerified,
    photoUrl = photoUrl,
    notificationPrefs = notificationPrefs.toDomain(),
    createdAtEpochMs = createdAt?.toDate()?.time ?: 0L,
)

fun User.toDto(): UserDto = UserDto(
    uid = uid,
    displayName = displayName,
    email = email,
    phoneNumber = phoneNumber,
    phoneVerified = phoneVerified,
    photoUrl = photoUrl,
    notificationPrefs = notificationPrefs.toDto(),
    createdAt = com.google.firebase.Timestamp(createdAtEpochMs / 1000, 0),
)

fun NotificationPrefsDto.toDomain(): NotificationPrefs = NotificationPrefs(
    lostPetsNearby = lostPetsNearby,
    foundPetsNearby = foundPetsNearby,
    sightingsOnMyReports = sightingsOnMyReports,
)

fun NotificationPrefs.toDto(): NotificationPrefsDto = NotificationPrefsDto(
    lostPetsNearby = lostPetsNearby,
    foundPetsNearby = foundPetsNearby,
    sightingsOnMyReports = sightingsOnMyReports,
)
