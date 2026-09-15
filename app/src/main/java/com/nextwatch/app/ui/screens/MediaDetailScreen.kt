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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale
import coil.compose.AsyncImage
import com.nextwatch.app.data.MediaItem
import com.nextwatch.app.network.NextWatchApiClient
import com.nextwatch.app.network.OmdbDetails
import com.nextwatch.app.network.TmdbDetails
import com.nextwatch.app.network.TmdbMovie
import com.nextwatch.app.ui.theme.DarkSurface
import com.nextwatch.app.ui.theme.NetflixRed
import com.nextwatch.app.ui.theme.PureBlack
import com.nextwatch.app.ui.theme.StarGold
import com.nextwatch.app.ui.viewmodel.NextWatchViewModel

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
        containerColor = PureBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
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
                        .size(width = 160.dp, height = 240.dp)
                        .clip(RoundedCornerShape(8.dp)),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = tmdbDetails?.title ?: media.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.height(6.dp))

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
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    TypeBadge(text = media.mediaType)
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (loadingDetails) {
                    CircularProgressIndicator(
                        color = NetflixRed,
                        modifier = Modifier.size(28.dp),
                    )
                } else {
                    DetailsRow(
                        details = tmdbDetails,
                        mediaType = media.mediaType,
                        imdbRating = omdbDetails?.imdbRating,
                    )

                    val genres = tmdbDetails?.genres.orEmpty()
                    if (genres.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = genres.joinToString(" • "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    val overview = tmdbDetails?.overview
                    if (!overview.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Overview",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = overview,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 22.sp,
                            )
                        }
                    }

                    SearchAdditionalInfo(
                        details = tmdbDetails,
                        omdbDetails = omdbDetails,
                        mediaType = media.mediaType,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Streamlined Adding Media: Single primary "Add to Watchlist" action
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
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NetflixRed,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = if (saving) "Adding..." else "Add to Watchlist",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
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
    val stats = buildList {
        if (mediaType == MediaItem.TYPE_SERIES) {
            details?.seasonCount?.let { add("Seasons" to it.toString()) }
            details?.episodeCount?.let { add("Episodes" to it.toString()) }
        } else {
            details?.runtimeMinutes?.let { add("Runtime" to "$it min") }
        }
        imdbRating?.let { add("IMDb" to String.format(Locale.US, "%.1f", it)) }
    }

    if (stats.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        for ((label, value) in stats) {
            RatingStat(
                label = label,
                value = if (label == "IMDb") "★ $value" else value,
                modifier = Modifier.weight(1f),
            )
        }
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
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (label == "IMDb") StarGold else MaterialTheme.colorScheme.onSurface,
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
        shape = RoundedCornerShape(4.dp),
        color = DarkSurface,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = 6.dp,
                vertical = 2.dp,
            ),
        )
    }
}

@Composable
private fun SearchAdditionalInfo(
    details: TmdbDetails?,
    omdbDetails: OmdbDetails?,
    mediaType: String,
) {
    val infoList = buildList {
        if (mediaType == MediaItem.TYPE_SERIES) {
            details?.creator?.takeIf { it.isNotBlank() }?.let { add("Creator" to it) }
            details?.releaseDate?.takeIf { it.isNotBlank() }
                ?.let { add("First Aired" to formatSearchReleaseDate(it)) }
        } else {
            val director = details?.director ?: omdbDetails?.director
            director?.takeIf { it.isNotBlank() }?.let { add("Director" to it) }
            details?.releaseDate?.takeIf { it.isNotBlank() }
                ?.let { add("Release Date" to formatSearchReleaseDate(it)) }
        }

        details?.originalLanguage?.takeIf { it.isNotBlank() }?.let { add("Language" to it) }
        val country = details?.country ?: omdbDetails?.country
        country?.takeIf { it.isNotBlank() }?.let { add("Country" to it) }
    }

    if (infoList.isEmpty()) return

    Spacer(modifier = Modifier.height(20.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
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

private fun formatSearchReleaseDate(rawDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val date = parser.parse(rawDate)
        if (date != null) formatter.format(date) else rawDate
    } catch (_: Exception) {
        rawDate
    }
}