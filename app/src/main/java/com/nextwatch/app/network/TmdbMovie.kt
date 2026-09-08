package com.nextwatch.app.network

typealias TmdbMovie = SearchResult

data class OmdbDetails(
    val imdbId: String?,
    val imdbRating: String?,
    val rottenTomatoesRating: String?,
    val runtime: String?,
)
