package com.mascotasperdidas.app.data.fake

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import com.mascotasperdidas.app.domain.port.out.PetReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePetReportRepository @Inject constructor() : PetReportRepository {

    private val now = System.currentTimeMillis()

    // ── Datos hardcodeados de los mockups ─────────────────────────
    private val seed = mutableListOf(
        PetReport(
            id = "report-001",
            ownerUid = "fake-uid-001",
            ownerInitial = "M",
            petName = "Max",
            type = ReportType.LOST,
            breed = "Golden Retriever",
            description = "Max se escapó de casa el lunes por la tarde. Lleva collar " +
                "rojo con placa de identificación. Es muy amigable pero puede estar asustado. " +
                "Responde a su nombre y le encantan los premios.",
            location = "Parque Simón Bolívar, Bogotá",
            imageUrl = "https://placedog.net/400/300?id=1",
            recencyLabel = "RECIENTE",
            createdAtEpochMs = now - 24 * 60 * 60 * 1000L, // hace 1 día
        ),
        PetReport(
            id = "report-002",
            ownerUid = "fake-uid-002",
            ownerInitial = "A",
            petName = "Luna",
            type = ReportType.FOUND,
            breed = "Siamés",
            description = "Encontrada cerca del centro comercial. Parece tener dueño, " +
                "lleva collar azul sin placa. Es muy cariñosa y se deja cargar. " +
                "Está bien alimentada, parece perdida hace poco.",
            location = "Centro Comercial Andino, Bogotá",
            imageUrl = "https://placekitten.com/400/300",
            recencyLabel = "RECIENTE",
            createdAtEpochMs = now - 2 * 60 * 60 * 1000L, // hace 2 horas
        ),
        PetReport(
            id = "report-003",
            ownerUid = "fake-uid-003",
            ownerInitial = "C",
            petName = "Rocky",
            type = ReportType.LOST,
            breed = "Bulldog Francés",
            description = "Rocky salió corriendo por la puerta del jardín esta mañana. " +
                "Es de color gris atigrado, lleva arnés azul. Es juguetón pero " +
                "desconfía de extraños. Tiene microchip.",
            location = "Zona Rosa, Bogotá",
            imageUrl = "https://placedog.net/400/300?id=3",
            recencyLabel = "RECIENTE",
            createdAtEpochMs = now - 6 * 60 * 60 * 1000L, // hace 6 horas
        ),
    )

    private val _reports = MutableStateFlow(seed.toList())

    override fun observeReports(type: ReportType): Flow<List<PetReport>> = _reports.asStateFlow()

    override suspend fun deleteReport(id: String) {
        _reports.value = _reports.value.filter { it.id != id }
    }

    override suspend fun createReport(report: PetReport, imageBytes: ByteArray?) {
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
