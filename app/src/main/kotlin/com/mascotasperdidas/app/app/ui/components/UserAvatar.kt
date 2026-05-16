package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme

@Composable
fun UserAvatar(
    initial: String,
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val firstChar = initial.firstOrNull()?.uppercase() ?: "?"

    if (!photoUrl.isNullOrBlank() && !LocalInspectionMode.current) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape),
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = firstChar,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserAvatarInitialPreview() {
    MascotasPerdidasTheme {
        UserAvatar(initial = "M", photoUrl = null, size = 48.dp)
    }
}

@Preview(showBackground = true)
@Composable
private fun UserAvatarPhotoPreview() {
    MascotasPerdidasTheme {
        UserAvatar(initial = "A", photoUrl = "https://placekitten.com/100/100", size = 48.dp)
    }
}
