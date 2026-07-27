package com.teamodoro.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
@Dao
interface RoomDao {
    @Query("SELECT * FROM room_config WHERE id = 1")
    fun getRoomConfigFlow(): Flow<RoomConfigEntity?>
    @Query("SELECT * FROM room_config WHERE id = 1")
    suspend fun getRoomConfig(): RoomConfigEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRoomConfig(config: RoomConfigEntity)
    @Query("UPDATE room_config SET roomOffset = :offset WHERE id = 1")
    suspend fun updateOffset(offset: Long)
    @Query("UPDATE room_config SET roomId = :roomId WHERE id = 1")
    suspend fun updateRoomId(roomId: String)
}
