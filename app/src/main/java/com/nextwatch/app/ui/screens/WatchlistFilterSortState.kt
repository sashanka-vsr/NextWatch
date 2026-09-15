package com.nextwatch.app.ui.screens

import com.nextwatch.app.data.MediaItem

/**
 * All available sort orders for the Watchlist.
 * [defaultForSeries] = true means this sort naturally respects the Currently Watching pinning
 * behaviour when [WatchlistFilterState.preserveWatchingSection] is true.
 */
enum class WatchlistSortOrder(val label: String) : java.io.Serializable {
    RecentlyAdded("Recently added"),
    TitleAZ("Title: A → Z"),
    TitleZA("Title: Z → A"),
    YearNewest("Release year: newest first"),
    YearOldest("Release year: oldest first"),
    RatingHighest("IMDb rating: highest first"),
    RatingLowest("IMDb rating: lowest first"),
    RuntimeShortest("Runtime: shortest first"),
    RuntimeLongest("Runtime: longest first"),
    SeasonsMore("Seasons: most first"),
    SeasonsLess("Seasons: fewest first"),
    EpisodesMore("Episodes: most first"),
    EpisodesLess("Episodes: fewest first"),
}

/** Movie-only sort orders. */
val MovieSortOrders = listOf(
    WatchlistSortOrder.RecentlyAdded,
    WatchlistSortOrder.TitleAZ,
    WatchlistSortOrder.TitleZA,
    WatchlistSortOrder.YearNewest,
    WatchlistSortOrder.YearOldest,
    WatchlistSortOrder.RatingHighest,
    WatchlistSortOrder.RatingLowest,
    WatchlistSortOrder.RuntimeShortest,
    WatchlistSortOrder.RuntimeLongest,
)

/** Series sort orders — includes season/episode options. */
val SeriesSortOrders = listOf(
    WatchlistSortOrder.RecentlyAdded,
    WatchlistSortOrder.TitleAZ,
    WatchlistSortOrder.TitleZA,
    WatchlistSortOrder.YearNewest,
    WatchlistSortOrder.YearOldest,
    WatchlistSortOrder.RatingHighest,
    WatchlistSortOrder.RatingLowest,
    WatchlistSortOrder.SeasonsMore,
    WatchlistSortOrder.SeasonsLess,
    WatchlistSortOrder.EpisodesMore,
    WatchlistSortOrder.EpisodesLess,
)

/**
 * Immutable snapshot of the user's active filter selections.
 * All fields are nullable/empty = no filter applied.
 * Multiple selections within a field are OR-combined; fields are AND-combined.
 */
data class WatchlistFilterState(
    val genres: Set<String> = emptySet(),
    val languages: Set<String> = emptySet(),
    val directors: Set<String> = emptySet(),         // movies
    val creators: Set<String> = emptySet(),          // series
    val yearMin: Int? = null,
    val yearMax: Int? = null,
    val ratingMin: Double? = null,
    val ratingMax: Double? = null,
) : java.io.Serializable {
    val isActive: Boolean
        get() = genres.isNotEmpty() ||
                languages.isNotEmpty() ||
                directors.isNotEmpty() ||
                creators.isNotEmpty() ||
                yearMin != null ||
                yearMax != null ||
                ratingMin != null ||
                ratingMax != null

    companion object {
        val Empty = WatchlistFilterState()
    }
}

/** Apply [WatchlistFilterState] to a list of items. [genreMap] is mediaId -> genres. */
fun List<MediaItem>.applyFilter(
    filter: WatchlistFilterState,
    genreMap: Map<Long, List<String>>,
): List<MediaItem> {
    if (!filter.isActive) return this
    return filter { item ->
        // Genre: match if any saved genre for this item is in the filter set
        val genreOk = filter.genres.isEmpty() ||
            genreMap[item.id].orEmpty().any { it in filter.genres }

        // Language
        val langOk = filter.languages.isEmpty() ||
            item.originalLanguage?.let { it in filter.languages } == true

        // Director (movies) / Creator (series)
        val creatorOk = if (item.type == MediaItem.TYPE_MOVIE) {
            filter.directors.isEmpty() ||
                item.director?.let { dir ->
                    filter.directors.any { f -> dir.contains(f, ignoreCase = true) }
                } == true
        } else {
            filter.creators.isEmpty() ||
                item.creator?.let { cr ->
                    filter.creators.any { f -> cr.contains(f, ignoreCase = true) }
                } == true
        }

        // Year
        val year = item.releaseDate?.takeIf { it.length >= 4 }?.take(4)?.toIntOrNull()
        val yearOk = (filter.yearMin == null || (year != null && year >= filter.yearMin)) &&
                     (filter.yearMax == null || (year != null && year <= filter.yearMax))

        // IMDb rating
        val ratingOk = (filter.ratingMin == null || (item.imdbRating != null && item.imdbRating >= filter.ratingMin)) &&
                       (filter.ratingMax == null || (item.imdbRating != null && item.imdbRating <= filter.ratingMax))

        genreOk && langOk && creatorOk && yearOk && ratingOk
    }
}

/** Apply [WatchlistSortOrder] to a list. Nulls sort last. */
fun List<MediaItem>.applySort(order: WatchlistSortOrder): List<MediaItem> {
    return when (order) {
        WatchlistSortOrder.RecentlyAdded -> sortedByDescending { it.dateAdded }
        WatchlistSortOrder.TitleAZ -> sortedBy { it.title.lowercase() }
        WatchlistSortOrder.TitleZA -> sortedByDescending { it.title.lowercase() }
        WatchlistSortOrder.YearNewest -> sortedWith(
            compareByDescending { it.releaseDate?.takeIf { d -> d.length >= 4 }?.take(4)?.toIntOrNull() }
        )
        WatchlistSortOrder.YearOldest -> sortedWith(
            compareBy(nullsLast()) { it.releaseDate?.takeIf { d -> d.length >= 4 }?.take(4)?.toIntOrNull() }
        )
        WatchlistSortOrder.RatingHighest -> sortedWith(
            compareByDescending { it.imdbRating }
        )
        WatchlistSortOrder.RatingLowest -> sortedWith(
            compareBy(nullsLast()) { it.imdbRating }
        )
        WatchlistSortOrder.RuntimeShortest -> sortedWith(
            compareBy(nullsLast()) { it.runtimeMinutes }
        )
        WatchlistSortOrder.RuntimeLongest -> sortedWith(
            compareByDescending { it.runtimeMinutes }
        )
        WatchlistSortOrder.SeasonsMore -> sortedWith(
            compareByDescending { it.seasonCount }
        )
        WatchlistSortOrder.SeasonsLess -> sortedWith(
            compareBy(nullsLast()) { it.seasonCount }
        )
        WatchlistSortOrder.EpisodesMore -> sortedWith(
            compareByDescending { it.episodeCount }
        )
        WatchlistSortOrder.EpisodesLess -> sortedWith(
            compareBy(nullsLast()) { it.episodeCount }
        )
    }
}

/**
 * Extract unique, non-blank values from a list of items for a given field.
 * Sorted alphabetically.
 */
fun List<MediaItem>.distinctLanguages(): List<String> =
    mapNotNull { it.originalLanguage?.takeIf { l -> l.isNotBlank() } }
        .distinct()
        .sorted()

fun List<MediaItem>.distinctDirectors(): List<String> =
    mapNotNull { it.director?.takeIf { d -> d.isNotBlank() } }
        .flatMap { it.split(",", ";") }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

fun List<MediaItem>.distinctCreators(): List<String> =
    mapNotNull { it.creator?.takeIf { c -> c.isNotBlank() } }
        .flatMap { it.split(",", ";") }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

fun List<MediaItem>.releaseYearRange(): Pair<Int, Int>? {
    val years = mapNotNull {
        it.releaseDate?.takeIf { d -> d.length >= 4 }?.take(4)?.toIntOrNull()
    }
    if (years.isEmpty()) return null
    return Pair(years.min(), years.max())
}
