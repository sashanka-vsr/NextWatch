package com.nextwatch.app.data.backup

data class BackupData(
    val format: String = BACKUP_FORMAT,
    val version: Int = BACKUP_VERSION,
    val exportedAt: Long = System.currentTimeMillis(),
    val items: List<BackupMediaItem> = emptyList(),
) {
    companion object {
        const val BACKUP_FORMAT = "nextwatch_backup"
        const val BACKUP_VERSION = 1
    }
}

data class BackupMediaItem(
    val tmdbId: Int? = null,
    val imdbId: String? = null,
    val title: String,
    val type: String,
    val status: String,
    val overview: String? = null,
    val posterUrl: String? = null,
    val posterAssetPath: String? = null,
    val releaseDate: String? = null,
    val runtimeMinutes: Int? = null,
    val seasonCount: Int? = null,
    val imdbRating: Double? = null,
    val director: String? = null,
    val creator: String? = null,
    val episodeCount: Int? = null,
    val originalLanguage: String? = null,
    val country: String? = null,
    val dateAdded: Long = System.currentTimeMillis(),
    val genres: List<String> = emptyList(),
)

sealed interface BackupExportResult {
    data class Success(val itemCount: Int, val posterCount: Int) : BackupExportResult
    data class Failure(val message: String, val throwable: Throwable? = null) : BackupExportResult
}

sealed interface BackupValidationResult {
    data class Success(
        val backupData: BackupData,
        val movieCount: Int,
        val seriesCount: Int,
        val usablePostersCount: Int,
        val stagingDirectory: java.io.File,
    ) : BackupValidationResult

    data class Failure(val message: String, val throwable: Throwable? = null) : BackupValidationResult
}

sealed interface BackupRestoreResult {
    data class Success(val restoredCount: Int) : BackupRestoreResult
    data class Failure(val message: String, val throwable: Throwable? = null) : BackupRestoreResult
}
