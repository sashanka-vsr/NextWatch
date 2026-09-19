package com.nextwatch.app

import com.nextwatch.app.data.MediaItem
import com.nextwatch.app.ui.screens.MovieSortCriteria
import com.nextwatch.app.ui.screens.SeriesSortCriteria
import com.nextwatch.app.ui.screens.SortDirection
import com.nextwatch.app.ui.screens.WatchlistSort
import com.nextwatch.app.ui.screens.WatchlistSortCriterion
import com.nextwatch.app.ui.screens.applySort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchlistSortTest {

    private fun createItem(
        id: Long,
        title: String,
        dateAdded: Long = 0L,
        releaseDate: String? = null,
        imdbRating: Double? = null,
        runtimeMinutes: Int? = null,
        seasonCount: Int? = null,
        episodeCount: Int? = null,
    ): MediaItem {
        return MediaItem(
            id = id,
            tmdbId = id.toInt(),
            title = title,
            type = MediaItem.TYPE_MOVIE,
            status = MediaItem.STATUS_WATCHLIST,
            dateAdded = dateAdded,
            releaseDate = releaseDate,
            imdbRating = imdbRating,
            runtimeMinutes = runtimeMinutes,
            seasonCount = seasonCount,
            episodeCount = episodeCount,
        )
    }

    @Test
    fun testCriteriaSeparation() {
        // Movies have Runtime, but not Seasons or Episodes
        assertTrue(MovieSortCriteria.contains(WatchlistSortCriterion.Runtime))
        assertFalse(MovieSortCriteria.contains(WatchlistSortCriterion.Seasons))
        assertFalse(MovieSortCriteria.contains(WatchlistSortCriterion.Episodes))

        // Series have Seasons and Episodes, but not Runtime
        assertTrue(SeriesSortCriteria.contains(WatchlistSortCriterion.Seasons))
        assertTrue(SeriesSortCriteria.contains(WatchlistSortCriterion.Episodes))
        assertFalse(SeriesSortCriteria.contains(WatchlistSortCriterion.Runtime))
    }

    @Test
    fun testAddedSort() {
        val item1 = createItem(1, "Oldest", dateAdded = 1000L)
        val item2 = createItem(2, "Newest", dateAdded = 2000L)
        val list = listOf(item1, item2)

        // Descending: newest added first (default)
        val desc = list.applySort(WatchlistSort(WatchlistSortCriterion.Added, SortDirection.Descending))
        assertEquals(listOf(item2, item1), desc)

        // Ascending: oldest added first
        val asc = list.applySort(WatchlistSort(WatchlistSortCriterion.Added, SortDirection.Ascending))
        assertEquals(listOf(item1, item2), asc)
    }

    @Test
    fun testTitleSort() {
        val itemA = createItem(1, "Alpha")
        val itemZ = createItem(2, "Zulu")
        val list = listOf(itemZ, itemA)

        // Ascending: A -> Z
        val asc = list.applySort(WatchlistSort(WatchlistSortCriterion.Title, SortDirection.Ascending))
        assertEquals(listOf(itemA, itemZ), asc)

        // Descending: Z -> A
        val desc = list.applySort(WatchlistSort(WatchlistSortCriterion.Title, SortDirection.Descending))
        assertEquals(listOf(itemZ, itemA), desc)
    }

    @Test
    fun testRatingSort() {
        val itemLow = createItem(1, "Low", imdbRating = 5.2)
        val itemHigh = createItem(2, "High", imdbRating = 8.9)
        val list = listOf(itemLow, itemHigh)

        // Descending: highest first
        val desc = list.applySort(WatchlistSort(WatchlistSortCriterion.Rating, SortDirection.Descending))
        assertEquals(listOf(itemHigh, itemLow), desc)

        // Ascending: lowest first
        val asc = list.applySort(WatchlistSort(WatchlistSortCriterion.Rating, SortDirection.Ascending))
        assertEquals(listOf(itemLow, itemHigh), asc)
    }

    @Test
    fun testReleaseYearSort() {
        val item1990 = createItem(1, "Old", releaseDate = "1990-05-10")
        val item2024 = createItem(2, "New", releaseDate = "2024-01-01")
        val list = listOf(item1990, item2024)

        // Descending: newest first
        val desc = list.applySort(WatchlistSort(WatchlistSortCriterion.Year, SortDirection.Descending))
        assertEquals(listOf(item2024, item1990), desc)

        // Ascending: oldest first
        val asc = list.applySort(WatchlistSort(WatchlistSortCriterion.Year, SortDirection.Ascending))
        assertEquals(listOf(item1990, item2024), asc)
    }

    @Test
    fun testRuntimeSort() {
        val shortItem = createItem(1, "Short", runtimeMinutes = 90)
        val longItem = createItem(2, "Long", runtimeMinutes = 180)
        val list = listOf(shortItem, longItem)

        // Ascending: shortest first
        val asc = list.applySort(WatchlistSort(WatchlistSortCriterion.Runtime, SortDirection.Ascending))
        assertEquals(listOf(shortItem, longItem), asc)

        // Descending: longest first
        val desc = list.applySort(WatchlistSort(WatchlistSortCriterion.Runtime, SortDirection.Descending))
        assertEquals(listOf(longItem, shortItem), desc)
    }

    @Test
    fun testSeasonsAndEpisodesSort() {
        val s1 = createItem(1, "Few", seasonCount = 2, episodeCount = 10)
        val s2 = createItem(2, "Many", seasonCount = 8, episodeCount = 100)
        val list = listOf(s1, s2)

        // Seasons Ascending: fewest first
        val ascSeasons = list.applySort(WatchlistSort(WatchlistSortCriterion.Seasons, SortDirection.Ascending))
        assertEquals(listOf(s1, s2), ascSeasons)

        // Seasons Descending: most first
        val descSeasons = list.applySort(WatchlistSort(WatchlistSortCriterion.Seasons, SortDirection.Descending))
        assertEquals(listOf(s2, s1), descSeasons)

        // Episodes Ascending: fewest first
        val ascEpisodes = list.applySort(WatchlistSort(WatchlistSortCriterion.Episodes, SortDirection.Ascending))
        assertEquals(listOf(s1, s2), ascEpisodes)

        // Episodes Descending: most first
        val descEpisodes = list.applySort(WatchlistSort(WatchlistSortCriterion.Episodes, SortDirection.Descending))
        assertEquals(listOf(s2, s1), descEpisodes)
    }
}
