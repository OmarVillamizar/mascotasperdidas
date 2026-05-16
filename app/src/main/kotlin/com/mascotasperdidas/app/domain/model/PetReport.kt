package com.mascotasperdidas.app.domain.model

data class PetReport(
    val id: String,
    val ownerUid: String,
    val ownerInitial: String,
    val petName: String,
    val type: ReportType,
    val breed: String,
    val description: String,
    val location: String,
    val imageUrl: String,
    val recencyLabel: String,
    val createdAtEpochMs: Long,
)
