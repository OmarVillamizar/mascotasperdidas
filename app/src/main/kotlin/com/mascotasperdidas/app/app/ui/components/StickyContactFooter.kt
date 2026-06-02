package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme

@Composable
fun StickyContactFooter(
    onContact: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = onContact,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ),
        ) {
            Text(
                text = stringResource(R.string.contactar),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StickyContactFooterPreview() {
    MascotasPerdidasTheme {
        StickyContactFooter(onContact = {})
    }
}
