/**
 * Copyright (c) 2016-2024 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.github.bsautner

import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter
import com.thoughtworks.xstream.io.xml.StaxDriver
import com.zsmartsystems.zigbee.IeeeAddress
import com.zsmartsystems.zigbee.database.*
import com.zsmartsystems.zigbee.security.ZigBeeKey
import com.zsmartsystems.zigbee.zdo.field.BindingTable
import com.zsmartsystems.zigbee.zdo.field.NodeDescriptor.*
import com.zsmartsystems.zigbee.zdo.field.PowerDescriptor.PowerSourceType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import java.util.*

/**
 * Serializes and deserializes the ZigBee network state.
 *
 * @author Chris Jackson
 */
class ZigBeeDataStore(networkId: String) : ZigBeeNetworkDataStore {
    private val networkId: String

    init {
        this.networkId = DATABASE + networkId + "/"
        var file: File?

        file = File(this.networkId + "/" + KEYSTORE)
        if (!file.exists() && !file.mkdirs()) {
            logger.error("Error creating network database folder {}", file)
        }
        file = File(DATABASE + BACKUP)
        if (!file.exists() && !file.mkdirs()) {
            logger.error("Error creating network backup folder {}", file)
        }
    }

    private fun openStream(): XStream? {
        try {
            val stream = XStream(StaxDriver())
            XStream.setupDefaultSecurity(stream)
            stream.alias("ZigBeeKey", ZigBeeKey::class.java)
            stream.alias("ZigBeeNode", ZigBeeNodeDao::class.java)
            stream.alias("ZigBeeEndpoint", ZigBeeEndpointDao::class.java)
            stream.alias("ZclCluster", ZclClusterDao::class.java)
            stream.alias("ZclAttribute", ZclAttributeDao::class.java)
            stream.alias("MacCapabilitiesType", MacCapabilitiesType::class.java)
            stream.alias("ServerCapabilitiesType", ServerCapabilitiesType::class.java)
            stream.alias("PowerSourceType", PowerSourceType::class.java)
            stream.alias("FrequencyBandType", FrequencyBandType::class.java)
            stream.alias("BindingTable", BindingTable::class.java)
            stream.alias("IeeeAddress", IeeeAddress::class.java)
            stream.registerConverter(IeeeAddressConverter())

            // stream.registerLocalConverter(ZigBeeKey.class, "key", new KeyArrayConverter());
            // stream.registerLocalConverter(ZigBeeKey.class, "address", new IeeeAddressConverter());
            // stream.registerLocalConverter(BindingTable.class, "srcAddr", new IeeeAddressConverter());
            // stream.registerLocalConverter(BindingTable.class, "dstAddr", new IeeeAddressConverter());
            stream.allowTypesByWildcard(
                arrayOf<String>(
                    "com.zsmartsystems.zigbee.**"
                )
            )

            return stream
        } catch (e: Exception) {
            logger.debug("Error opening XStream ", e)
            return null
        }
    }

    private fun getFile(address: IeeeAddress?): File {
        return File(networkId + address + ".xml")
    }

    private fun getFile(uuid: UUID?): File {
        return File(DATABASE + BACKUP + "/" + uuid + ".xml")
    }

    private fun getFile(key: String?): File {
        return File(networkId + KEYSTORE + "/" + key + ".xml")
    }

    override fun readNetworkNodes(): MutableSet<IeeeAddress?> {
        val nodes: MutableSet<IeeeAddress?> = HashSet<IeeeAddress?>()
        val dir = File(networkId)
        val files = dir.listFiles()

        if (files == null) {
            return nodes
        }

        for (file in files) {
            if (!file.getName().lowercase(Locale.getDefault()).endsWith(".xml")) {
                continue
            }

            try {
                val address = IeeeAddress(file.getName().substring(0, 16))
                nodes.add(address)
            } catch (e: IllegalArgumentException) {
                logger.error("Error parsing database filename: {}", file.getName())
            }
        }

        return nodes
    }

    override fun readNode(address: IeeeAddress?): ZigBeeNodeDao? {
        val stream = openStream()
        val file = getFile(address)

        var node: ZigBeeNodeDao? = null
        try {
            BufferedReader(InputStreamReader(FileInputStream(file), CHARSET)).use { reader ->
                node = stream!!.fromXML(reader) as ZigBeeNodeDao?
                reader.close()
            }
        } catch (e: Exception) {
            logger.error("{}: Error reading network state: ", address, e)
        }

        return node
    }

    override fun writeNode(node: ZigBeeNodeDao) {
        val stream = openStream()
        val file = getFile(node.getIeeeAddress())

        try {
            BufferedWriter(OutputStreamWriter(FileOutputStream(file), CHARSET)).use { writer ->
                stream!!.marshal(node, PrettyPrintWriter(writer))
                writer.close()
            }
        } catch (e: Exception) {
            logger.error("{}: Error writing network state: ", node.getIeeeAddress(), e)
        }
    }

    override fun removeNode(address: IeeeAddress?) {
        val file = getFile(address)
        if (!file.delete()) {
            logger.error("{}: Error removing network state", address)
        }
    }

    override fun writeObject(key: String?, `object`: Any?) {
        val stream = openStream()
        val file = getFile(key)

        try {
            BufferedWriter(OutputStreamWriter(FileOutputStream(file), CHARSET)).use { writer ->
                stream!!.marshal(`object`, PrettyPrintWriter(writer))
                writer.close()
            }
        } catch (e: Exception) {
            logger.error("{}: Error writing key: ", key, e)
        }
    }

    override fun readObject(key: String?): Any? {
        return null
    }

    override fun writeBackup(backup: ZigBeeNetworkBackupDao): Boolean {
        val stream = openStream()
        val file = getFile(backup.getUuid())

        try {
            BufferedWriter(OutputStreamWriter(FileOutputStream(file), CHARSET)).use { writer ->
                stream!!.marshal(backup, PrettyPrintWriter(writer))
                writer.close()
            }
        } catch (e: Exception) {
            logger.error("{}: Error writing network backup: ", backup.getUuid(), e)
            return false
        }

        return true
    }

    override fun readBackup(uuid: UUID?): ZigBeeNetworkBackupDao? {
        val stream = openStream()
        val file = getFile(uuid)

        var backup: ZigBeeNetworkBackupDao? = null
        try {
            BufferedReader(InputStreamReader(FileInputStream(file), CHARSET)).use { reader ->
                backup = stream!!.fromXML(reader) as ZigBeeNetworkBackupDao?
                reader.close()
            }
        } catch (e: Exception) {
            logger.error("{}: Error reading network backup: ", uuid, e)
        }

        return backup
    }

    override fun listBackups(): MutableSet<ZigBeeNetworkBackupDao?> {
        val backups: MutableSet<ZigBeeNetworkBackupDao?> = HashSet<ZigBeeNetworkBackupDao?>()
        val dir: File = File(DATABASE + BACKUP)
        val files = dir.listFiles()

        if (files == null) {
            return backups
        }

        for (file in files) {
            if (!file.getName().lowercase(Locale.getDefault()).endsWith(".xml")) {
                continue
            }

            try {
                val filename = file.getName()
                val uuid = UUID.fromString(filename.substring(0, filename.length - 4))
                val backup = readBackup(uuid)
                for (node in backup!!.getNodes()) {
                    node.setEndpoints(null)
                    node.setBindingTable(null)
                }
                backups.add(backup)
            } catch (e: IllegalArgumentException) {
                logger.error("Error parsing database filename: {}", file.getName())
            }
        }

        return backups
    }

    companion object {
        /**
         * The logger.
         */
        private val logger: Logger = LoggerFactory.getLogger(ZigBeeDataStore::class.java)

        private const val CHARSET = "UTF-8"
        private const val DATABASE = "database1/"
        private const val KEYSTORE = "keystore"
        private const val BACKUP = "backup"
    }
}
