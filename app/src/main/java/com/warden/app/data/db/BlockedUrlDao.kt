package com.warden.app.data.db

import androidx.room.*
import com.warden.app.data.model.BlockedUrl
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedUrlDao {

    @Query("SELECT * FROM blocked_urls ORDER BY domain ASC")
    fun getAllUrls(): Flow<List<BlockedUrl>>

    @Query("SELECT * FROM blocked_urls ORDER BY domain ASC")
    suspend fun getAllUrlsList(): List<BlockedUrl>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(url: BlockedUrl)

    @Delete
    suspend fun delete(url: BlockedUrl)

    @Query("DELETE FROM blocked_urls WHERE domain = :domain")
    suspend fun deleteByDomain(domain: String)

    @Query("SELECT COUNT(*) FROM blocked_urls WHERE :url LIKE '%' || domain || '%'")
    suspend fun isUrlBlocked(url: String): Int
}
