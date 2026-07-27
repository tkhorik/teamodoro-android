package com.teamodoro.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepository @Inject constructor(
    private val dao: RoomDao,
) {
    val roomOffset: Flow<Long> = dao.getRoomConfigFlow().map { it?.roomOffset ?: 0L }

    val roomId: Flow<String?> = dao.getRoomConfigFlow().map { it?.roomId }

    suspend fun getRoomOffset(): Long = dao.getRoomConfig()?.roomOffset ?: 0L

    suspend fun saveRoomConfig(roomId: String, offsetMillis: Long) {
        dao.saveRoomConfig(RoomConfigEntity(roomId = roomId, roomOffset = offsetMillis))
    }

    suspend fun updateOffset(offsetMillis: Long) {
        ensureConfigExists()
        dao.updateOffset(offsetMillis)
    }

    suspend fun updateRoomId(roomId: String) {
        ensureConfigExists()
        dao.updateRoomId(roomId)
    }

    private suspend fun ensureConfigExists() {
        if (dao.getRoomConfig() == null) {
            dao.saveRoomConfig(RoomConfigEntity())
        }
    }
}
