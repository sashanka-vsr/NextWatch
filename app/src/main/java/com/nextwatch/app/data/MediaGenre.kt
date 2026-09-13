package com.nextwatch.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "media_genres",
    primaryKeys = ["mediaId", "genre"],
    foreignKeys = [
        ForeignKey(
            entity = MediaItem::class,
            parentColumns = ["id"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("genre"),
    ],
)
data class MediaGenre(
    val mediaId: Long,
    val genre: String,
)