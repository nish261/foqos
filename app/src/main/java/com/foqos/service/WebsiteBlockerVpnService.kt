package com.foqos.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.foqos.data.repository.SessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.InetAddress
import java.nio.ByteBuffer
import javax.inject.Inject

/**
 * VPN Service for blocking distracting websites.
 * 
 * This service creates a local VPN connection to filter DNS requests
 * and HTTP/HTTPS traffic to block configured domains.
 * 
 * NOTE: User must approve VPN connection via system dialog
 */
@AndroidEntryPoint
class WebsiteBlockerVpnService : VpnService() {
    
    @Inject
    lateinit var sessionRepository: SessionRepository
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var blockedDomains: Set<String> = emptySet()
    private var isRunning = false
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_VPN -> startVpn()
            ACTION_STOP_VPN -> stopVpn()
            else -> {
                // Monitor active session
                observeActiveSession()
            }
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
        serviceScope.cancel()
    }
    
    /**
     * Observe active session and update blocked domains.
     */
    private fun observeActiveSession() {
        serviceScope.launch {
            sessionRepository.getActiveSessionFlow().collect { session ->
                if (session != null && session.blockedDomains.isNotEmpty()) {
                    blockedDomains = session.blockedDomains.toSet()
                    if (!isRunning) {
                        startVpn()
                    }
                } else {
                    if (isRunning) {
                        stopVpn()
                    }
                }
            }
        }
    }
    
    /**
     * Start VPN connection.
     */
    private fun startVpn() {
        // Stop existing connection if any
        vpnInterface?.close()
        
        try {
            val builder = Builder()
                .addAddress("10.0.0.2", 24)
                .addRoute("0.0.0.0", 0)
                .addDnsServer("1.1.1.1") // Cloudflare DNS
                .setSession("Foqos Website Blocker")
                .setBlocking(false)
            
            vpnInterface = builder.establish()
            isRunning = true
            
            // Start packet processing
            processPackets()
        } catch (e: Exception) {
            isRunning = false
        }
    }
    
    /**
     * Stop VPN connection.
     */
    private fun stopVpn() {
        vpnInterface?.close()
        vpnInterface = null
        isRunning = false
    }
    
    /**
     * Process VPN packets and filter blocked domains.
     * 
     * NOTE: This is a simplified implementation.
     * A production app would need more sophisticated packet parsing.
     */
    private fun processPackets() {
        serviceScope.launch(Dispatchers.IO) {
            val vpn = vpnInterface ?: return@launch
            val inputStream = FileInputStream(vpn.fileDescriptor)
            val outputStream = FileOutputStream(vpn.fileDescriptor)
            
            val buffer = ByteBuffer.allocate(32767)
            
            try {
                while (isRunning) {
                    val length = inputStream.read(buffer.array())
                    if (length > 0) {
                        buffer.limit(length)
                        
                        // Parse packet and check if domain is blocked
                        val shouldBlock = shouldBlockPacket(buffer)
                        
                        if (!shouldBlock) {
                            // Forward packet
                            outputStream.write(buffer.array(), 0, length)
                        }
                        // If blocked, just drop the packet
                        
                        buffer.clear()
                    }
                }
            } catch (e: Exception) {
                // VPN closed or error occurred
            }
        }
    }
    
    /**
     * Check if packet should be blocked based on domain rules.
     * Uses Deep Packet Inspection for DNS and HTTPS/SNI.
     */
    private fun shouldBlockPacket(packet: ByteBuffer): Boolean {
        try {
            // Parse IP header
            val ipInfo = com.foqos.util.PacketInspector.parseIPHeader(packet) ?: return false
            
            // Check DNS queries
            if (ipInfo.destPort == 53) {
                val domain = com.foqos.util.PacketInspector.parseDNSQuery(packet, ipInfo)
                if (domain != null && com.foqos.util.PacketInspector.isDomainBlocked(domain, blockedDomains)) {
                    return true
                }
            }
            
            // Check HTTPS/TLS SNI for encrypted traffic
            if (ipInfo.destPort == 443) {
                val sni = com.foqos.util.PacketInspector.extractSNI(packet, ipInfo)
                if (sni != null && com.foqos.util.PacketInspector.isDomainBlocked(sni, blockedDomains)) {
                    return true
                }
            }
            
            // Check HTTP Host header (port 80)
            if (ipInfo.destPort == 80) {
                // Could parse HTTP headers here for completeness
                // Most traffic is HTTPS now, so DNS + SNI covers most cases
            }
            
            return false
        } catch (e: Exception) {
            return false
        }
    }
    
    companion object {
        const val ACTION_START_VPN = "com.foqos.START_VPN"
        const val ACTION_STOP_VPN = "com.foqos.STOP_VPN"
        
        fun startVpn(service: android.content.Context) {
            val intent = Intent(service, WebsiteBlockerVpnService::class.java).apply {
                action = ACTION_START_VPN
            }
            service.startService(intent)
        }
        
        fun stopVpn(service: android.content.Context) {
            val intent = Intent(service, WebsiteBlockerVpnService::class.java).apply {
                action = ACTION_STOP_VPN
            }
            service.startService(intent)
        }
    }
}
