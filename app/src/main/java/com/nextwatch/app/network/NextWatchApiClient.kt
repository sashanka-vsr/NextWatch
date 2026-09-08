package com.nextwatch.app.network

import com.nextwatch.app.data.MediaItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class SearchResult(
    val tmdbId: Int,
    val title: String,
    val year: String?,
    val posterUrl: String?,
    val mediaType: String,
)

class NextWatchApiClient(
    private val httpClient: HttpClient = sharedClient,
    private val tmdbApiKey: String = ApiKeys.TMDB,
    private val omdbApiKey: String = ApiKeys.OMDB,
) {

    suspend fun searchMovies(query: String): List<TmdbMovie> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext emptyList()

        val body = httpClient.get(TMDB_SEARCH_URL) {
            parameter("api_key", tmdbApiKey)
            parameter("query", trimmed)
            parameter("include_adult", false)
        }.bodyAsText()

        parseTmdbResults(body)
    }

    suspend fun fetchOmdbDetails(
        title: String,
        year: String?,
        mediaType: String? = null,
    ): OmdbDetails? = withContext(Dispatchers.IO) {
        val body = httpClient.get(OMDB_URL) {
            parameter("apikey", omdbApiKey)
            parameter("t", title)
            if (!year.isNullOrBlank()) {
                parameter("y", year)
            }
            when (mediaType) {
                MediaItem.TYPE_SERIES -> parameter("type", "series")
                MediaItem.TYPE_MOVIE -> parameter("type", "movie")
            }
        }.bodyAsText()

        parseOmdbDetails(body)
    }

    private fun parseTmdbResults(body: String): List<TmdbMovie> {
        val results = JSONObject(body).optJSONArray("results") ?: return emptyList()
        return buildList {
            for (index in 0 until results.length()) {
                val item = results.optJSONObject(index) ?: continue
                val mediaType = when (item.optString("media_type")) {
                    "movie" -> MediaItem.TYPE_MOVIE
                    "tv" -> MediaItem.TYPE_SERIES
                    else -> continue
                }
                val title = when (mediaType) {
                    MediaItem.TYPE_SERIES -> item.optString("name")
                    else -> item.optString("title")
                }.takeIf { it.isNotBlank() && it != "null" } ?: continue
                val dateField = when (mediaType) {
                    MediaItem.TYPE_SERIES -> "first_air_date"
                    else -> "release_date"
                }
                add(
                    TmdbMovie(
                        tmdbId = item.optInt("id"),
                        title = title,
                        year = item.optString(dateField).take(4).takeIf { it.length == 4 },
                        posterUrl = posterUrl(item.optString("poster_path")),
                        mediaType = mediaType,
                    ),
                )
            }
        }
    }

    private fun parseOmdbDetails(body: String): OmdbDetails? {
        val json = JSONObject(body)
        if (json.optString("Response") == "False") return null
        return OmdbDetails(
            imdbId = clean(json.optString("imdbID")),
            imdbRating = clean(json.optString("imdbRating")),
            rottenTomatoesRating = rottenTomatoes(json.optJSONArray("Ratings")),
            runtime = clean(json.optString("Runtime")),
        )
    }

    private fun posterUrl(path: String): String? {
        val trimmed = path.takeIf { it.isNotBlank() && it != "null" } ?: return null
        return "$TMDB_IMAGE_BASE$trimmed"
    }

    private fun rottenTomatoes(ratings: JSONArray?): String? {
        if (ratings == null) return null
        for (index in 0 until ratings.length()) {
            val rating = ratings.optJSONObject(index) ?: continue
            if (rating.optString("Source") == "Rotten Tomatoes") {
                return clean(rating.optString("Value"))
            }
        }
        return null
    }

    private fun clean(value: String): String? =
        value.takeIf { it.isNotBlank() && it != "null" && it != "N/A" }

    companion object {
        private const val TMDB_SEARCH_URL = "https://api.themoviedb.org/3/search/multi"
        private const val TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w185"
        private const val OMDB_URL = "https://www.omdbapi.com/"

        private val sharedClient by lazy {
            HttpClient(Android) {
                expectSuccess = false
            }
        }
    }
}
