package com.foqos.nfc

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for reading NFC tags.
 * Extracts the tag ID which is used to identify and match tags.
 */
@Singleton
class NFCReader @Inject constructor() {
    
    companion object {
        private const val TAG = "NFCReader"
    }
    
    /**
     * Extract the unique ID from an NFC tag.
     * This ID is used to match tags for starting/stopping sessions.
     */
    fun getTagId(tag: Tag): String {
        val id = tag.id
        return bytesToHex(id)
    }
    
    /**
     * Get tag technologies supported by this tag.
     */
    fun getTagTechnologies(tag: Tag): List<String> {
        return tag.techList.toList()
    }
    
    /**
     * Check if tag is writable (has NDEF or can be formatted).
     */
    suspend fun isWritable(tag: Tag): Boolean = withContext(Dispatchers.IO) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                val isWritable = ndef.isWritable
                ndef.close()
                return@withContext isWritable
            }
            
            // Check if can be formatted
            val ndefFormatable = NdefFormatable.get(tag)
            if (ndefFormatable != null) {
                ndefFormatable.close()
                return@withContext true
            }
            
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if tag is writable", e)
            false
        }
    }
    
    /**
     * Convert byte array to hex string for tag ID.
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789ABCDEF"
        val result = StringBuilder(bytes.size * 2)
        bytes.forEach {
            val octet = it.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(hexChars[firstIndex])
            result.append(hexChars[secondIndex])
        }
        return result.toString()
    }
    
    /**
     * Check if discovered tag matches any configured tags and return the mode.
     */
    fun checkTagMode(
        tag: Tag,
        configuredTags: List<com.foqos.domain.model.NFCTagConfig>
    ): com.foqos.domain.model.NFCTagMode? {
        val tagId = getTagId(tag)
        val matchingTag = configuredTags.firstOrNull { it.tagId == tagId }
        return matchingTag?.mode
    }
    
    /**
     * Get tag info including technology support.
     */
    fun getTagInfo(tag: Tag): NFCTagInfo {
        val tagId = getTagId(tag)
        val technologies = tag.techList.map { it.substringAfterLast('.') }
        
        return NFCTagInfo(
            id = tagId,
            technologies = technologies,
            isNdefSupported = tag.techList.contains("android.nfc.tech.Ndef")
        )
    }
    
    data class NFCTagInfo(
        val id: String,
        val technologies: List<String>,
        val isNdefSupported: Boolean
    )
}
