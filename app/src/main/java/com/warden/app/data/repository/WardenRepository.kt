package com.warden.app.data.repository

import android.content.Context
import com.warden.app.data.db.WardenDatabase
import com.warden.app.data.model.BlockedApp
import com.warden.app.data.model.BlockedUrl
import com.warden.app.data.model.ScheduleBlock
import kotlinx.coroutines.flow.Flow

class WardenRepository(context: Context) {

    private val db = WardenDatabase.getInstance(context)
    private val appDao = db.blockedAppDao()
    private val urlDao = db.blockedUrlDao()
    private val scheduleDao = db.scheduleBlockDao()

    // ---- App Blacklist ----
    fun getBlockedApps(): Flow<List<BlockedApp>> = appDao.getBlockedApps()
    fun getAllApps(): Flow<List<BlockedApp>> = appDao.getAllApps()
    suspend fun getBlockedAppsList(): List<BlockedApp> = appDao.getBlockedAppsList()
    suspend fun isAppBlocked(packageName: String): Boolean = appDao.isBlocked(packageName)
    suspend fun setAppBlocked(packageName: String, appName: String, blocked: Boolean) {
        appDao.insertOrUpdate(BlockedApp(packageName, appName, blocked))
    }
    suspend fun removeApp(packageName: String) = appDao.deleteByPackage(packageName)

    // ---- URL Blacklist ----
    fun getAllUrls(): Flow<List<BlockedUrl>> = urlDao.getAllUrls()
    suspend fun getAllUrlsList(): List<BlockedUrl> = urlDao.getAllUrlsList()
    suspend fun addUrl(domain: String) = urlDao.insert(BlockedUrl(domain = domain.trim().lowercase()))
    suspend fun removeUrl(url: BlockedUrl) = urlDao.delete(url)
    suspend fun isUrlBlocked(url: String): Boolean = urlDao.isUrlBlocked(url) > 0

    // ---- Schedule ----
    fun getAllScheduleBlocks(): Flow<List<ScheduleBlock>> = scheduleDao.getAllBlocks()
    suspend fun getAllScheduleBlocksList(): List<ScheduleBlock> = scheduleDao.getAllBlocksList()
    suspend fun saveScheduleBlock(block: ScheduleBlock) = scheduleDao.insert(block)
    suspend fun deleteScheduleBlock(block: ScheduleBlock) = scheduleDao.delete(block)
    suspend fun deleteScheduleForDay(day: Int) = scheduleDao.deleteForDay(day)
    suspend fun clearAllSchedule() = scheduleDao.deleteAll()
    suspend fun getBlocksForDay(day: Int): List<ScheduleBlock> = scheduleDao.getBlocksForDay(day)
}
