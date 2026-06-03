package com.mascotasperdidas.app.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mascotasperdidas.app.data.dto.PetReportDto
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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePetReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
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

        val uploadedUrls = imageBytesList.mapNotNull { bytes ->
            if (bytes.isEmpty()) return@mapNotNull null
            val ref = storage.reference.child("pet_reports/$uid/${UUID.randomUUID()}.jpg")
            ref.putBytes(bytes).await()
            ref.downloadUrl.await().toString()
        }

        val primaryUrl = uploadedUrls.firstOrNull() ?: report.imageUrl
        val extraUrls = if (uploadedUrls.size > 1) uploadedUrls.drop(1) else report.additionalPhotos

        val docRef = firestore.collection("pet_reports").document()
        val dto = report.toDto().copy(
            imageUrl = primaryUrl,
            additionalPhotos = extraUrls,
        )
        docRef.set(dto).await()
    }

    override suspend fun searchReports(query: String, type: ReportType): List<PetReport> {
        val snapshot = firestore.collection("pet_reports")
            .whereEqualTo("type", type.name)
            .get()
            .await()

        val q = query.trim().lowercase()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(PetReportDto::class.java)?.toDomain(doc.id)
        }.filter { report ->
            report.petName.lowercase().contains(q) ||
                report.breed.lowercase().contains(q) ||
                report.description.lowercase().contains(q)
        }
    }
}
