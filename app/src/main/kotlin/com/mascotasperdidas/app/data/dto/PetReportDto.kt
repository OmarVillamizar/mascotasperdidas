package com.mascotasperdidas.app.data.dto

import com.google.firebase.Timestamp

data class PetReportDto(
    val ownerUid: String = "",
    val ownerInitial: String = "",
    val petName: String = "",
    val type: String = "LOST",
    val breed: String = "",
    val description: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val recencyLabel: String = "",
    val createdAt: Timestamp? = null,
)
