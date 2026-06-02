package com.mascotasperdidas.app.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mascotasperdidas.app.R
import com.mascotasperdidas.app.app.theme.MascotasPerdidasTheme

@Composable
fun MapBottomBar(
    onAdd: () -> Unit,
    onFilter: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        shape = RoundedCornerShape(32.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.nuevo_reporte),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            IconButton(onClick = onFilter) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = stringResource(R.string.filtrar),
                )
            }
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.feed_search_hint),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MapBottomBarPreview() {
    MascotasPerdidasTheme {
        MapBottomBar(onAdd = {}, onFilter = {}, onSearch = {})
    }
}
