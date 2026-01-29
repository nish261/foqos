package com.foqos.data.local.dao

import androidx.room.*
import com.foqos.data.local.entity.BlockedProfileSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM blocked_sessions WHERE profileId = :profileId ORDER BY startTime DESC")
    fun getSessionsForProfile(profileId: String): Flow<List<BlockedProfileSessionEntity>>
    
    @Query("SELECT * FROM blocked_sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(): BlockedProfileSessionEntity?
    
    @Query("SELECT * FROM blocked_sessions WHERE endTime IS NULL")
    fun getActiveSessionFlow(): Flow<BlockedProfileSessionEntity?>
    
    @Query("SELECT * FROM blocked_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): BlockedProfileSessionEntity?
    
    @Query("SELECT * FROM blocked_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 50): Flow<List<BlockedProfileSessionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: BlockedProfileSessionEntity)
    
    @Update
    suspend fun updateSession(session: BlockedProfileSessionEntity)
    
    @Delete
    suspend fun deleteSession(session: BlockedProfileSessionEntity)
    
    @Query("DELETE FROM blocked_sessions WHERE profileId = :profileId")
    suspend fun deleteSessionsForProfile(profileId: String)
}
