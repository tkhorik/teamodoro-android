package com.teamodoro.di

import android.content.Context
import androidx.room.Room
import com.teamodoro.data.RoomDao
import com.teamodoro.data.TeamodoroDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TeamodoroDatabase =
        Room.databaseBuilder(context, TeamodoroDatabase::class.java, "teamodoro.db")
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()

    @Provides
    fun provideRoomDao(db: TeamodoroDatabase): RoomDao = db.roomDao()
}
