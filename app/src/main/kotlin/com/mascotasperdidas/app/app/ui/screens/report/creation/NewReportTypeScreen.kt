package com.mascotasperdidas.app.app.ui.screens.report.creation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme
import com.mascotasperdidas.app.app.ui.components.SelectionWizardStep
import com.mascotasperdidas.app.app.ui.components.WizardOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReportTypeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLostForm: () -> Unit,
    onNavigateToFoundSubType: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.nuevo_reporte)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp),
        ) {
            SelectionWizardStep(
                instruction = stringResource(R.string.selecciona_tipo),
                options = listOf(
                    WizardOption(
                        label = stringResource(R.string.mascota_perdida),
                        description = "Mi mascota se escapó o fue robada",
                        icon = Icons.Outlined.Pets,
                        onClick = onNavigateToLostForm,
                    ),
                    WizardOption(
                        label = stringResource(R.string.reportar_hallazgo),
                        description = "Vi o tengo un animal perdido",
                        icon = Icons.Outlined.Visibility,
                        onClick = onNavigateToFoundSubType,
                    ),
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NewReportTypeScreenPreview() {
    MascotasPerdidasTheme {
        NewReportTypeScreen(
            onNavigateBack = {},
            onNavigateToLostForm = {},
            onNavigateToFoundSubType = {},
        )
    }
}
