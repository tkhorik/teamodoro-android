package com.teamodoro.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "room_config")
data class RoomConfigEntity(
    @PrimaryKey val id: Int = 1,
    val roomId: String = "default",
    val roomOffset: Long = 0L
)
