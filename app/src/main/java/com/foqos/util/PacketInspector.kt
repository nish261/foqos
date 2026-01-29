package com.foqos.util

import java.net.InetAddress
import java.nio.ByteBuffer

/**
 * Utilities for parsing and inspecting network packets.
 * Used by VPN service for DNS and HTTP/HTTPS filtering.
 */
object PacketInspector {
    
    /**
     * Parse IP header to extract protocol and ports.
     */
    fun parseIPHeader(packet: ByteBuffer): IPPacketInfo? {
        return try {
            packet.position(0)
            
            // IP version and header length
            val versionAndLen = packet.get().toInt()
            val ipVersion = (versionAndLen shr 4) and 0xF
            if (ipVersion != 4) return null // Only IPv4 for now
            
            val headerLength = (versionAndLen and 0xF) * 4
            
            // Skip to protocol field (byte 9)
            packet.position(9)
            val protocol = packet.get().toInt() and 0xFF
            
            // Source and dest IP (bytes 12-19)
            packet.position(12)
            val sourceIP = ByteArray(4)
            packet.get(sourceIP)
            
            val destIP = ByteArray(4)
            packet.get(destIP)
            
            // Move to transport layer
            packet.position(headerLength)
            
            var sourcePort = 0
            var destPort = 0
            
            // TCP (6) or UDP (17)
            if (protocol == 6 || protocol == 17) {
                sourcePort = packet.short.toInt() and 0xFFFF
                destPort = packet.short.toInt() and 0xFFFF
            }
            
            IPPacketInfo(
                protocol = protocol,
                sourceIP = InetAddress.getByAddress(sourceIP).hostAddress ?: "",
                destIP = InetAddress.getByAddress(destIP).hostAddress ?: "",
                sourcePort = sourcePort,
                destPort = destPort
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if packet is a DNS query and extract domain name.
     */
    fun parseDNSQuery(packet: ByteBuffer, ipInfo: IPPacketInfo): String? {
        // DNS uses port 53
        if (ipInfo.destPort != 53) return null
        
        return try {
            // Skip UDP header (8 bytes) and DNS header (12 bytes)
            packet.position(packet.position() + 20)
            
            // Parse DNS question section
            val domain = StringBuilder()
            var labelLength = packet.get().toInt()
            
            while (labelLength > 0) {
                val label = ByteArray(labelLength)
                packet.get(label)
                domain.append(String(label))
                
                labelLength = packet.get().toInt()
                if (labelLength > 0) {
                    domain.append(".")
                }
            }
            
            domain.toString()
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Extract SNI (Server Name Indication) from TLS ClientHello.
     * Used for HTTPS domain inspection.
     */
    fun extractSNI(packet: ByteBuffer, ipInfo: IPPacketInfo): String? {
        // HTTPS uses port 443
        if (ipInfo.destPort != 443) return null
        
        return try {
            // Skip TCP header (assume 20 bytes for simplicity)
            packet.position(packet.position() + 20)
            
            // TLS Record Layer
            val contentType = packet.get().toInt() and 0xFF
            if (contentType != 22) return null // Not a handshake
            
            // Skip version and length
            packet.position(packet.position() + 4)
            
            // Handshake Type
            val handshakeType = packet.get().toInt() and 0xFF
            if (handshakeType != 1) return null // Not ClientHello
            
            // Skip handshake length, version, random (32 bytes)
            packet.position(packet.position() + 38)
            
            // Session ID length and skip it
            val sessionIdLength = packet.get().toInt() and 0xFF
            packet.position(packet.position() + sessionIdLength)
            
            // Cipher Suites length and skip it
            val cipherSuitesLength = packet.short.toInt() and 0xFFFF
            packet.position(packet.position() + cipherSuitesLength)
            
            // Compression Methods length and skip it
            val compMethodsLength = packet.get().toInt() and 0xFF
            packet.position(packet.position() + compMethodsLength)
            
            // Extensions
            val extensionsLength = packet.short.toInt() and 0xFFFF
            val extensionsEnd = packet.position() + extensionsLength
            
            // Look for SNI extension (type 0)
            while (packet.position() < extensionsEnd) {
                val extType = packet.short.toInt() and 0xFFFF
                val extLength = packet.short.toInt() and 0xFFFF
                
                if (extType == 0) { // SNI extension
                    packet.position(packet.position() + 3) // Skip list length and name type
                    val nameLength = packet.short.toInt() and 0xFFFF
                    val nameBytes = ByteArray(nameLength)
                    packet.get(nameBytes)
                    return String(nameBytes)
                } else {
                    packet.position(packet.position() + extLength)
                }
            }
            
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if domain matches any blocked domain patterns.
     */
    fun isDomainBlocked(domain: String, blockedDomains: Set<String>): Boolean {
        val normalizedDomain = domain.lowercase()
        
        return blockedDomains.any { blocked ->
            val normalizedBlocked = blocked.lowercase()
            // Exact match or subdomain match
            normalizedDomain == normalizedBlocked ||
                    normalizedDomain.endsWith(".$normalizedBlocked")
        }
    }
}

data class IPPacketInfo(
    val protocol: Int,
    val sourceIP: String,
    val destIP: String,
    val sourcePort: Int,
    val destPort: Int
)
