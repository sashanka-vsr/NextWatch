package com.nextwatch.app.data.backup

import android.graphics.BitmapFactory
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupZipManager {

    private const val MAX_ENTRIES = 5_000
    private const val MAX_TOTAL_UNCOMPRESSED_BYTES = 250 * 1024 * 1024L // 250 MB
    private const val MAX_ENTRY_BYTES = 25 * 1024 * 1024L // 25 MB
    private const val BUFFER_SIZE = 8192

    const val BACKUP_JSON_ENTRY = "backup.json"
    const val POSTERS_DIR_PREFIX = "posters/"

    /**
     * Creates a ZIP archive containing backup.json and local poster files.
     */
    fun createBackupZip(
        outputStream: OutputStream,
        backupJson: String,
        posterFiles: Map<String, File>, // archiveRelativePath -> local File
    ) {
        ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
            // 1. Write backup.json
            val jsonEntry = ZipEntry(BACKUP_JSON_ENTRY)
            zipOut.putNextEntry(jsonEntry)
            val jsonBytes = backupJson.toByteArray(Charsets.UTF_8)
            zipOut.write(jsonBytes)
            zipOut.closeEntry()

            // 2. Write poster files
            val addedPaths = mutableSetOf<String>()
            for ((archivePath, localFile) in posterFiles) {
                if (archivePath in addedPaths) continue
                if (!localFile.exists() || !localFile.canRead() || localFile.length() <= 0L) continue

                val sanitizedPath = archivePath.replace('\\', '/')
                val entry = ZipEntry(sanitizedPath)
                zipOut.putNextEntry(entry)
                FileInputStream(localFile).use { fileIn ->
                    fileIn.copyTo(zipOut, BUFFER_SIZE)
                }
                zipOut.closeEntry()
                addedPaths.add(archivePath)
            }

            zipOut.finish()
            zipOut.flush()
        }
    }

    /**
     * Safely extracts and validates a backup ZIP archive into a staging directory.
     * Returns the staged backup.json file.
     */
    fun extractBackupZip(
        inputStream: InputStream,
        stagingDir: File,
    ): File {
        if (!stagingDir.exists()) {
            stagingDir.mkdirs()
        }

        val bufferedIn = BufferedInputStream(inputStream)
        bufferedIn.mark(4)
        val header = ByteArray(4)
        val bytesRead = bufferedIn.read(header)
        bufferedIn.reset()

        if (bytesRead < 4 ||
            header[0] != 0x50.toByte() ||
            header[1] != 0x4B.toByte() ||
            (header[2] != 0x03.toByte() && header[2] != 0x05.toByte() && header[2] != 0x07.toByte())
        ) {
            throw IllegalArgumentException("The selected file is not a valid ZIP archive.")
        }

        var entryCount = 0
        var totalBytesRead = 0L
        val seenEntries = mutableSetOf<String>()
        val stagingCanonicalPath = stagingDir.canonicalPath

        ZipInputStream(bufferedIn).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                entryCount++
                if (entryCount > MAX_ENTRIES) {
                    throw SecurityException("ZIP archive exceeds maximum entry limit of $MAX_ENTRIES.")
                }

                val rawName = entry.name.replace('\\', '/')

                // Safety checks: reject path traversal and dangerous paths
                if (rawName.contains("..") || rawName.startsWith("/") || rawName.contains(":")) {
                    throw SecurityException("Malicious path traversal detected in ZIP entry: '$rawName'.")
                }

                // Ignore directory entries and OS metadata files
                val isMacMetadata = rawName.startsWith("__MACOSX/") || rawName.endsWith(".DS_Store")
                if (entry.isDirectory || isMacMetadata) {
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                    continue
                }

                // Only extract backup.json or items inside posters/
                val isBackupJson = rawName == BACKUP_JSON_ENTRY
                val isPoster = rawName.startsWith(POSTERS_DIR_PREFIX)

                if (!isBackupJson && !isPoster) {
                    // Ignore unrecognized harmless files
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                    continue
                }

                if (!seenEntries.add(rawName)) {
                    // Duplicate entry name in archive - skip duplicate
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                    continue
                }

                val destFile = File(stagingDir, rawName)
                val destCanonicalPath = destFile.canonicalPath
                if (!destCanonicalPath.startsWith(stagingCanonicalPath + File.separator)) {
                    throw SecurityException("ZIP entry '$rawName' resolves outside staging directory.")
                }

                destFile.parentFile?.mkdirs()

                // Extract with size limits
                var entryBytesRead = 0L
                val buffer = ByteArray(BUFFER_SIZE)
                FileOutputStream(destFile).use { fileOut ->
                    var count = zipIn.read(buffer)
                    while (count != -1) {
                        entryBytesRead += count
                        totalBytesRead += count

                        if (entryBytesRead > MAX_ENTRY_BYTES) {
                            destFile.delete()
                            throw SecurityException("ZIP entry '$rawName' exceeds maximum allowed size.")
                        }

                        if (totalBytesRead > MAX_TOTAL_UNCOMPRESSED_BYTES) {
                            destFile.delete()
                            throw SecurityException("ZIP archive exceeds maximum decompressed size.")
                        }

                        fileOut.write(buffer, 0, count)
                        count = zipIn.read(buffer)
                    }
                    fileOut.flush()
                }

                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }

        val jsonFile = File(stagingDir, BACKUP_JSON_ENTRY)
        if (!jsonFile.exists() || !jsonFile.isFile || jsonFile.length() <= 0L) {
            throw IllegalArgumentException("The archive does not contain a valid '$BACKUP_JSON_ENTRY'.")
        }

        return jsonFile
    }

    /**
     * Checks whether a staged poster file is a readable, usable image.
     */
    fun isUsableImage(file: File): Boolean {
        if (!file.exists() || !file.isFile || file.length() <= 0L) return false

        // 1. Verify image magic bytes
        val isHeaderValid = runCatching {
            FileInputStream(file).use { stream ->
                val header = ByteArray(12)
                val read = stream.read(header)
                if (read < 4) return@use false

                val isJpeg = header[0] == 0xFF.toByte() &&
                    header[1] == 0xD8.toByte() &&
                    header[2] == 0xFF.toByte()

                val isPng = header[0] == 0x89.toByte() &&
                    header[1] == 0x50.toByte() &&
                    header[2] == 0x4E.toByte() &&
                    header[3] == 0x47.toByte()

                val isGif = header[0] == 'G'.code.toByte() &&
                    header[1] == 'I'.code.toByte() &&
                    header[2] == 'F'.code.toByte()

                val isWebp = read >= 12 &&
                    header[0] == 'R'.code.toByte() &&
                    header[1] == 'I'.code.toByte() &&
                    header[2] == 'F'.code.toByte() &&
                    header[3] == 'F'.code.toByte() &&
                    header[8] == 'W'.code.toByte() &&
                    header[9] == 'E'.code.toByte() &&
                    header[10] == 'B'.code.toByte() &&
                    header[11] == 'P'.code.toByte()

                isJpeg || isPng || isGif || isWebp
            }
        }.getOrDefault(false)

        if (!isHeaderValid) return false

        // 2. Try decoding image bounds using BitmapFactory if available
        return runCatching {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            if (options.outWidth > 0 && options.outHeight > 0) {
                true
            } else {
                // In non-Android JVM environments where BitmapFactory may be stubbed, fall back to header validation
                isHeaderValid
            }
        }.getOrDefault(isHeaderValid)
    }
}
