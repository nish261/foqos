package com.foqos.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility for writing data to NFC tags.
 * Writes profile information and deep links to tags.
 */
@Singleton
class NFCWriter @Inject constructor() {
    
    companion object {
        private const val TAG = "NFCWriter"
    }
    
    /**
     * Write profile deep link to NFC tag.
     * This allows the tag to open the profile when scanned.
     */
    suspend fun writeProfileToTag(
        tag: Tag,
        profileId: String
    ): WriteResult = withContext(Dispatchers.IO) {
        try {
            val deepLink = "https://foqos.app/profile/$profileId"
            val message = createNdefMessage(deepLink)
            
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                return@withContext writeToNdef(ndef, message)
            }
            
            val ndefFormatable = NdefFormatable.get(tag)
            if (ndefFormatable != null) {
                return@withContext formatAndWrite(ndefFormatable, message)
            }
            
            WriteResult.Error("Tag does not support NDEF")
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to tag", e)
            WriteResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Write to an NDEF-formatted tag.
     */
    private fun writeToNdef(ndef: Ndef, message: NdefMessage): WriteResult {
        return try {
            ndef.connect()
            
            if (!ndef.isWritable) {
                ndef.close()
                return WriteResult.Error("Tag is not writable")
            }
            
            val size = message.toByteArray().size
            val capacity = ndef.maxSize
            
            if (capacity < size) {
                ndef.close()
                return WriteResult.Error("Tag capacity is too small")
            }
            
            ndef.writeNdefMessage(message)
            ndef.close()
            WriteResult.Success
        } catch (e: IOException) {
            Log.e(TAG, "IOException writing to NDEF tag", e)
            WriteResult.Error("Failed to write to tag")
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to NDEF tag", e)
            WriteResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Format and write to an unformatted tag.
     */
    private fun formatAndWrite(
        ndefFormatable: NdefFormatable,
        message: NdefMessage
    ): WriteResult {
        return try {
            ndefFormatable.connect()
            ndefFormatable.format(message)
            ndefFormatable.close()
            WriteResult.Success
        } catch (e: IOException) {
            Log.e(TAG, "IOException formatting tag", e)
            WriteResult.Error("Failed to format tag")
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting tag", e)
            WriteResult.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Create NDEF message with URI record.
     */
    private fun createNdefMessage(uri: String): NdefMessage {
        val uriRecord = NdefRecord.createUri(uri)
        val appRecord = NdefRecord.createApplicationRecord("com.foqos")
        return NdefMessage(arrayOf(uriRecord, appRecord))
    }
    
    /**
     * Create NDEF message with text record.
     */
    private fun createTextMessage(text: String): NdefMessage {
        val textBytes = text.toByteArray(Charset.forName("UTF-8"))
        val textRecord = NdefRecord(
            NdefRecord.TNF_WELL_KNOWN,
            NdefRecord.RTD_TEXT,
            ByteArray(0),
            textBytes
        )
        return NdefMessage(arrayOf(textRecord))
    }
    
    sealed class WriteResult {
        object Success : WriteResult()
        data class Error(val message: String) : WriteResult()
    }
}
