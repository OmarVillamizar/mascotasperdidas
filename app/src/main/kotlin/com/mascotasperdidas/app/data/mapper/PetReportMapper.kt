package com.mascotasperdidas.app.data.mapper

import com.google.firebase.Timestamp
import com.mascotasperdidas.app.data.dto.PetReportDto
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType

fun PetReportDto.toDomain(documentId: String): PetReport = PetReport(
    id = documentId,
    ownerUid = ownerUid,
    ownerInitial = ownerInitial,
    ownerName = ownerName,
    petName = petName,
    type = when (type) {
        "LOST" -> ReportType.LOST
        "FOUND_SIGHTING" -> ReportType.FOUND_SIGHTING
        "FOUND_IN_CARE" -> ReportType.FOUND_IN_CARE
        else -> ReportType.FOUND_SIGHTING // legacy "FOUND" docs map to FOUND_SIGHTING
    },
    breed = breed,
    species = species,
    size = size,
    gender = gender,
    color = color,
    collarColor = collarColor,
    ageApprox = ageApprox,
    microchip = microchip,
    hasCollarPlate = hasCollarPlate,
    hasMicrochip = hasMicrochip,
    physicalStatus = physicalStatus,
    behaviors = behaviors,
    notes = notes,
    description = description,
    location = location,
    imageUrl = imageUrl,
    additionalPhotos = additionalPhotos,
    urgency = urgency,
    stillInArea = stillInArea,
    latitude = latitude,
    longitude = longitude,
    statuses = statuses,
    recencyLabel = recencyLabel,
    createdAtEpochMs = createdAt?.toDate()?.time ?: 0L,
)

fun PetReport.toDto(): PetReportDto = PetReportDto(
    ownerUid = ownerUid,
    ownerInitial = ownerInitial,
    ownerName = ownerName,
    petName = petName,
    type = type.name,
    breed = breed,
    species = species,
    size = size,
    gender = gender,
    color = color,
    collarColor = collarColor,
    ageApprox = ageApprox,
    microchip = microchip,
    hasCollarPlate = hasCollarPlate,
    hasMicrochip = hasMicrochip,
    physicalStatus = physicalStatus,
    behaviors = behaviors,
    notes = notes,
    description = description,
    location = location,
    imageUrl = imageUrl,
    additionalPhotos = additionalPhotos,
    urgency = urgency,
    stillInArea = stillInArea,
    latitude = latitude,
    longitude = longitude,
    statuses = statuses,
    recencyLabel = recencyLabel,
    createdAt = Timestamp(createdAtEpochMs / 1000, 0),
)
