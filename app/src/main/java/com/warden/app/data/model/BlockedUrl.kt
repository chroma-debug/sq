package com.warden.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_urls")
data class BlockedUrl(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val domain: String
)
