package com.nextwatch.app

import com.nextwatch.app.data.MediaItem
import com.nextwatch.app.data.backup.BackupData
import com.nextwatch.app.data.backup.BackupMediaItem
import com.nextwatch.app.data.backup.BackupSerializer
import com.nextwatch.app.data.backup.BackupZipManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BackupTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testSerializationAndDeserializationFull() {
        val originalItems = listOf(
            BackupMediaItem(
                tmdbId = 550,
                imdbId = "tt0137523",
                title = "Fight Club",
                type = MediaItem.TYPE_MOVIE,
                status = MediaItem.STATUS_WATCHED,
                overview = "An insomniac office worker...",
                posterUrl = "https://image.tmdb.org/t/p/w500/fight_club.jpg",
                posterAssetPath = "posters/tmdb_550.jpg",
                releaseDate = "1999-10-15",
                runtimeMinutes = 139,
                seasonCount = null,
                imdbRating = 8.8,
                director = "David Fincher",
                creator = null,
                episodeCount = null,
                originalLanguage = "en",
                country = "United States",
                dateAdded = 1600000000000L,
                genres = listOf("Drama", "Thriller"),
            ),
            BackupMediaItem(
                tmdbId = 1399,
                imdbId = "tt0944947",
                title = "Game of Thrones",
                type = MediaItem.TYPE_SERIES,
                status = MediaItem.STATUS_WATCHING,
                overview = "Seven noble families...",
                posterUrl = "https://image.tmdb.org/t/p/w500/got.jpg",
                posterAssetPath = "posters/tmdb_1399.jpg",
                releaseDate = "2011-04-17",
                runtimeMinutes = null,
                seasonCount = 8,
                imdbRating = 9.2,
                director = null,
                creator = "David Benioff",
                episodeCount = 73,
                originalLanguage = "en",
                country = "United States",
                dateAdded = 1610000000000L,
                genres = listOf("Action", "Adventure", "Drama", "Fantasy"),
            ),
            BackupMediaItem(
                tmdbId = 27205,
                imdbId = "tt1375666",
                title = "Inception",
                type = MediaItem.TYPE_MOVIE,
                status = MediaItem.STATUS_REWATCH,
                overview = "Cobb steals information...",
                posterUrl = "https://image.tmdb.org/t/p/w500/inception.jpg",
                posterAssetPath = null,
                releaseDate = "2010-07-16",
                runtimeMinutes = 148,
                seasonCount = null,
                imdbRating = 8.8,
                director = "Christopher Nolan",
                creator = null,
                episodeCount = null,
                originalLanguage = "en",
                country = "United States",
                dateAdded = 1620000000000L,
                genres = listOf("Action", "Sci-Fi"),
            ),
            BackupMediaItem(
                tmdbId = null,
                imdbId = "tt1234567",
                title = "Custom Indie Short",
                type = MediaItem.TYPE_MOVIE,
                status = MediaItem.STATUS_WATCHLIST,
                overview = null,
                posterUrl = null,
                posterAssetPath = null,
                releaseDate = null,
                runtimeMinutes = 20,
                seasonCount = null,
                imdbRating = null,
                director = "Indie Director",
                creator = null,
                episodeCount = null,
                originalLanguage = null,
                country = null,
                dateAdded = 1630000000000L,
                genres = emptyList(),
            )
        )

        val backupData = BackupData(
            format = BackupData.BACKUP_FORMAT,
            version = BackupData.BACKUP_VERSION,
            exportedAt = 1640000000000L,
            items = originalItems,
        )

        val jsonString = BackupSerializer.toJson(backupData)
        val deserialized = BackupSerializer.fromJson(jsonString)

        assertEquals(BackupData.BACKUP_FORMAT, deserialized.format)
        assertEquals(BackupData.BACKUP_VERSION, deserialized.version)
        assertEquals(1640000000000L, deserialized.exportedAt)
        assertEquals(4, deserialized.items.size)

        // Item 1
        val item0 = deserialized.items[0]
        assertEquals(550, item0.tmdbId)
        assertEquals("tt0137523", item0.imdbId)
        assertEquals("Fight Club", item0.title)
        assertEquals(MediaItem.TYPE_MOVIE, item0.type)
        assertEquals(MediaItem.STATUS_WATCHED, item0.status)
        assertEquals("An insomniac office worker...", item0.overview)
        assertEquals("posters/tmdb_550.jpg", item0.posterAssetPath)
        assertEquals(139, item0.runtimeMinutes)
        assertEquals(8.8, item0.imdbRating!!, 0.01)
        assertEquals("David Fincher", item0.director)
        assertEquals(listOf("Drama", "Thriller"), item0.genres)

        // Item 2
        val item1 = deserialized.items[1]
        assertEquals(1399, item1.tmdbId)
        assertEquals(MediaItem.TYPE_SERIES, item1.type)
        assertEquals(MediaItem.STATUS_WATCHING, item1.status)
        assertEquals(8, item1.seasonCount)
        assertEquals(73, item1.episodeCount)
        assertEquals("David Benioff", item1.creator)
        assertEquals(listOf("Action", "Adventure", "Drama", "Fantasy"), item1.genres)

        // Item 3
        val item2 = deserialized.items[2]
        assertEquals(MediaItem.STATUS_REWATCH, item2.status)
        assertNull(item2.posterAssetPath)

        // Item 4 (preserves nulls)
        val item3 = deserialized.items[3]
        assertNull(item3.tmdbId)
        assertEquals("tt1234567", item3.imdbId)
        assertEquals(MediaItem.STATUS_WATCHLIST, item3.status)
        assertNull(item3.overview)
        assertNull(item3.posterUrl)
        assertNull(item3.imdbRating)
        assertTrue(item3.genres.isEmpty())
    }

    @Test
    fun testEmptyBackupValid() {
        val backupData = BackupData(
            items = emptyList(),
        )

        val json = BackupSerializer.toJson(backupData)
        val result = BackupSerializer.fromJson(json)

        assertEquals(BackupData.BACKUP_FORMAT, result.format)
        assertEquals(BackupData.BACKUP_VERSION, result.version)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun testInvalidFormatRejected() {
        val invalidJson = """
            {
                "format": "wrong_format",
                "version": 1,
                "items": []
            }
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            BackupSerializer.fromJson(invalidJson)
        }
    }

    @Test
    fun testUnsupportedVersionRejected() {
        val invalidJson = """
            {
                "format": "nextwatch_backup",
                "version": 999,
                "items": []
            }
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            BackupSerializer.fromJson(invalidJson)
        }
    }

    @Test
    fun testMalformedJsonRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            BackupSerializer.fromJson("{ not valid json at all")
        }
    }

    @Test
    fun testDuplicateTmdbIdAndTypeRejected() {
        val duplicateJson = """
            {
                "format": "nextwatch_backup",
                "version": 1,
                "items": [
                    {
                        "tmdbId": 100,
                        "title": "Movie One",
                        "type": "Movie",
                        "status": "Watchlist"
                    },
                    {
                        "tmdbId": 100,
                        "title": "Movie One Duplicate",
                        "type": "Movie",
                        "status": "Watched"
                    }
                ]
            }
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            BackupSerializer.fromJson(duplicateJson)
        }
    }

    @Test
    fun testInvalidMediaTypeRejected() {
        val invalidJson = """
            {
                "format": "nextwatch_backup",
                "version": 1,
                "items": [
                    {
                        "title": "Invalid Type Item",
                        "type": "Podcast",
                        "status": "Watchlist"
                    }
                ]
            }
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            BackupSerializer.fromJson(invalidJson)
        }
    }

    @Test
    fun testInvalidStatusRejected() {
        val invalidJson = """
            {
                "format": "nextwatch_backup",
                "version": 1,
                "items": [
                    {
                        "title": "Invalid Status Item",
                        "type": "Movie",
                        "status": "Archived"
                    }
                ]
            }
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            BackupSerializer.fromJson(invalidJson)
        }
    }

    @Test
    fun testMissingTitleRejected() {
        val invalidJson = """
            {
                "format": "nextwatch_backup",
                "version": 1,
                "items": [
                    {
                        "title": "   ",
                        "type": "Movie",
                        "status": "Watchlist"
                    }
                ]
            }
        """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            BackupSerializer.fromJson(invalidJson)
        }
    }

    @Test
    fun testZipArchiveCreationAndExtraction() {
        val posterFile = tempFolder.newFile("sample_poster.jpg")
        // Write mock JPEG bytes
        posterFile.writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x00, 0x10))

        val backupData = BackupData(
            items = listOf(
                BackupMediaItem(
                    tmdbId = 123,
                    title = "Test Title",
                    type = MediaItem.TYPE_MOVIE,
                    status = MediaItem.STATUS_WATCHLIST,
                    posterAssetPath = "posters/sample_poster.jpg",
                )
            )
        )
        val json = BackupSerializer.toJson(backupData)

        val zipBytesOut = ByteArrayOutputStream()
        BackupZipManager.createBackupZip(
            outputStream = zipBytesOut,
            backupJson = json,
            posterFiles = mapOf("posters/sample_poster.jpg" to posterFile),
        )

        val zipBytes = zipBytesOut.toByteArray()
        assertTrue(zipBytes.isNotEmpty())

        val stagingDir = tempFolder.newFolder("staging")
        val extractedJson = BackupZipManager.extractBackupZip(
            inputStream = ByteArrayInputStream(zipBytes),
            stagingDir = stagingDir,
        )

        assertTrue(extractedJson.exists())
        assertEquals(json, extractedJson.readText())

        val extractedPoster = File(stagingDir, "posters/sample_poster.jpg")
        assertTrue(extractedPoster.exists())
        assertEquals(posterFile.length(), extractedPoster.length())
        assertTrue(BackupZipManager.isUsableImage(extractedPoster))
    }

    @Test
    fun testZipPathTraversalRejected() {
        val outStream = ByteArrayOutputStream()
        ZipOutputStream(outStream).use { zipOut ->
            zipOut.putNextEntry(ZipEntry("../traversal.txt"))
            zipOut.write("malicious".toByteArray())
            zipOut.closeEntry()
        }

        val stagingDir = tempFolder.newFolder("traversal_staging")
        assertThrows(SecurityException::class.java) {
            BackupZipManager.extractBackupZip(
                inputStream = ByteArrayInputStream(outStream.toByteArray()),
                stagingDir = stagingDir,
            )
        }

        // Verify the file was not written outside stagingDir
        val outsideFile = File(stagingDir.parentFile, "traversal.txt")
        assertFalse(outsideFile.exists())
    }

    @Test
    fun testNonZipFileRejected() {
        val nonZipData = "This is definitely not a ZIP file".toByteArray()
        val stagingDir = tempFolder.newFolder("non_zip_staging")

        assertThrows(IllegalArgumentException::class.java) {
            BackupZipManager.extractBackupZip(
                inputStream = ByteArrayInputStream(nonZipData),
                stagingDir = stagingDir,
            )
        }
    }

    @Test
    fun testImageValidationMagicBytes() {
        val jpegFile = tempFolder.newFile("test.jpg")
        jpegFile.writeBytes(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xDB.toByte()))
        assertTrue(BackupZipManager.isUsableImage(jpegFile))

        val pngFile = tempFolder.newFile("test.png")
        pngFile.writeBytes(byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), 0x0D, 0x0A, 0x1A, 0x0A))
        assertTrue(BackupZipManager.isUsableImage(pngFile))

        val textFile = tempFolder.newFile("test.txt")
        textFile.writeText("hello world plain text")
        assertFalse(BackupZipManager.isUsableImage(textFile))

        val emptyFile = tempFolder.newFile("empty.jpg")
        assertFalse(BackupZipManager.isUsableImage(emptyFile))
    }
}
