package com.foqos.data.repository

import com.foqos.data.local.dao.BlockedProfileDao
import com.foqos.data.local.entity.BlockedProfileEntity
import com.foqos.domain.model.NFCTagConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

    // NFC Tag Management
    suspend fun getNFCTags(profileId: String): List<NFCTagConfig> {
        val profile = getProfileById(profileId) ?: return emptyList()
        return profile.nfcTagsJson?.let {
            try {
                Json.decodeFromString<List<NFCTagConfig>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    suspend fun addNFCTag(profileId: String, tag: NFCTagConfig) {
        val profile = getProfileById(profileId) ?: return
        val currentTags = getNFCTags(profileId).toMutableList()
        currentTags.add(tag)
        val tagsJson = Json.encodeToString(currentTags)
        updateProfile(profile.copy(nfcTagsJson = tagsJson))
    }

    suspend fun removeNFCTag(profileId: String, tagId: String) {
        val profile = getProfileById(profileId) ?: return
        val currentTags = getNFCTags(profileId).filter { it.tagId != tagId }
        val tagsJson = if (currentTags.isEmpty()) null else Json.encodeToString(currentTags)
        updateProfile(profile.copy(nfcTagsJson = tagsJson))
    }
}
