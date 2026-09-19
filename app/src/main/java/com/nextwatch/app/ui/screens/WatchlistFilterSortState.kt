package com.nextwatch.app.ui.screens

import com.nextwatch.app.data.MediaItem

enum class SortDirection {
    Ascending,
    Descending,
}

/**
 * All available sort criteria for the Watchlist.
 */
enum class WatchlistSortCriterion(val label: String) : java.io.Serializable {
    Added("Added"),
    Title("Title"),
    Year("Release year"),
    Rating("IMDb rating"),
    Runtime("Runtime"),
    Seasons("Seasons"),
    Episodes("Episodes"),
}

/**
 * Encapsulates the active sort criterion and direction.
 */
data class WatchlistSort(
    val criterion: WatchlistSortCriterion = WatchlistSortCriterion.Added,
    val direction: SortDirection = SortDirection.Descending,
) : java.io.Serializable

/** Movie-only sort criteria. */
val MovieSortCriteria = listOf(
    WatchlistSortCriterion.Added,
    WatchlistSortCriterion.Title,
    WatchlistSortCriterion.Year,
    WatchlistSortCriterion.Rating,
    WatchlistSortCriterion.Runtime,
)

/** Series sort criteria — includes season/episode options. */
val SeriesSortCriteria = listOf(
    WatchlistSortCriterion.Added,
    WatchlistSortCriterion.Title,
    WatchlistSortCriterion.Year,
    WatchlistSortCriterion.Rating,
    WatchlistSortCriterion.Seasons,
    WatchlistSortCriterion.Episodes,
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

/** Apply [WatchlistSort] to a list. Nulls sort last. */
fun List<MediaItem>.applySort(sort: WatchlistSort): List<MediaItem> {
    return when (sort.criterion) {
        WatchlistSortCriterion.Added -> when (sort.direction) {
            SortDirection.Descending -> sortedByDescending { it.dateAdded }
            SortDirection.Ascending -> sortedBy { it.dateAdded }
        }
        WatchlistSortCriterion.Title -> when (sort.direction) {
            SortDirection.Ascending -> sortedBy { it.title.lowercase() }
            SortDirection.Descending -> sortedByDescending { it.title.lowercase() }
        }
        WatchlistSortCriterion.Year -> when (sort.direction) {
            SortDirection.Descending -> sortedWith(
                compareByDescending { it.releaseDate?.takeIf { d -> d.length >= 4 }?.take(4)?.toIntOrNull() }
            )
            SortDirection.Ascending -> sortedWith(
                compareBy(nullsLast()) { it.releaseDate?.takeIf { d -> d.length >= 4 }?.take(4)?.toIntOrNull() }
            )
        }
        WatchlistSortCriterion.Rating -> when (sort.direction) {
            SortDirection.Descending -> sortedWith(
                compareByDescending { it.imdbRating }
            )
            SortDirection.Ascending -> sortedWith(
                compareBy(nullsLast()) { it.imdbRating }
            )
        }
        WatchlistSortCriterion.Runtime -> when (sort.direction) {
            SortDirection.Descending -> sortedWith(
                compareByDescending { it.runtimeMinutes }
            )
            SortDirection.Ascending -> sortedWith(
                compareBy(nullsLast()) { it.runtimeMinutes }
            )
        }
        WatchlistSortCriterion.Seasons -> when (sort.direction) {
            SortDirection.Descending -> sortedWith(
                compareByDescending { it.seasonCount }
            )
            SortDirection.Ascending -> sortedWith(
                compareBy(nullsLast()) { it.seasonCount }
            )
        }
        WatchlistSortCriterion.Episodes -> when (sort.direction) {
            SortDirection.Descending -> sortedWith(
                compareByDescending { it.episodeCount }
            )
            SortDirection.Ascending -> sortedWith(
                compareBy(nullsLast()) { it.episodeCount }
            )
        }
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
