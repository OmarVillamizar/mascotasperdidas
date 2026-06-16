package com.mascotasperdidas.app.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mascotasperdidas.app.data.dto.PetReportDto
import com.mascotasperdidas.app.data.image.Base64ImageCodec
import com.mascotasperdidas.app.data.image.ImageCompressor
import com.mascotasperdidas.app.data.image.ImageTooLargeException
import com.mascotasperdidas.app.data.mapper.toDomain
import com.mascotasperdidas.app.data.mapper.toDto
import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.port.out.PetReportRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePetReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val imageCompressor: ImageCompressor,
) : PetReportRepository {

    private val currentUid: String?
        get() = auth.currentUser?.uid

    override fun observeReports(type: ReportType): Flow<List<PetReport>> = callbackFlow {
        // No orderBy — compound whereEqualTo+orderBy requires a composite index.
        // Sort client-side instead.
        val query = firestore.collection("pet_reports")
            .whereEqualTo("type", type.name)

        var seeded = false

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val reports = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(PetReportDto::class.java)?.toDomain(doc.id)
            }?.sortedByDescending { it.createdAtEpochMs } ?: emptyList()
            trySend(reports)

            val uid = currentUid
            if (!seeded && reports.isEmpty() && uid != null) {
                seeded = true
                launch { seedIfEmpty(uid) }
            }
        }
        awaitClose { listener.remove() }
    }

    private suspend fun seedIfEmpty(ownerUid: String) {
        val alreadySeeded = firestore.collection("pet_reports")
            .whereEqualTo("petName", "Max")
            .limit(1)
            .get()
            .await()
            .isEmpty
            .not()
        if (alreadySeeded) return
        seedReports(ownerUid)
    }

    private suspend fun seedReports(ownerUid: String) {
        val now = Timestamp.now()
        val seeds = listOf(
            PetReportDto(
                ownerUid = ownerUid,
                ownerInitial = "M",
                ownerName = "María García",
                ownerPhone = "+573001112233",
                petName = "Max",
                type = "LOST",
                breed = "Golden Retriever",
                species = "Perro",
                gender = "Macho",
                color = "Dorado",
                description = "Se escapó de casa el lunes por la tarde. Lleva collar rojo con placa.",
                location = "Parque Simón Bolívar, Cúcuta",
                imageUrl = "https://placedog.net/400/300?id=1",
                recencyLabel = "RECIENTE",
                latitude = 7.8939,
                longitude = -72.5078,
                statuses = listOf("RECIENTE"),
                createdAt = Timestamp(now.seconds - 86400, 0),
            ),
            PetReportDto(
                ownerUid = ownerUid,
                ownerInitial = "L",
                ownerName = "Luis Morales",
                ownerPhone = "+573004445566",
                petName = "Luna",
                type = "FOUND_SIGHTING",
                breed = "Siamés",
                species = "Gato",
                gender = "Hembra",
                color = "Crema",
                description = "Encontrada cerca del centro comercial. Collar azul sin placa.",
                location = "Centro Comercial Ventura Plaza, Cúcuta",
                imageUrl = "https://picsum.photos/seed/luna-cat/400/300",
                recencyLabel = "RECIENTE",
                latitude = 7.8820,
                longitude = -72.4960,
                statuses = listOf("RECIENTE"),
                createdAt = Timestamp(now.seconds - 7200, 0),
            ),
            PetReportDto(
                ownerUid = ownerUid,
                ownerInitial = "R",
                ownerName = "Rosa Quintero",
                ownerPhone = "+573007778899",
                petName = "Rocky",
                type = "FOUND_IN_CARE",
                breed = "Bulldog Francés",
                species = "Perro",
                gender = "Macho",
                color = "Gris atigrado",
                description = "Lo encontré deambulando. Está bajo mi cuidado temporal hasta encontrar a su dueño.",
                location = "Zona Rosa, Cúcuta",
                imageUrl = "https://placedog.net/400/300?id=3",
                recencyLabel = "RECIENTE",
                urgency = "ALTA",
                latitude = 7.9015,
                longitude = -72.5120,
                statuses = listOf("RECIENTE"),
                createdAt = Timestamp(now.seconds - 21600, 0),
            ),
        )
        seeds.forEach { dto ->
            firestore.collection("pet_reports").add(dto).await()
        }
    }

    override suspend fun deleteReport(id: String) {
        firestore.collection("pet_reports").document(id).delete().await()
    }

    override suspend fun createReport(report: PetReport, imageBytesList: List<ByteArray>) {
        val uid = currentUid ?: throw IllegalStateException("Usuario no autenticado")

        val primaryBase64 = imageBytesList.firstOrNull { it.isNotEmpty() }?.let { raw ->
            val jpeg = imageCompressor.compressToJpeg(raw)
            val b64 = Base64ImageCodec.encode(jpeg)
            if (Base64ImageCodec.exceedsBudget(b64)) {
                throw ImageTooLargeException("La imagen es demasiado grande. Por favor, elegí una foto más pequeña.")
            }
            b64
        } ?: report.imageUrl
        // TODO(multi-photo): additional photos deferred — one image per doc for MVP

        val docRef = firestore.collection("pet_reports").document()
        val dto = report.toDto().copy(
            ownerUid = uid,
            // Contact phone (E.164) for WhatsApp / call. Prefer the user's stored
            // profile phone (report.ownerPhone); auth.currentUser.phoneNumber is
            // only populated once real OTP links the number (future feature).
            ownerPhone = report.ownerPhone.ifBlank { auth.currentUser?.phoneNumber ?: "" },
            imageUrl = primaryBase64,
            additionalPhotos = emptyList(),
        )
        docRef.set(dto).await()
    }

    override suspend fun searchReports(query: String, type: ReportType): List<PetReport> {
        // The "Avistadas" tab merges FOUND_SIGHTING + FOUND_IN_CARE in the live feed;
        // mirror that here so search does not silently exclude in-care reports.
        val types = if (type == ReportType.FOUND_SIGHTING) {
            listOf(ReportType.FOUND_SIGHTING.name, ReportType.FOUND_IN_CARE.name)
        } else {
            listOf(type.name)
        }
        val snapshot = firestore.collection("pet_reports")
            .whereIn("type", types)
            .get()
            .await()

        val q = query.trim().lowercase()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(PetReportDto::class.java)?.toDomain(doc.id)
        }.filter { r ->
            r.petName.lowercase().contains(q) ||
                r.breed.lowercase().contains(q) ||
                r.description.lowercase().contains(q)
        }
    }
}
