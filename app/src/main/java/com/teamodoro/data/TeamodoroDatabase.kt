package com.teamodoro.data
import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = [RoomConfigEntity::class], version = 1, exportSchema = false)
abstract class TeamodoroDatabase : RoomDatabase() {
    abstract fun roomDao(): RoomDao
}
