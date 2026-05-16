package com.mascotasperdidas.app.app.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.domain.model.ReportType

@Composable
fun StatusChip(
    reportType: ReportType,
    modifier: Modifier = Modifier,
) {
    val (labelRes, containerColor, labelColor) = when (reportType) {
        ReportType.LOST -> Triple(
            R.string.chip_lost,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError,
        )
        ReportType.FOUND -> Triple(
            R.string.chip_found,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiary,
        )
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelMedium,
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = labelColor,
        ),
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun StatusChipLostPreview() {
    MascotasPerdidasTheme {
        StatusChip(reportType = ReportType.LOST)
    }
}

@Preview(showBackground = true)
@Composable
private fun StatusChipFoundPreview() {
    MascotasPerdidasTheme {
        StatusChip(reportType = ReportType.FOUND)
    }
}
