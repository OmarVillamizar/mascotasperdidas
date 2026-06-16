package com.mascotasperdidas.app.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.util.launchDialer
import com.mascotasperdidas.app.app.util.launchWhatsApp

/**
 * Modal chooser to contact a report owner via WhatsApp or phone call.
 * Caller must guarantee [phone] is non-blank before showing.
 */
@Composable
fun ContactOptionsDialog(
    phone: String,
    contactName: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val connectingMsg = stringResource(R.string.contact_connecting_whatsapp)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.contact_title, contactName)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        Toast.makeText(context, connectingMsg, Toast.LENGTH_SHORT).show()
                        launchWhatsApp(context, phone)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = stringResource(R.string.contact_whatsapp),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                FilledTonalButton(
                    onClick = {
                        launchDialer(context, phone)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.contact_call),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar))
            }
        },
    )
}
