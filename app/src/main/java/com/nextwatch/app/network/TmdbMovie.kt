package com.nextwatch.app.network

typealias TmdbMovie = SearchResult

data class OmdbDetails(
    val imdbId: String?,
    val imdbRating: Double?,
)

data class TmdbDetails(
    val tmdbId: Int,
    val title: String,
    val overview: String?,
    val releaseDate: String?,
    val runtimeMinutes: Int?,
    val seasonCount: Int?,
    val genres: List<String>,
    val posterUrl: String?,
    val imdbId: String?,
)