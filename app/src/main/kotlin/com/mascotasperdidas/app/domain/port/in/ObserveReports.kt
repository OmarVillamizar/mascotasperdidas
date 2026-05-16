package com.mascotasperdidas.app.domain.port.`in`

import com.mascotasperdidas.app.domain.model.PetReport
import com.mascotasperdidas.app.domain.model.ReportType
import kotlinx.coroutines.flow.Flow

fun interface ObserveReports {
    operator fun invoke(type: ReportType): Flow<List<PetReport>>
}
