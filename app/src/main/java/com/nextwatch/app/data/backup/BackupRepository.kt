package com.nextwatch.app.data.backup

import androidx.room.withTransaction
import com.nextwatch.app.data.MediaDao
import com.nextwatch.app.data.MediaGenre
import com.nextwatch.app.data.MediaGenreDao
import com.nextwatch.app.data.MediaItem
import com.nextwatch.app.data.NextWatchDatabase
import com.nextwatch.app.data.PosterCache
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupRepository(
    private val database: NextWatchDatabase,
    private val mediaDao: MediaDao,
    private val mediaGenreDao: MediaGenreDao,
    private val posterCache: PosterCache,
    private val cacheDir: File,
) {

    suspend fun exportBackup(outputStream: OutputStream): BackupExportResult {
        return withContext(Dispatchers.IO) {
            try {
                val items = mediaDao.getAllItems()
                val genres = mediaGenreDao.getAllGenres()
                val genresByMediaId = genres.groupBy({ it.mediaId }, { it.genre })

                val posterFiles = mutableMapOf<String, File>()
                val localPathToArchivePath = mutableMapOf<String, String>()
                val backupItems = mutableListOf<BackupMediaItem>()

                for (item in items) {
                    var archiveAssetPath: String? = null

                    val localFile = item.posterLocalPath
                        ?.takeIf { it.isNotBlank() }
                        ?.let(::File)
                        ?.takeIf { it.exists() && it.canRead() && it.length() > 0L }

                    if (localFile != null) {
                        val canonical = localFile.canonicalPath
                        val existingArchivePath = localPathToArchivePath[canonical]
                        if (existingArchivePath != null) {
                            archiveAssetPath = existingArchivePath
                        } else {
                            val baseName = posterCache.posterFileName(item.tmdbId, item.imdbId)
                                ?: "poster_${item.id}.jpg"
                            val newArchivePath = "${BackupZipManager.POSTERS_DIR_PREFIX}$baseName"
                            posterFiles[newArchivePath] = localFile
                            localPathToArchivePath[canonical] = newArchivePath
                            archiveAssetPath = newArchivePath
                        }
                    }

                    backupItems.add(
                        BackupMediaItem(
                            tmdbId = item.tmdbId,
                            imdbId = item.imdbId,
                            title = item.title,
                            type = item.type,
                            status = item.status,
                            overview = item.overview,
                            posterUrl = item.posterUrl,
                            posterAssetPath = archiveAssetPath,
                            releaseDate = item.releaseDate,
                            runtimeMinutes = item.runtimeMinutes,
                            seasonCount = item.seasonCount,
                            imdbRating = item.imdbRating,
                            director = item.director,
                            creator = item.creator,
                            episodeCount = item.episodeCount,
                            originalLanguage = item.originalLanguage,
                            country = item.country,
                            dateAdded = item.dateAdded,
                            genres = genresByMediaId[item.id].orEmpty(),
                        )
                    )
                }

                val backupData = BackupData(
                    exportedAt = System.currentTimeMillis(),
                    items = backupItems,
                )

                val jsonString = BackupSerializer.toJson(backupData)
                BackupZipManager.createBackupZip(outputStream, jsonString, posterFiles)

                BackupExportResult.Success(
                    itemCount = backupItems.size,
                    posterCount = posterFiles.size,
                )
            } catch (t: Throwable) {
                BackupExportResult.Failure(
                    message = t.message ?: "Failed to export backup",
                    throwable = t,
                )
            }
        }
    }

    suspend fun validateBackup(inputStream: InputStream): BackupValidationResult {
        return withContext(Dispatchers.IO) {
            val stagingDir = File(
                cacheDir,
                "backup_staging_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
            )

            try {
                val jsonFile = BackupZipManager.extractBackupZip(inputStream, stagingDir)
                val jsonString = jsonFile.readText(Charsets.UTF_8)
                val backupData = BackupSerializer.fromJson(jsonString)

                var usablePostersCount = 0
                for (item in backupData.items) {
                    val assetPath = item.posterAssetPath
                    if (!assetPath.isNullOrBlank()) {
                        val stagedPoster = File(stagingDir, assetPath)
                        if (BackupZipManager.isUsableImage(stagedPoster)) {
                            usablePostersCount++
                        }
                    }
                }

                val movieCount = backupData.items.count { it.type == MediaItem.TYPE_MOVIE }
                val seriesCount = backupData.items.count { it.type == MediaItem.TYPE_SERIES }

                BackupValidationResult.Success(
                    backupData = backupData,
                    movieCount = movieCount,
                    seriesCount = seriesCount,
                    usablePostersCount = usablePostersCount,
                    stagingDirectory = stagingDir,
                )
            } catch (t: Throwable) {
                stagingDir.deleteRecursively()
                BackupValidationResult.Failure(
                    message = t.message ?: "Backup validation failed",
                    throwable = t,
                )
            }
        }
    }

    suspend fun restoreBackup(validated: BackupValidationResult.Success): BackupRestoreResult {
        return withContext(Dispatchers.IO) {
            val newlyInstalledPosters = mutableListOf<File>()
            val activeLocalPaths = mutableSetOf<String>()

            try {
                val sessionId = System.currentTimeMillis()
                val archivePathToInstalledPath = mutableMapOf<String, String>()

                // 1. Install usable posters with unique filenames to prevent clobbering existing ones
                for (item in validated.backupData.items) {
                    val assetPath = item.posterAssetPath
                    if (!assetPath.isNullOrBlank() && assetPath !in archivePathToInstalledPath) {
                        val stagedFile = File(validated.stagingDirectory, assetPath)
                        if (BackupZipManager.isUsableImage(stagedFile)) {
                            val cleanName = stagedFile.name.replace(Regex("[^A-Za-z0-9._-]"), "_")
                            val installedFile = File(
                                posterCache.posterDirectory,
                                "imp_${sessionId}_${UUID.randomUUID().toString().take(6)}_$cleanName"
                            )
                            stagedFile.copyTo(installedFile, overwrite = true)
                            newlyInstalledPosters.add(installedFile)
                            archivePathToInstalledPath[assetPath] = installedFile.absolutePath
                        }
                    }
                }

                // 2. Prepare media items and genre entities
                val preparedItemsWithGenres = validated.backupData.items.map { item ->
                    val installedPath = item.posterAssetPath?.let { archivePathToInstalledPath[it] }
                    if (installedPath != null) {
                        activeLocalPaths.add(installedPath)
                    }

                    val mediaItem = MediaItem(
                        id = 0,
                        tmdbId = item.tmdbId,
                        imdbId = item.imdbId,
                        title = item.title,
                        type = item.type,
                        status = item.status,
                        overview = item.overview,
                        posterUrl = item.posterUrl,
                        posterLocalPath = installedPath,
                        releaseDate = item.releaseDate,
                        runtimeMinutes = item.runtimeMinutes,
                        seasonCount = item.seasonCount,
                        imdbRating = item.imdbRating,
                        director = item.director,
                        creator = item.creator,
                        episodeCount = item.episodeCount,
                        originalLanguage = item.originalLanguage,
                        country = item.country,
                        dateAdded = item.dateAdded,
                    )
                    mediaItem to item.genres
                }

                // 3. Atomically replace collection in Room transaction
                database.withTransaction {
                    mediaGenreDao.deleteAll()
                    mediaDao.deleteAll()

                    for ((mediaItem, genres) in preparedItemsWithGenres) {
                        val newId = mediaDao.insert(mediaItem)
                        if (genres.isNotEmpty()) {
                            val genreEntities = genres.map { genre ->
                                MediaGenre(mediaId = newId, genre = genre)
                            }
                            mediaGenreDao.insertAll(genreEntities)
                        }
                    }
                }

                // 4. Safe post-commit obsolete poster cleanup
                runCatching {
                    posterCache.cleanObsoletePosters(activeLocalPaths)
                }

                // 5. Clean up temporary staging directory
                runCatching {
                    validated.stagingDirectory.deleteRecursively()
                }

                BackupRestoreResult.Success(restoredCount = preparedItemsWithGenres.size)
            } catch (t: Throwable) {
                // If anything failed, revert newly installed poster files
                newlyInstalledPosters.forEach { file ->
                    runCatching { file.delete() }
                }
                runCatching {
                    validated.stagingDirectory.deleteRecursively()
                }

                BackupRestoreResult.Failure(
                    message = t.message ?: "Failed to restore backup collection",
                    throwable = t,
                )
            }
        }
    }

    fun cleanupStaging(stagingDir: File) {
        runCatching { stagingDir.deleteRecursively() }
    }
}
