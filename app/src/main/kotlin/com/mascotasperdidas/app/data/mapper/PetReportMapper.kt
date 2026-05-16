package com.mascotasperdidas.app.data.mapper

import com.mascotasperdidas.app.data.dto.PetReportDto
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.google.firebase.Timestamp

fun PetReportDto.toDomain(documentId: String): PetReport = PetReport(
    id = documentId,
    ownerUid = ownerUid,
    ownerInitial = ownerInitial,
    petName = petName,
    type = if (type == "FOUND") ReportType.FOUND else ReportType.LOST,
    breed = breed,
    description = description,
    location = location,
    imageUrl = imageUrl,
    recencyLabel = recencyLabel,
    createdAtEpochMs = createdAt?.toDate()?.time ?: 0L,
)

fun PetReport.toDto(): PetReportDto = PetReportDto(
    ownerUid = ownerUid,
    ownerInitial = ownerInitial,
    petName = petName,
    type = type.name,
    breed = breed,
    description = description,
    location = location,
    imageUrl = imageUrl,
    recencyLabel = recencyLabel,
    createdAt = Timestamp(createdAtEpochMs / 1000, 0),
)
