package com.nextwatch.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nextwatch.app.data.MediaItem
import com.nextwatch.app.network.NextWatchApiClient
import com.nextwatch.app.network.TmdbDetails
import com.nextwatch.app.network.TmdbMovie
import com.nextwatch.app.ui.viewmodel.NextWatchViewModel
import com.nextwatch.app.network.OmdbDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    media: TmdbMovie,
    viewModel: NextWatchViewModel,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val apiClient = remember { NextWatchApiClient() }

    var tmdbDetails by remember { mutableStateOf<TmdbDetails?>(null) }
    var omdbDetails by remember { mutableStateOf<OmdbDetails?>(null) }
    var loadingDetails by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(media.tmdbId, media.mediaType) {
        loadingDetails = true

        tmdbDetails = runCatching {
            apiClient.fetchTmdbDetails(
                tmdbId = media.tmdbId,
                mediaType = media.mediaType,
            )
        }.getOrNull()
        
        omdbDetails = tmdbDetails?.imdbId?.let { imdbId ->
            runCatching {
                apiClient.fetchOmdbDetails(imdbId)
            }.getOrNull()
        }
        
        loadingDetails = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(media.title) },
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
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
        ) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                AsyncImage(
                    model = tmdbDetails?.posterUrl ?: media.posterUrl,
                    contentDescription = media.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 180.dp, height = 270.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = tmdbDetails?.title ?: media.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val releaseDate = tmdbDetails?.releaseDate
                    val year = releaseDate
                        ?.takeIf { it.length >= 4 }
                        ?.take(4)
                        ?: media.year

                    if (!year.isNullOrBlank()) {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    TypeBadge(text = media.mediaType)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (loadingDetails) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                    )
                } else {
                    DetailsRow(
                        details = tmdbDetails,
                        mediaType = media.mediaType,
                        imdbRating = omdbDetails?.imdbRating,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (saving) return@Button

                    saving = true

                    viewModel.addMediaFromSearchResult(
                        media = media,
                        status = MediaItem.STATUS_WATCHLIST,
                    ) {
                        saving = false
                        onSaved()
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add to Watchlist")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    if (saving) return@OutlinedButton

                    saving = true

                    viewModel.addMediaFromSearchResult(
                        media = media,
                        status = MediaItem.STATUS_WATCHED,
                    ) {
                        saving = false
                        onSaved()
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Mark as Watched")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailsRow(
    details: TmdbDetails?,
    mediaType: String,
    imdbRating: Double?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RatingStat(
            label = if (mediaType == MediaItem.TYPE_SERIES) "Seasons" else "Runtime",
            value = if (mediaType == MediaItem.TYPE_SERIES) {
                details?.seasonCount?.toString() ?: "—"
            } else {
                details?.runtimeMinutes?.let { "$it min" } ?: "—"
            },
            modifier = Modifier.weight(1f),
        )

        RatingStat(
            label = "IMDb",
            value = imdbRating?.let { String.format("%.1f", it) } ?: "—",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RatingStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 16.dp,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun TypeBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            ),
        )
    }
}