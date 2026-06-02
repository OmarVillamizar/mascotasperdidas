package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme

@Composable
fun HeroImage(
    url: String,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp)),
    )
}

@Preview(showBackground = true)
@Composable
private fun HeroImagePreview() {
    MascotasPerdidasTheme {
        HeroImage(url = "https://placedog.net/400/300?id=1")
    }
}
