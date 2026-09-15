package com.nextwatch.app.data

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PosterCache(
    context: Context,
) {

    private val posterDirectory = File(
        context.applicationContext.filesDir,
        "posters",
    ).apply {
        mkdirs()
    }

    fun getPosterFile(tmdbId: Int?, imdbId: String?): File? {
        val fileName = posterFileName(tmdbId, imdbId) ?: return null
        val file = File(posterDirectory, fileName)

        return file.takeIf { it.exists() && it.length() > 0L }
    }

    suspend fun downloadPoster(
        posterUrl: String?,
        tmdbId: Int?,
        imdbId: String?,
    ): String? {
        if (posterUrl.isNullOrBlank()) return null

        val fileName = posterFileName(tmdbId, imdbId) ?: return null
        val targetFile = File(posterDirectory, fileName)

        return withContext(Dispatchers.IO) {
            if (targetFile.exists() && targetFile.length() > 0L) {
                return@withContext targetFile.absolutePath
            }

            try {
                val connection = URL(posterUrl).openConnection() as HttpURLConnection

                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.requestMethod = "GET"

                connection.connect()

                if (connection.responseCode !in 200..299) {
                    connection.disconnect()
                    return@withContext null
                }

                connection.inputStream.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                connection.disconnect()

                targetFile
                    .takeIf { it.exists() && it.length() > 0L }
                    ?.absolutePath
            } catch (_: Exception) {
                targetFile.delete()
                null
            }
        }
    }

    suspend fun deletePoster(
        tmdbId: Int?,
        imdbId: String?,
        localPath: String? = null,
    ) {
        withContext(Dispatchers.IO) {
            val files = buildSet {
                posterFileName(tmdbId, imdbId)?.let { name ->
                    add(File(posterDirectory, name))
                }
                localPath
                    ?.takeIf { it.isNotBlank() }
                    ?.let(::File)
                    ?.let(::add)
            }

            files.forEach { file ->
                runCatching {
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
        }
    }

    private fun posterFileName(
        tmdbId: Int?,
        imdbId: String?,
    ): String? {
        return when {
            tmdbId != null -> "tmdb_$tmdbId.jpg"
            !imdbId.isNullOrBlank() -> "imdb_${imdbId.replace(Regex("[^A-Za-z0-9._-]"), "_")}.jpg"
            else -> null
        }
    }
}