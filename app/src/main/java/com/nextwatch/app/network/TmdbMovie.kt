package com.nextwatch.app.network

typealias TmdbMovie = SearchResult

data class OmdbDetails(
    val imdbId: String?,
    val imdbRating: Double?,
    val director: String? = null,
    val country: String? = null,
    val language: String? = null,
)

data class TmdbDetails(
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val releaseDate: String?,
    val runtimeMinutes: Int?,
    val seasonCount: Int?,
    val episodeCount: Int? = null,
    val director: String? = null,
    val creator: String? = null,
    val originalLanguage: String? = null,
    val country: String? = null,
    val genres: List<String>,
    val posterUrl: String?,
    val imdbId: String?,
)

data class StreamingProvider(
    val providerId: Int,
    val providerName: String,
    val logoPath: String?,
)

data class RegionAvailability(
    val link: String?,
    val flatrate: List<StreamingProvider>,
    val rent: List<StreamingProvider>,
    val buy: List<StreamingProvider>,
)