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
        imdbId: String,
        ): OmdbDetails? = withContext(Dispatchers.IO) {

            val body = httpClient.get(OMDB_URL) {
                parameter("apikey", omdbApiKey)
                parameter("i", imdbId)
            }.bodyAsText()

            parseOmdbDetails(body)
    }

    suspend fun fetchTmdbDetails(
            tmdbId: Int,
            mediaType: String,
        ): TmdbDetails? = withContext(Dispatchers.IO) {

            val url = when (mediaType) {
                MediaItem.TYPE_MOVIE -> "$TMDB_BASE_URL/movie/$tmdbId"
                MediaItem.TYPE_SERIES -> "$TMDB_BASE_URL/tv/$tmdbId"
                else -> return@withContext null
            }

            val body = httpClient.get(url) {
                parameter("api_key", tmdbApiKey)
                parameter("append_to_response", "external_ids,credits")
            }.bodyAsText()

            parseTmdbDetails(body, mediaType)
    }

    private fun parseTmdbDetails(
            body: String,
            mediaType: String,
    ): TmdbDetails? {

    val json = JSONObject(body)

    if (json.has("success") && !json.optBoolean("success", true)) {
        return null
    }

    val title = when (mediaType) {
        MediaItem.TYPE_MOVIE -> json.optString("title")
        MediaItem.TYPE_SERIES -> json.optString("name")
        else -> ""
    }.takeIf { it.isNotBlank() } ?: return null

    val releaseDate = when (mediaType) {
        MediaItem.TYPE_MOVIE -> json.optString("release_date")
        MediaItem.TYPE_SERIES -> json.optString("first_air_date")
        else -> ""
    }.takeIf { it.isNotBlank() }

    val runtimeMinutes = when (mediaType) {
        MediaItem.TYPE_MOVIE -> {
            json.optInt("runtime", 0).takeIf { it > 0 }
        }

        MediaItem.TYPE_SERIES -> {
            val runtimes = json.optJSONArray("episode_run_time")

            if (runtimes != null && runtimes.length() > 0) {
                runtimes.optInt(0).takeIf { it > 0 }
            } else {
                null
            }
        }

        else -> null
    }

    val seasonCount = if (mediaType == MediaItem.TYPE_SERIES) {
        json.optInt("number_of_seasons", 0)
            .takeIf { it > 0 }
    } else {
        null
    }

    val episodeCount = if (mediaType == MediaItem.TYPE_SERIES) {
        json.optInt("number_of_episodes", 0)
            .takeIf { it > 0 }
    } else {
        null
    }

    val creator = if (mediaType == MediaItem.TYPE_SERIES) {
        val createdBy = json.optJSONArray("created_by")
        if (createdBy != null && createdBy.length() > 0) {
            val names = mutableListOf<String>()
            for (index in 0 until createdBy.length()) {
                val name = createdBy.optJSONObject(index)
                    ?.optString("name")
                    ?.takeIf { it.isNotBlank() && it != "null" }
                if (name != null && !names.contains(name)) {
                    names.add(name)
                }
            }
            names.joinToString(", ").takeIf { it.isNotBlank() }
        } else {
            null
        }
    } else {
        null
    }

    val director = if (mediaType == MediaItem.TYPE_MOVIE) {
        val crew = json.optJSONObject("credits")?.optJSONArray("crew")
        if (crew != null && crew.length() > 0) {
            val directors = mutableListOf<String>()
            for (index in 0 until crew.length()) {
                val member = crew.optJSONObject(index) ?: continue
                if (member.optString("job").equals("Director", ignoreCase = true)) {
                    val name = member.optString("name")
                        ?.takeIf { it.isNotBlank() && it != "null" }
                    if (name != null && !directors.contains(name)) {
                        directors.add(name)
                    }
                }
            }
            directors.joinToString(", ").takeIf { it.isNotBlank() }
        } else {
            null
        }
    } else {
        null
    }

    val rawLang = json.optString("original_language").takeIf { it.isNotBlank() && it != "null" }
    val originalLanguage = rawLang?.let { code ->
        try {
            val locale = java.util.Locale.forLanguageTag(code)
            val display = locale.getDisplayLanguage(java.util.Locale.ENGLISH)
            if (display.isNotBlank() && !display.equals(code, ignoreCase = true)) {
                display.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ENGLISH) else it.toString() }
            } else {
                code.uppercase(java.util.Locale.ENGLISH)
            }
        } catch (_: Exception) {
            code.uppercase(java.util.Locale.ENGLISH)
        }
    }

    val country = run {
        val prodCountries = json.optJSONArray("production_countries")
        if (prodCountries != null && prodCountries.length() > 0) {
            prodCountries.optJSONObject(0)
                ?.optString("name")
                ?.takeIf { it.isNotBlank() && it != "null" }
        } else {
            null
        }
    }

    val genres = buildList {
        val genreArray = json.optJSONArray("genres") ?: return@buildList

        for (index in 0 until genreArray.length()) {
            val genre = genreArray.optJSONObject(index)
                ?.optString("name")
                ?.takeIf { it.isNotBlank() }

            if (genre != null) {
                add(genre)
            }
        }
    }

    val posterPath = json.optString("poster_path")

    val imdbId = if (mediaType == MediaItem.TYPE_SERIES) {
        json.optJSONObject("external_ids")
            ?.optString("imdb_id")
            ?.takeIf { it.isNotBlank() && it != "null" }
    } else {
        json.optString("imdb_id")
            .takeIf { it.isNotBlank() && it != "null" }
    }

    return TmdbDetails(
        tmdbId = json.optInt("id"),
        title = title,
        overview = json.optString("overview")
            .takeIf { it.isNotBlank() && it != "null" },
        releaseDate = releaseDate,
        runtimeMinutes = runtimeMinutes,
        seasonCount = seasonCount,
        episodeCount = episodeCount,
        director = director,
        creator = creator,
        originalLanguage = originalLanguage,
        country = country,
        genres = genres,
        posterUrl = posterUrl(posterPath),
        imdbId = imdbId,
    )
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
    
        if (json.optString("Response") == "False") {
            return null
        }
    
        val imdbId = clean(json.optString("imdbID"))
    
        val imdbRating = json.optString("imdbRating")
            .toDoubleOrNull()

        val director = clean(json.optString("Director"))
        val country = clean(json.optString("Country"))
    
        return OmdbDetails(
            imdbId = imdbId,
            imdbRating = imdbRating,
            director = director,
            country = country,
        )
    }

    private fun posterUrl(path: String): String? {
        val trimmed = path.takeIf { it.isNotBlank() && it != "null" } ?: return null
        return "$TMDB_IMAGE_BASE$trimmed"
    }

    private fun clean(value: String): String? =
        value.takeIf { it.isNotBlank() && it != "null" && it != "N/A" }

    companion object {
        private const val TMDB_BASE_URL = "https://api.themoviedb.org/3"
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
