package com.warden.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.warden.app.data.model.BlockedApp
import com.warden.app.data.model.BlockedUrl
import com.warden.app.data.model.ScheduleBlock

@Database(
    entities = [BlockedApp::class, BlockedUrl::class, ScheduleBlock::class],
    version = 1,
    exportSchema = false
)
abstract class WardenDatabase : RoomDatabase() {

    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun blockedUrlDao(): BlockedUrlDao
    abstract fun scheduleBlockDao(): ScheduleBlockDao

    companion object {
        @Volatile
        private var INSTANCE: WardenDatabase? = null

        fun getInstance(context: Context): WardenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WardenDatabase::class.java,
                    "warden_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
