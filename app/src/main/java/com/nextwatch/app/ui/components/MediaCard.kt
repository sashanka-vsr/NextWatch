package com.nextwatch.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nextwatch.app.data.MediaItem
import com.nextwatch.app.ui.theme.DarkSurface
import com.nextwatch.app.ui.theme.DarkSurfaceVariant
import com.nextwatch.app.ui.theme.NetflixRed
import com.nextwatch.app.ui.theme.StarGold
import com.nextwatch.app.ui.theme.WatchingGreen
import com.nextwatch.app.ui.viewmodel.NextWatchViewModel
import java.io.File
import java.util.Locale

fun savedPosterModel(item: MediaItem): Any? {
    val localFile = item.posterLocalPath
        ?.takeIf { it.isNotBlank() }
        ?.let(::File)
        ?.takeIf { it.exists() && it.length() > 0L }

    if (localFile != null) {
        return localFile
    }

    return item.posterUrl?.takeIf { it.isNotBlank() }
}

@Composable
fun SavedMediaCard(
    item: MediaItem,
    viewModel: NextWatchViewModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val genres by viewModel.observeGenres(item.id).collectAsState(initial = emptyList())
    val isWatching = item.status == MediaItem.STATUS_WATCHING
    val isCurrentlyWatchingSeries =
        isWatching && item.type == MediaItem.TYPE_SERIES
    val isRewatching =
        (isWatching && item.type == MediaItem.TYPE_MOVIE) ||
        (item.status == MediaItem.STATUS_REWATCH)
    val statusAccent = when {
        isCurrentlyWatchingSeries -> WatchingGreen
        isRewatching -> NetflixRed
        else -> null
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = DarkSurface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (statusAccent != null) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(statusAccent)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Compact Poster
            val posterModel = savedPosterModel(item)
            if (posterModel != null) {
                AsyncImage(
                    model = posterModel,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 48.dp, height = 72.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 48.dp, height = 72.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No\nPoster",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp,
                        lineHeight = 11.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Metadata Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                // Line 1: Title
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Line 2: Year · Movie/Series
                val year = item.releaseDate?.takeIf { it.length >= 4 }?.take(4)
                val line2 = buildString {
                    if (!year.isNullOrBlank()) {
                        append(year)
                    }
                    if (item.type.isNotBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append(item.type)
                    }
                    if (isCurrentlyWatchingSeries) {
                        if (isNotEmpty()) append(" · ")
                        append("Watching")
                    } else if (isRewatching) {
                        if (isNotEmpty()) append(" · ")
                        append("Re-Watch")
                    }
                }

                if (line2.isNotBlank()) {
                    Text(
                        text = line2,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusAccent ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Line 3: Genres · IMDb rating
                val genresText = genres.take(3).joinToString(" • ").takeIf { it.isNotBlank() }
                val ratingText = item.imdbRating?.let { String.format(Locale.US, "%.1f", it) }

                if (genresText != null || ratingText != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (genresText != null) {
                            Text(
                                text = genresText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                        } else {
                            Spacer(modifier = Modifier.width(0.dp))
                        }

                        if (ratingText != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 6.dp),
                            ) {
                                Text(
                                    text = "★ ",
                                    color = StarGold,
                                    fontSize = 11.sp,
                                )
                                Text(
                                    text = ratingText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }

            trailingContent?.invoke()
        }
    }
}
