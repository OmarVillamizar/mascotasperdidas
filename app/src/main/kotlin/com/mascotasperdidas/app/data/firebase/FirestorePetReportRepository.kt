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

    // Seeds demo data once; checks by sentinel petName so user reports don't block seeding.
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
                petName = "Max",
                type = "LOST",
                breed = "Golden Retriever",
                description = "Se escapó de casa el lunes por la tarde. Lleva collar rojo con placa.",
                location = "Parque Simón Bolívar, Bogotá",
                imageUrl = "https://placedog.net/400/300?id=1",
                recencyLabel = "RECIENTE",
                createdAt = Timestamp(now.seconds - 86400, 0),
            ),
            PetReportDto(
                ownerUid = ownerUid,
                ownerInitial = "L",
                petName = "Luna",
                type = "FOUND",
                breed = "Siamés",
                description = "Encontrada cerca del centro comercial. Collar azul sin placa.",
                location = "Centro Comercial Andino, Bogotá",
                imageUrl = "https://picsum.photos/seed/luna-cat/400/300",
                recencyLabel = "RECIENTE",
                createdAt = Timestamp(now.seconds - 7200, 0),
            ),
            PetReportDto(
                ownerUid = ownerUid,
                ownerInitial = "R",
                petName = "Rocky",
                type = "LOST",
                breed = "Bulldog Francés",
                description = "Salió corriendo por la puerta del jardín. Color gris atigrado, arnés azul.",
                location = "Zona Rosa, Bogotá",
                imageUrl = "https://placedog.net/400/300?id=3",
                recencyLabel = "RECIENTE",
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

    override suspend fun createReport(report: PetReport, imageBytes: ByteArray?) {
        val uid = currentUid ?: return

        var imageUrl = report.imageUrl
        if (imageBytes != null && imageBytes.isNotEmpty()) {
            val storageRef = storage.reference
                .child("pet_reports/$uid/${UUID.randomUUID()}.jpg")
            storageRef.putBytes(imageBytes).await()
            imageUrl = storageRef.downloadUrl.await().toString()
        }

        val docRef = firestore.collection("pet_reports").document()
        val dto = report.toDto().copy(imageUrl = imageUrl)
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
