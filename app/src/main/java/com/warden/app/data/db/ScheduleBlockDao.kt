package com.warden.app.data.db

import androidx.room.*
import com.warden.app.data.model.ScheduleBlock
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleBlockDao {

    @Query("SELECT * FROM schedule_blocks ORDER BY dayOfWeek, startMinute")
    fun getAllBlocks(): Flow<List<ScheduleBlock>>

    @Query("SELECT * FROM schedule_blocks ORDER BY dayOfWeek, startMinute")
    suspend fun getAllBlocksList(): List<ScheduleBlock>

    @Query("SELECT * FROM schedule_blocks WHERE dayOfWeek = :day ORDER BY startMinute")
    suspend fun getBlocksForDay(day: Int): List<ScheduleBlock>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(block: ScheduleBlock)

    @Delete
    suspend fun delete(block: ScheduleBlock)

    @Query("DELETE FROM schedule_blocks WHERE dayOfWeek = :day")
    suspend fun deleteForDay(day: Int)

    @Query("DELETE FROM schedule_blocks")
    suspend fun deleteAll()
}
