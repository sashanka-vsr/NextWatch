package com.nextwatch.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextwatch.app.data.MediaItem
import com.nextwatch.app.ui.components.SavedMediaCard
import com.nextwatch.app.ui.theme.DarkBorder
import com.nextwatch.app.ui.theme.DarkSurfaceVariant
import com.nextwatch.app.ui.theme.NetflixRed
import com.nextwatch.app.ui.theme.PureBlack
import com.nextwatch.app.ui.theme.WatchingGreen
import com.nextwatch.app.ui.viewmodel.NextWatchViewModel

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

private enum class WatchlistTab {
    Movies,
    Series,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    viewModel: NextWatchViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onItemClick: (MediaItem) -> Unit = {},
) {
    val pagerState = rememberPagerState(initialPage = 0) { WatchlistTab.entries.size }
    val coroutineScope = rememberCoroutineScope()
    val movies by viewModel.watchlistMovies.collectAsState()
    val series by viewModel.watchlistSeries.collectAsState()

    var activeActionItem by remember { mutableStateOf<MediaItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BackHandler(onBack = onBackClick)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Watchlist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add title",
                            tint = NetflixRed,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = PureBlack,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                        color = NetflixRed,
                    )
                },
            ) {
                WatchlistTab.entries.forEach { tab ->
                    val count = if (tab == WatchlistTab.Movies) movies.size else series.size
                    Tab(
                        selected = pagerState.currentPage == tab.ordinal,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(tab.ordinal)
                            }
                        },
                        text = {
                            Text(
                                text = "${tab.name} ($count)",
                                fontWeight = if (pagerState.currentPage == tab.ordinal) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        },
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (WatchlistTab.entries[page]) {
                WatchlistTab.Movies -> {
                    if (movies.isEmpty()) {
                        EmptyWatchlistState(
                            message = "No movies in your watchlist",
                            actionLabel = "Search Movies",
                            onActionClick = onAddClick,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(movies, key = { it.id }) { item ->
                                SavedMediaCard(
                                    item = item,
                                    viewModel = viewModel,
                                    onClick = { onItemClick(item) },
                                    trailingContent = {
                                        IconButton(
                                            onClick = { activeActionItem = item },
                                            modifier = Modifier.size(36.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = "Options",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                WatchlistTab.Series -> {
                    if (series.isEmpty()) {
                        EmptyWatchlistState(
                            message = "No series in your watchlist",
                            actionLabel = "Search Series",
                            onActionClick = onAddClick,
                        )
                    } else {
                        val watching = series.filter { it.status == MediaItem.STATUS_WATCHING }
                        val queued = series.filter { it.status != MediaItem.STATUS_WATCHING }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (watching.isNotEmpty()) {
                                item(key = "watching_header") {
                                    SectionHeader(
                                        text = "CURRENTLY WATCHING",
                                        isAccent = true,
                                    )
                                }
                                items(watching, key = { "watching_${it.id}" }) { item ->
                                    SavedMediaCard(
                                        item = item,
                                        viewModel = viewModel,
                                        onClick = { onItemClick(item) },
                                        trailingContent = {
                                            IconButton(
                                                onClick = { activeActionItem = item },
                                                modifier = Modifier.size(36.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Options",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }
                                        },
                                    )
                                }
                            }

                            if (queued.isNotEmpty()) {
                                item(key = "queued_header") {
                                    SectionHeader(
                                        text = if (watching.isNotEmpty()) "PLAN TO WATCH" else "WATCHLIST",
                                        isAccent = false,
                                    )
                                }
                                items(queued, key = { "queued_${it.id}" }) { item ->
                                    SavedMediaCard(
                                        item = item,
                                        viewModel = viewModel,
                                        onClick = { onItemClick(item) },
                                        trailingContent = {
                                            IconButton(
                                                onClick = { activeActionItem = item },
                                                modifier = Modifier.size(36.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "Options",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    // Material 3 Bottom Sheet for 3-dot Action Menu
    if (activeActionItem != null) {
        val selectedItem = activeActionItem!!

        ModalBottomSheet(
            onDismissRequest = { activeActionItem = null },
            sheetState = sheetState,
            containerColor = DarkSurfaceVariant,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = null,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                // Item title preview
                Text(
                    text = selectedItem.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DarkBorder)
                Spacer(modifier = Modifier.height(8.dp))

                if (selectedItem.type == MediaItem.TYPE_SERIES) {
                    // Option 1: Currently Watching (Series only)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                coroutineScope.launch {
                                    viewModel.markAsWatching(selectedItem)
                                    sheetState.hide()
                                    activeActionItem = null
                                }
                            }
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = WatchingGreen,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Continue Watching",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                // Option: Mark as Watched
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            coroutineScope.launch {
                                viewModel.moveToHistory(selectedItem)
                                sheetState.hide()
                                activeActionItem = null
                            }
                        }
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = NetflixRed,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Mark as Watched",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Option: Remove
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            coroutineScope.launch {
                                viewModel.deleteMedia(selectedItem)
                                sheetState.hide()
                                activeActionItem = null
                            }
                        }
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Remove",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Option: Cancel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            coroutineScope.launch {
                                sheetState.hide()
                                activeActionItem = null
                            }
                        }
                        .padding(vertical = 14.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    isAccent: Boolean,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        color = if (isAccent) WatchingGreen else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun EmptyWatchlistState(
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NetflixRed,
                    contentColor = PureBlack,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = actionLabel,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}