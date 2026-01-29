package com.foqos.data.local.dao

import androidx.room.*
import com.foqos.data.local.entity.BlockedProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedProfileDao {
    @Query("SELECT * FROM blocked_profiles ORDER BY `order` ASC, createdAt DESC")
    fun getAllProfiles(): Flow<List<BlockedProfileEntity>>
    
    @Query("SELECT * FROM blocked_profiles WHERE id = :id")
    suspend fun getProfileById(id: String): BlockedProfileEntity?
    
    @Query("SELECT * FROM blocked_profiles ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getMostRecentlyUpdated(): BlockedProfileEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BlockedProfileEntity)
    
    @Update
    suspend fun updateProfile(profile: BlockedProfileEntity)
    
    @Delete
    suspend fun deleteProfile(profile: BlockedProfileEntity)
    
    @Query("DELETE FROM blocked_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: String)
    
    @Query("UPDATE blocked_profiles SET `order` = :order WHERE id = :id")
    suspend fun updateOrder(id: String, order: Int)
}
