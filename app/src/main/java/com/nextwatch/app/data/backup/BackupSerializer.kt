package com.nextwatch.app.data.backup

import com.nextwatch.app.data.MediaItem
import org.json.JSONArray
import org.json.JSONObject

object BackupSerializer {

    private val VALID_TYPES = setOf(
        MediaItem.TYPE_MOVIE,
        MediaItem.TYPE_SERIES,
    )

    private val VALID_STATUSES = setOf(
        MediaItem.STATUS_WATCHLIST,
        MediaItem.STATUS_WATCHING,
        MediaItem.STATUS_WATCHED,
        MediaItem.STATUS_REWATCH,
    )

    fun toJson(data: BackupData): String {
        val root = JSONObject()
        root.put("format", data.format)
        root.put("version", data.version)
        root.put("exportedAt", data.exportedAt)

        val itemsArray = JSONArray()
        for (item in data.items) {
            val itemObj = JSONObject()
            if (item.tmdbId != null) itemObj.put("tmdbId", item.tmdbId)
            if (!item.imdbId.isNullOrBlank()) itemObj.put("imdbId", item.imdbId)
            itemObj.put("title", item.title)
            itemObj.put("type", item.type)
            itemObj.put("status", item.status)

            if (!item.overview.isNullOrBlank()) itemObj.put("overview", item.overview)
            if (!item.posterUrl.isNullOrBlank()) itemObj.put("posterUrl", item.posterUrl)
            if (!item.posterAssetPath.isNullOrBlank()) itemObj.put("posterAssetPath", item.posterAssetPath)
            if (!item.releaseDate.isNullOrBlank()) itemObj.put("releaseDate", item.releaseDate)
            if (item.runtimeMinutes != null) itemObj.put("runtimeMinutes", item.runtimeMinutes)
            if (item.seasonCount != null) itemObj.put("seasonCount", item.seasonCount)
            if (item.imdbRating != null) itemObj.put("imdbRating", item.imdbRating)
            if (!item.director.isNullOrBlank()) itemObj.put("director", item.director)
            if (!item.creator.isNullOrBlank()) itemObj.put("creator", item.creator)
            if (item.episodeCount != null) itemObj.put("episodeCount", item.episodeCount)
            if (!item.originalLanguage.isNullOrBlank()) itemObj.put("originalLanguage", item.originalLanguage)
            if (!item.country.isNullOrBlank()) itemObj.put("country", item.country)
            itemObj.put("dateAdded", item.dateAdded)

            val genresArray = JSONArray()
            for (genre in item.genres) {
                if (genre.isNotBlank()) {
                    genresArray.put(genre.trim())
                }
            }
            itemObj.put("genres", genresArray)

            itemsArray.put(itemObj)
        }

        root.put("items", itemsArray)
        return root.toString(2)
    }

    fun fromJson(jsonStr: String): BackupData {
        if (jsonStr.isBlank()) {
            throw IllegalArgumentException("Backup JSON content is empty")
        }

        val root = try {
            JSONObject(jsonStr)
        } catch (e: Exception) {
            throw IllegalArgumentException("Malformed JSON backup: ${e.message}", e)
        }

        val format = root.optString("format", "")
        if (format != BackupData.BACKUP_FORMAT) {
            throw IllegalArgumentException("Unsupported backup format: '$format'")
        }

        val version = root.optInt("version", -1)
        if (version != BackupData.BACKUP_VERSION) {
            throw IllegalArgumentException("Unsupported backup version: $version")
        }

        val exportedAt = if (root.has("exportedAt")) root.optLong("exportedAt") else System.currentTimeMillis()

        val itemsArray = root.optJSONArray("items")
            ?: throw IllegalArgumentException("Missing 'items' list in backup")

        val items = mutableListOf<BackupMediaItem>()
        val seenTmdbKeys = mutableSetOf<Pair<Int, String>>()

        for (i in 0 until itemsArray.length()) {
            val itemObj = itemsArray.optJSONObject(i)
                ?: throw IllegalArgumentException("Backup item at index $i is not a valid JSON object")

            val title = itemObj.optString("title", "").trim()
            if (title.isBlank()) {
                throw IllegalArgumentException("Backup item at index $i is missing required title")
            }

            val type = itemObj.optString("type", "").trim()
            if (type !in VALID_TYPES) {
                throw IllegalArgumentException("Item '$title' has unsupported media type: '$type'")
            }

            val status = itemObj.optString("status", "").trim()
            if (status !in VALID_STATUSES) {
                throw IllegalArgumentException("Item '$title' has unsupported status: '$status'")
            }

            val tmdbId = if (itemObj.has("tmdbId") && !itemObj.isNull("tmdbId")) {
                itemObj.optInt("tmdbId").takeIf { it != 0 }
            } else null

            val imdbId = if (itemObj.has("imdbId") && !itemObj.isNull("imdbId")) {
                itemObj.optString("imdbId").trim().takeIf { it.isNotBlank() && it != "null" }
            } else null

            // Check for duplicate / conflicting records
            if (tmdbId != null) {
                val key = tmdbId to type
                if (!seenTmdbKeys.add(key)) {
                    throw IllegalArgumentException(
                        "Duplicate record found with TMDB ID $tmdbId and type '$type'"
                    )
                }
            }

            val overview = if (itemObj.has("overview") && !itemObj.isNull("overview")) {
                itemObj.optString("overview").trim().takeIf { it.isNotBlank() && it != "null" }
            } else null

            val posterUrl = if (itemObj.has("posterUrl") && !itemObj.isNull("posterUrl")) {
                itemObj.optString("posterUrl").trim().takeIf { it.isNotBlank() && it != "null" }
            } else null

            val posterAssetPath = if (itemObj.has("posterAssetPath") && !itemObj.isNull("posterAssetPath")) {
                itemObj.optString("posterAssetPath").trim().takeIf { it.isNotBlank() && it != "null" }
            } else null

            val releaseDate = if (itemObj.has("releaseDate") && !itemObj.isNull("releaseDate")) {
                itemObj.optString("releaseDate").trim().takeIf { it.isNotBlank() && it != "null" }
            } else null

            val runtimeMinutes = if (itemObj.has("runtimeMinutes") && !itemObj.isNull("runtimeMinutes")) {
                itemObj.optInt("runtimeMinutes").takeIf { it > 0 }
            } else null

            val seasonCount = if (itemObj.has("seasonCount") && !itemObj.isNull("seasonCount")) {
                itemObj.optInt("seasonCount").takeIf { it > 0 }
            } else null

            val imdbRating = if (itemObj.has("imdbRating") && !itemObj.isNull("imdbRating")) {
                itemObj.optDouble("imdbRating").takeIf { !it.isNaN() }
            } else null

            val director = if (itemObj.has("director") && !itemObj.isNull("director")) {
                itemObj.optString("director").trim().takeIf { it.isNotBlank() && it != "null" }
            } else null

            val creator = if (itemObj.has("creator") && !itemObj.isNull("creator")) {
                itemObj.optString("creator").trim().takeIf { it.isNotBlank() && it != "null" }
            } else null

            val episodeCount = if (itemObj.has("episodeCount") && !itemObj.isNull("episodeCount")) {
                itemObj.optInt("episodeCount").takeIf { it > 0 }
            } else null

            val originalLanguage = if (itemObj.has("originalLanguage") && !itemObj.isNull("originalLanguage")) {
                itemObj.optString("originalLanguage").trim().takeIf { it.isNotBlank() && it != "null" }
            } else null

            val country = if (itemObj.has("country") && !itemObj.isNull("country")) {
                itemObj.optString("country").trim().takeIf { it.isNotBlank() && it != "null" }
            } else null

            val dateAdded = if (itemObj.has("dateAdded") && !itemObj.isNull("dateAdded")) {
                itemObj.optLong("dateAdded", System.currentTimeMillis())
            } else System.currentTimeMillis()

            val genres = mutableListOf<String>()
            val genresArray = itemObj.optJSONArray("genres")
            if (genresArray != null) {
                for (g in 0 until genresArray.length()) {
                    val genreStr = genresArray.optString(g, "").trim()
                    if (genreStr.isNotBlank() && genreStr !in genres) {
                        genres.add(genreStr)
                    }
                }
            }

            items.add(
                BackupMediaItem(
                    tmdbId = tmdbId,
                    imdbId = imdbId,
                    title = title,
                    type = type,
                    status = status,
                    overview = overview,
                    posterUrl = posterUrl,
                    posterAssetPath = posterAssetPath,
                    releaseDate = releaseDate,
                    runtimeMinutes = runtimeMinutes,
                    seasonCount = seasonCount,
                    imdbRating = imdbRating,
                    director = director,
                    creator = creator,
                    episodeCount = episodeCount,
                    originalLanguage = originalLanguage,
                    country = country,
                    dateAdded = dateAdded,
                    genres = genres,
                )
            )
        }

        return BackupData(
            format = format,
            version = version,
            exportedAt = exportedAt,
            items = items,
        )
    }
}
