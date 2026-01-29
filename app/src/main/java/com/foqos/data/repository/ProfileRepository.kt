package com.foqos.data.repository

import com.foqos.data.local.dao.BlockedProfileDao
import com.foqos.data.local.entity.BlockedProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: BlockedProfileDao
) {
    fun getAllProfiles(): Flow<List<BlockedProfileEntity>> {
        return profileDao.getAllProfiles()
    }
    
    suspend fun getProfileById(id: String): BlockedProfileEntity? {
        return profileDao.getProfileById(id)
    }
    
    suspend fun getMostRecentlyUpdated(): BlockedProfileEntity? {
        return profileDao.getMostRecentlyUpdated()
    }
    
    suspend fun insertProfile(profile: BlockedProfileEntity) {
        profileDao.insertProfile(profile)
    }
    
    suspend fun updateProfile(profile: BlockedProfileEntity) {
        profileDao.updateProfile(profile.copy(updatedAt = System.currentTimeMillis()))
    }
    
    suspend fun deleteProfile(profile: BlockedProfileEntity) {
        profileDao.deleteProfile(profile)
    }
    
    suspend fun deleteProfileById(id: String) {
        profileDao.deleteProfileById(id)
    }
    
    suspend fun updateOrder(id: String, order: Int) {
        profileDao.updateOrder(id, order)
    }
    
    suspend fun createProfile(
        name: String,
        selectedApps: List<String> = emptyList(),
        blockingStrategyId: String = "nfc",
        strategyData: String? = null,
        domains: List<String>? = null
    ): BlockedProfileEntity {
        val profile = BlockedProfileEntity(
            name = name,
            selectedApps = selectedApps,
            blockingStrategyId = blockingStrategyId,
            strategyData = strategyData,
            domains = domains
        )
        profileDao.insertProfile(profile)
        return profile
    }
}
