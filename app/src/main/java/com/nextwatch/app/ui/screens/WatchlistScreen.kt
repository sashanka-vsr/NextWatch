package com.nextwatch.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextwatch.app.data.MediaItem
import com.nextwatch.app.ui.viewmodel.NextWatchViewModel

private enum class WatchlistTab {
    Movies,
    Series,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: NextWatchViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {},
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(WatchlistTab.Movies.ordinal) }
    val movies by viewModel.watchlistMovies.collectAsState()
    val series by viewModel.watchlistSeries.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Watchlist") },
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
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SecondaryTabRow(selectedTabIndex = selectedTab) {
                WatchlistTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab.ordinal,
                        onClick = { selectedTab = tab.ordinal },
                        text = { Text(tab.name) },
                    )
                }
            }
            when (WatchlistTab.entries[selectedTab]) {
                WatchlistTab.Movies -> MoviesWatchlist(movies = movies)
                WatchlistTab.Series -> SeriesWatchlist(series = series)
            }
        }
    }
}

private val listContentPadding = PaddingValues(bottom = 88.dp)

@Composable
private fun MoviesWatchlist(movies: List<MediaItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = listContentPadding,
    ) {
        items(movies, key = { it.id }) { item ->
            TitleRow(item)
        }
    }
}

@Composable
private fun SeriesWatchlist(series: List<MediaItem>) {
    val watching = series.filter { it.status == MediaItem.STATUS_WATCHING }
    val queued = series.filter { it.status == MediaItem.STATUS_WATCHLIST }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = listContentPadding,
    ) {
        stickyHeader(key = "currently_watching_header") {
            SectionHeader(text = "Currently Watching")
        }
        items(watching, key = { "watching_${it.id}" }) { item ->
            TitleRow(item)
        }
        item(key = "watchlist_header") {
            SectionHeader(text = "Watchlist")
        }
        items(queued, key = { "watchlist_${it.id}" }) { item ->
            TitleRow(item)
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun TitleRow(item: MediaItem) {
    Column {
        ListItem(
            headlineContent = { Text(item.title) },
            supportingContent = {
                Text(item.releaseYear.orEmpty().ifBlank { item.status })
            },
        )
        HorizontalDivider()
    }
}
