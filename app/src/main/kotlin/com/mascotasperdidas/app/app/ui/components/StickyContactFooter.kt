package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme

@Composable
fun StickyContactFooter(
    onWhatsApp: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Button(
                onClick = onWhatsApp,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_whatsapp),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.contact_whatsapp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            FilledTonalButton(
                onClick = onCall,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.contact_call),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StickyContactFooterPreview() {
    MascotasPerdidasTheme {
        StickyContactFooter(onWhatsApp = {}, onCall = {})
    }
}
