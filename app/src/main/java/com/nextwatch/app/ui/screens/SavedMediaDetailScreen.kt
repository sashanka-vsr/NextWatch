package com.nextwatch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nextwatch.app.data.AppPreferences
import com.nextwatch.app.data.MediaItem
import com.nextwatch.app.network.RegionAvailability
import com.nextwatch.app.ui.components.savedPosterModel
import com.nextwatch.app.ui.theme.DarkBorder
import com.nextwatch.app.ui.theme.DarkSurface
import com.nextwatch.app.ui.theme.DarkSurfaceVariant
import com.nextwatch.app.ui.theme.NetflixRed
import com.nextwatch.app.ui.theme.PureBlack
import com.nextwatch.app.ui.theme.StarGold
import com.nextwatch.app.ui.viewmodel.NextWatchViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedMediaDetailScreen(
    mediaId: Long,
    viewModel: NextWatchViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }

    val media by viewModel
        .observeMedia(mediaId)
        .collectAsState(initial = null)

    val genres by viewModel
        .observeGenres(mediaId)
        .collectAsState(initial = emptyList())

    var watchProviders by remember { mutableStateOf<RegionAvailability?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = media?.title ?: "Details",
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
                    val currentItem = media
                    if (currentItem != null && currentItem.status != MediaItem.STATUS_WATCHED) {
                        IconButton(
                            onClick = {
                                viewModel.moveToHistory(currentItem)
                                onBackClick()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Mark as Watched",
                                tint = NetflixRed,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureBlack,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { innerPadding ->

        if (media == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = NetflixRed)
            }
            return@Scaffold
        }

        val item = media!!

        LaunchedEffect(item.tmdbId, item.type, preferences.getWatchRegion()) {
            if (item.tmdbId != null) {
                watchProviders = viewModel.getWatchProviders(
                    tmdbId = item.tmdbId,
                    mediaType = item.type,
                    region = preferences.getWatchRegion(),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SavedPoster(item = item)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val year = item.releaseDate
                    ?.takeIf { it.length >= 4 }
                    ?.take(4)

                if (!year.isNullOrBlank()) {
                    Text(
                        text = year,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                TypeBadge(text = item.type)
            }

            // 1. Main information / stat cards
            SavedStats(item = item)

            // 2. Genres
            if (genres.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = genres.joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // 3. Overview
            if (!item.overview.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp,
                    )
                }
            }

            // 4. Additional Information
            AdditionalInfoSection(item = item)

            // 5. Where to Watch
            WhereToWatchSection(providers = watchProviders)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SavedPoster(
    item: MediaItem,
) {
    val posterModel = savedPosterModel(item)

    if (posterModel != null) {
        AsyncImage(
            model = posterModel,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = 160.dp, height = 240.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
    } else {
        Box(
            modifier = Modifier
                .size(width = 160.dp, height = 240.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No Poster",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SavedStats(
    item: MediaItem,
) {
    val stats = buildList {
        if (item.type == MediaItem.TYPE_SERIES) {
            item.seasonCount?.let { count ->
                add("Seasons" to count.toString())
            }
            item.episodeCount?.let { count ->
                add("Episodes" to count.toString())
            }
        } else {
            item.runtimeMinutes?.let { minutes ->
                add("Runtime" to "$minutes min")
            }
        }

        item.imdbRating?.let { rating ->
            add("IMDb" to String.format(Locale.US, "%.1f", rating))
        }
    }

    if (stats.isNotEmpty()) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            for ((label, value) in stats) {
                DetailStat(
                    label = label,
                    value = value,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DetailStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = DarkSurface,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 12.dp,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (label == "IMDb") "★ $value" else value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (label == "IMDb") StarGold else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun AdditionalInfoSection(
    item: MediaItem,
) {
    val infoList = buildList {
        if (item.type == MediaItem.TYPE_SERIES) {
            item.creator?.takeIf { it.isNotBlank() }?.let { add("Creator" to it) }
            item.releaseDate?.takeIf { it.isNotBlank() }?.let { add("First Aired" to formatReleaseDate(it)) }
        } else {
            item.director?.takeIf { it.isNotBlank() }?.let { add("Director" to it) }
            item.releaseDate?.takeIf { it.isNotBlank() }?.let { add("Release Date" to formatReleaseDate(it)) }
        }

        item.originalLanguage?.takeIf { it.isNotBlank() }?.let { add("Language" to it) }
        item.country?.takeIf { it.isNotBlank() }?.let { add("Country" to it) }
    }

    if (infoList.isEmpty()) return

    Spacer(modifier = Modifier.height(20.dp))

    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "Additional Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = DarkSurface,
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                infoList.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(0.4f),
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(0.6f),
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
        }
    }
}

internal fun formatReleaseDate(rawDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val date = parser.parse(rawDate)
        if (date != null) formatter.format(date) else rawDate
    } catch (_: Exception) {
        rawDate
    }
}

@Composable
private fun WhereToWatchSection(
    providers: RegionAvailability?,
) {
    if (providers == null) return
    val flatrate = providers.flatrate
    val rentBuy = (providers.rent + providers.buy).distinctBy { it.providerId }

    if (flatrate.isEmpty() && rentBuy.isEmpty()) return

    Spacer(modifier = Modifier.height(20.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        if (flatrate.isNotEmpty()) {
            Text(
                text = "Where to Watch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(flatrate.size) { index ->
                    val provider = flatrate[index]
                    if (provider.logoPath != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurface,
                        ) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w92${provider.logoPath}",
                                contentDescription = provider.providerName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = "Where to Watch",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Available to rent or buy",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(rentBuy.size) { index ->
                    val provider = rentBuy[index]
                    if (provider.logoPath != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurface,
                        ) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w92${provider.logoPath}",
                                contentDescription = provider.providerName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Streaming data by JustWatch",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}