package com.nextwatch.app.ui.screens

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.nextwatch.app.data.MediaItem
import com.nextwatch.app.ui.viewmodel.NextWatchViewModel
import coil.compose.AsyncImage

private enum class HistoryTab {
    Movies,
    Series,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchHistoryScreen(
    viewModel: NextWatchViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onItemClick: (MediaItem) -> Unit = {},
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(HistoryTab.Movies.ordinal) }
    val movies by viewModel.historyMovies.collectAsState()
    val series by viewModel.historySeries.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Watch History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SecondaryTabRow(selectedTabIndex = selectedTab) {
                HistoryTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab.ordinal,
                        onClick = { selectedTab = tab.ordinal },
                        text = { Text(tab.name) },
                    )
                }
            }
            val items = when (HistoryTab.entries[selectedTab]) {
                HistoryTab.Movies -> movies
                HistoryTab.Series -> series
            }
            HistoryList(
                items = items,
                onRewatchClick = viewModel::rewatchMedia,
                onItemClick = onItemClick,
            )
        }
    }
}

@Composable
private fun HistoryList(
    items: List<MediaItem>,
    onRewatchClick: (MediaItem) -> Unit,
    onItemClick: (MediaItem) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items, key = { it.id }) { item ->
            HistoryRow(
                item = item,
                onRewatchClick = { onRewatchClick(item) },
                onItemClick = { onItemClick(item) },
            )
        }
    }
}

@Composable
private fun HistoryRow(
    item: MediaItem,
    onRewatchClick: () -> Unit,
    onItemClick: () -> Unit,
) {
    Column {
        ListItem(
            modifier = Modifier.clickable(onClick = onItemClick),
            headlineContent = {
                Text(item.title)
            },
            supportingContent = {
                Text(item.releaseDate?.take(4).orEmpty())
            },
            leadingContent = {
                AsyncImage(
                    model = item.posterLocalPath ?: item.posterUrl,
                    contentDescription = item.title,
                    modifier = Modifier.size(56.dp),
                )
            },
            trailingContent = {
                IconButton(onClick = onRewatchClick) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Re-watch",
                        tint = MaterialTheme.colorScheme.tertiary,
                    )
                }
            },
        )
        HorizontalDivider()
    }
}