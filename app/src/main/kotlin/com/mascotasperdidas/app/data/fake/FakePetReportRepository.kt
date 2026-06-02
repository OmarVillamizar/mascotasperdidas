package com.mascotasperdidas.app.data.fake

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.port.out.PetReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePetReportRepository @Inject constructor() : PetReportRepository {

    private val now = System.currentTimeMillis()

    private val seed = mutableListOf(
        PetReport(
            id = "report-001",
            ownerUid = "fake-uid-001",
            ownerInitial = "M",
            ownerName = "María García",
            petName = "Max",
            type = ReportType.LOST,
            breed = "Golden Retriever",
            species = "Perro",
            gender = "Macho",
            color = "Dorado",
            description = "Max se escapó de casa el lunes por la tarde. Lleva collar " +
                "rojo con placa de identificación. Es muy amigable pero puede estar asustado.",
            location = "Parque Simón Bolívar, Cúcuta",
            imageUrl = "https://placedog.net/400/300?id=1",
            recencyLabel = "RECIENTE",
            latitude = 7.8939,
            longitude = -72.5078,
            createdAtEpochMs = now - 24 * 60 * 60 * 1000L,
        ),
        PetReport(
            id = "report-002",
            ownerUid = "fake-uid-002",
            ownerInitial = "L",
            ownerName = "Luis Morales",
            petName = "Luna",
            type = ReportType.FOUND_SIGHTING,
            breed = "Siamés",
            species = "Gato",
            gender = "Hembra",
            color = "Crema",
            description = "Encontrada cerca del centro comercial. Parece tener dueño, " +
                "lleva collar azul sin placa. Es muy cariñosa y se deja cargar.",
            location = "Centro Comercial Ventura Plaza, Cúcuta",
            imageUrl = "https://picsum.photos/seed/luna-cat/400/300",
            recencyLabel = "RECIENTE",
            latitude = 7.8820,
            longitude = -72.4960,
            createdAtEpochMs = now - 2 * 60 * 60 * 1000L,
        ),
        PetReport(
            id = "report-003",
            ownerUid = "fake-uid-003",
            ownerInitial = "R",
            ownerName = "Rosa Quintero",
            petName = "Rocky",
            type = ReportType.FOUND_IN_CARE,
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
            createdAtEpochMs = now - 6 * 60 * 60 * 1000L,
        ),
    )

    private val _reports = MutableStateFlow(seed.toList())

    override fun observeReports(type: ReportType): Flow<List<PetReport>> =
        _reports.map { list -> list.filter { it.type == type } }

    override suspend fun deleteReport(id: String) {
        _reports.value = _reports.value.filter { it.id != id }
    }

    override suspend fun createReport(report: PetReport, imageBytesList: List<ByteArray>) {
        val newReport = report.copy(
            id = "report-${UUID.randomUUID()}",
            createdAtEpochMs = System.currentTimeMillis(),
        )
        _reports.value = _reports.value + newReport
    }

    override suspend fun searchReports(query: String, type: ReportType): List<PetReport> {
        val q = query.trim().lowercase()
        return _reports.value.filter { report ->
            report.type == type && (
                report.petName.lowercase().contains(q) ||
                    report.breed.lowercase().contains(q) ||
                    report.description.lowercase().contains(q)
                )
        }
    }
}
