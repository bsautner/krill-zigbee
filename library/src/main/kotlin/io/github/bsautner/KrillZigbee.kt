package com.github.bsautner.utils.io.github.bsautner

import com.zsmartsystems.zigbee.*
import com.zsmartsystems.zigbee.app.basic.ZigBeeBasicServerExtension
import com.zsmartsystems.zigbee.app.discovery.ZigBeeDiscoveryExtension
import com.zsmartsystems.zigbee.app.iasclient.ZigBeeIasCieExtension
import com.zsmartsystems.zigbee.app.otaserver.ZigBeeOtaUpgradeExtension
import com.zsmartsystems.zigbee.database.ZigBeeNetworkDataStore
import com.zsmartsystems.zigbee.dongle.ember.ZigBeeDongleEzsp
import com.zsmartsystems.zigbee.dongle.ember.ezsp.structure.EzspConfigId
import com.zsmartsystems.zigbee.security.ZigBeeKey
import com.zsmartsystems.zigbee.serial.ZigBeeSerialPort
import com.zsmartsystems.zigbee.serialization.DefaultDeserializer
import com.zsmartsystems.zigbee.serialization.DefaultSerializer
import com.zsmartsystems.zigbee.transport.*
import com.zsmartsystems.zigbee.transport.ZigBeePort.FlowControl
import com.zsmartsystems.zigbee.zcl.clusters.*
import com.zsmartsystems.zigbee.zcl.clusters.onoff.OnCommand
import com.zsmartsystems.zigbee.zcl.clusters.onoff.OffCommand
import com.zsmartsystems.zigbee.zdo.field.NodeDescriptor
import io.github.bsautner.EmberNcpHardwareReset
import io.github.bsautner.ZigBeeDataStore
import java.util.*
import javax.security.auth.callback.Callback

object ZigbeeManager {
    lateinit var networkManager: ZigBeeNetworkManager

    val transportOptions: TransportConfig = TransportConfig()
    fun start(
        serialPortName: String,
        serialBaud: Int,
        dongleName: DongleName,
        power: Int,
        callback: (ZigBeeCommand) -> Unit
    ) {
        val defaultProfileId = ZigBeeProfileType.ZIGBEE_HOME_AUTOMATION.key

        val flowControl = when (dongleName) {
            DongleName.EMBER -> FlowControl.FLOWCONTROL_OUT_RTSCTS

            else -> {
                FlowControl.FLOWCONTROL_OUT_NONE
            }
        }
        val serialPort: ZigBeePort = ZigBeeSerialPort(serialPortName, serialBaud, flowControl)
        val dongle: ZigBeeTransportTransmit = when (dongleName) {
            DongleName.EMBER -> {
                val emberDongle = ZigBeeDongleEzsp(serialPort)
                emberDongle.updateDefaultConfiguration(EzspConfigId.EZSP_CONFIG_ADDRESS_TABLE_SIZE, 16)
                emberDongle.updateDefaultConfiguration(EzspConfigId.EZSP_CONFIG_SOURCE_ROUTE_TABLE_SIZE, 100)
                emberDongle.updateDefaultConfiguration(EzspConfigId.EZSP_CONFIG_APS_UNICAST_MESSAGE_COUNT, 16)
                emberDongle.updateDefaultConfiguration(EzspConfigId.EZSP_CONFIG_NEIGHBOR_TABLE_SIZE, 24)
                transportOptions.addOption(TransportConfigOption.RADIO_TX_POWER, power)
                // Configure the concentrator
                // Max Hops defaults to system max
                val concentratorConfig = ConcentratorConfig()
                concentratorConfig.type = ConcentratorType.HIGH_RAM
                concentratorConfig.maxFailures = 8
                concentratorConfig.maxHops = 0
                concentratorConfig.refreshMinimum = 60
                concentratorConfig.refreshMaximum = 3600
                transportOptions.addOption(TransportConfigOption.CONCENTRATOR_CONFIG, concentratorConfig)
                emberDongle.setEmberNcpResetProvider(EmberNcpHardwareReset())
                emberDongle
            }

            DongleName.CC2531 -> TODO("not implemented yet")
            DongleName.TELEGESIS -> TODO("not implemented yet")
            DongleName.CONBEE -> TODO("not implemented yet")
            DongleName.XBEE -> TODO("not implemented yet")
        }

        val dataStore: ZigBeeNetworkDataStore = ZigBeeDataStore(dongleName.name)

        networkManager = ZigBeeNetworkManager(dongle)
        networkManager.apply {
            setNetworkDataStore(dataStore)
            setSerializer(DefaultSerializer::class.java, DefaultDeserializer::class.java)
            addCommandListener(CommandListener(callback))
            addNetworkStateListener(NetworkStateListener())
            addAnnounceListener(AnnounceListener())
            addNetworkNodeListener(NodeListener())
            addSupportedClientCluster(ZclBasicCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclBasicCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclIdentifyCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclGroupsCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclPowerConfigurationCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclScenesCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclPollControlCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclOnOffCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclLevelControlCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclColorControlCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclPressureMeasurementCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclTemperatureMeasurementCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclIasZoneCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclIasAceCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclThermostatCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclWindowCoveringCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclMeteringCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclElectricalMeasurementCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclDiagnosticsCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclPowerConfigurationCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclBallastConfigurationCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclOtaUpgradeCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclBinaryInputBasicCluster.CLUSTER_ID)
            addSupportedClientCluster(ZclBallastConfigurationCluster.CLUSTER_ID)
            addSupportedClientCluster(1000)
            addSupportedClientCluster(0xff17)


            addSupportedServerCluster(ZclBasicCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclIdentifyCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclGroupsCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclScenesCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclPowerConfigurationCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclPollControlCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclOnOffCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclLevelControlCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclColorControlCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclPressureMeasurementCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclOtaUpgradeCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclMeteringCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclIasAceCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclElectricalMeasurementCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclDiagnosticsCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclBallastConfigurationCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclTemperatureMeasurementCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclIasZoneCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclWindowCoveringCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclBinaryInputBasicCluster.CLUSTER_ID)
            addSupportedServerCluster(ZclBallastConfigurationCluster.CLUSTER_ID)
            addSupportedServerCluster(1000)
            addSupportedServerCluster(0xff17)
        }


        // Initialise the network
        val initResponse = networkManager.initialize()
        println("networkManager.initialize returned $initResponse")

        println("PAN ID          = " + networkManager.zigBeePanId)
        println("Extended PAN ID = " + networkManager.zigBeeExtendedPanId)
        println("Channel         = " + networkManager.zigBeeChannel)

        networkManager.setDefaultProfileId(defaultProfileId)
        transportOptions.addOption(TransportConfigOption.TRUST_CENTRE_JOIN_MODE, TrustCentreJoinMode.TC_JOIN_SECURE)
        // Add the default ZigBeeAlliance09 HA link key
        transportOptions.addOption(
            TransportConfigOption.TRUST_CENTRE_LINK_KEY, ZigBeeKey(
                intArrayOf(
                    0x5A, 0x69,
                    0x67, 0x42, 0x65, 0x65, 0x41, 0x6C, 0x6C, 0x69, 0x61, 0x6E, 0x63, 0x65, 0x30, 0x39
                )
            )
        )
        // transportOptions.addOption(TransportConfigOption.TRUST_CENTRE_LINK_KEY, new ZigBeeKey(new int[] { 0x41, 0x61,
        // 0x8F, 0xC0, 0xC8, 0x3B, 0x0E, 0x14, 0xA5, 0x89, 0x95, 0x4B, 0x16, 0xE3, 0x14, 0x66 }));
        dongle.updateTransportConfig(transportOptions)
        // Add the extensions to the network
        networkManager.addExtension(ZigBeeIasCieExtension())
        networkManager.addExtension(ZigBeeOtaUpgradeExtension())
        networkManager.addExtension(ZigBeeBasicServerExtension())

        val discoveryExtension = ZigBeeDiscoveryExtension()
        discoveryExtension.setUpdateMeshPeriod(0)
        discoveryExtension.setUpdateOnChange(false)
        networkManager.addExtension(discoveryExtension)
        networkManager.startup(false)

        //callback(initResponse)
        //startPairing()

    }



    fun startPairing() {

        networkManager.permitJoin(120)
        println("pairing start ${networkManager.networkState}")
    }

    /**
     * Turn on a smart plug by network address
     * @param networkAddress The network address of the device (e.g., 0x0FF9)
     * @param endpoint The endpoint ID (usually 1 for smart plugs)
     * @return true if command was sent successfully
     */
    fun turnOn(networkAddress: Int, endpoint: Int = 1): Boolean {
        try {
            val node = networkManager.getNode(networkAddress)
            if (node == null) {
                println("Node with address $networkAddress not found")
                return false
            }

            val zigbeeEndpoint = node.getEndpoint(endpoint)
            if (zigbeeEndpoint == null) {
                println("Endpoint $endpoint not found on node $networkAddress")
                return false
            }

            // Try input cluster first (most common for smart plugs)
            var onOffCluster = zigbeeEndpoint.getInputCluster(ZclOnOffCluster.CLUSTER_ID) as? ZclOnOffCluster
            if (onOffCluster == null) {
                // Fallback to output cluster
                onOffCluster = zigbeeEndpoint.getOutputCluster(ZclOnOffCluster.CLUSTER_ID) as? ZclOnOffCluster
            }

            if (onOffCluster == null) {
                println("OnOff cluster not found on node $networkAddress endpoint $endpoint")
                return false
            }

            val command = OnCommand()
            val future = onOffCluster.sendCommand(command)
            val response = future.get()

            println("Turn ON command sent to $networkAddress:$endpoint - Response: $response")
            return response != null
        } catch (e: Exception) {
            println("Error turning on device $networkAddress:$endpoint - ${e.message}")
            return false
        }
    }

    /**
     * Turn off a smart plug by network address
     * @param networkAddress The network address of the device (e.g., 0x0FF9)
     * @param endpoint The endpoint ID (usually 1 for smart plugs)
     * @return true if command was sent successfully
     */
    fun turnOff(networkAddress: Int, endpoint: Int = 1): Boolean {
        try {
            val node = networkManager.getNode(networkAddress)
            if (node == null) {
                println("Node with address $networkAddress not found")
                return false
            }

            val zigbeeEndpoint = node.getEndpoint(endpoint)
            if (zigbeeEndpoint == null) {
                println("Endpoint $endpoint not found on node $networkAddress")
                return false
            }

            // Try input cluster first (most common for smart plugs)
            var onOffCluster = zigbeeEndpoint.getInputCluster(ZclOnOffCluster.CLUSTER_ID) as? ZclOnOffCluster
            if (onOffCluster == null) {
                // Fallback to output cluster
                onOffCluster = zigbeeEndpoint.getOutputCluster(ZclOnOffCluster.CLUSTER_ID) as? ZclOnOffCluster
            }

            if (onOffCluster == null) {
                println("OnOff cluster not found on node $networkAddress endpoint $endpoint")
                return false
            }

            val command = OffCommand()
            val future = onOffCluster.sendCommand(command)
            val response = future.get()

            println("Turn OFF command sent to $networkAddress:$endpoint - Response: $response")
            return response != null
        } catch (e: Exception) {
            println("Error turning off device $networkAddress:$endpoint - ${e.message}")
            return false
        }
    }

    /**
     * Convenience function to turn on device by hex address string
     * @param hexAddress Hex address as string (e.g., "0FF9")
     * @param endpoint The endpoint ID (usually 1 for smart plugs)
     */
    fun turnOnByHex(hexAddress: String, endpoint: Int = 1): Boolean {
        val networkAddress = hexAddress.toInt(16)
        return turnOn(networkAddress, endpoint)
    }

    /**
     * Convenience function to turn off device by hex address string
     * @param hexAddress Hex address as string (e.g., "0FF9")
     * @param endpoint The endpoint ID (usually 1 for smart plugs)
     */
    fun turnOffByHex(hexAddress: String, endpoint: Int = 1): Boolean {
        val networkAddress = hexAddress.toInt(16)
        return turnOff(networkAddress, endpoint)
    }

    fun listNodes() {
        val nodes = networkManager.nodes
        val nodeIds = mutableListOf<Int>()

        for (node in nodes) {
            nodeIds.add(node.networkAddress)
        }

        val tableHeader = String.format(
            "%-7s  %-4s  %-16s  %-12s  %-10s  %-3s  %-25s  %-35s  %-20s  %-15s",
            "Network", "Addr", "IEEE Address", "Logical Type", "State", "EP", "Profile", "Device Type",
            "Manufacturer", "Model"
        )

        println("${networkManager.networkState} Total known nodes in network: " + nodes.size)
        println(tableHeader)

        for (nodeId in nodeIds) {
            printNode(networkManager.getNode(nodeId))
        }
    }

    private fun printNode(node: ZigBeeNode) {
        val nodeInfo = String.format(
            "%7d  %04X  %-16s  %-12s  %-10s", node.getNetworkAddress(),
            node.getNetworkAddress(), node.getIeeeAddress(), node.getLogicalType(), node.getNodeState()
        )
        val nodeInfoPadding = String.format("%7s  %4s  %16s  %12s  %10s", "", "", "", "", "")

        val endpoints: MutableList<ZigBeeEndpoint> = ArrayList<ZigBeeEndpoint>(node.getEndpoints())
        Collections.sort(
            endpoints,
            Comparator { ep1: ZigBeeEndpoint?, ep2: ZigBeeEndpoint? -> ep1!!.getEndpointId() - ep2!!.getEndpointId() })

        var first = true
        for (endpoint in endpoints) {
            val profileType: String?
            if (ZigBeeProfileType.getByValue(endpoint.getProfileId()) == null) {
                profileType = String.format("%04X", endpoint.getProfileId())
            } else {
                profileType = ZigBeeProfileType.getByValue(endpoint.getProfileId()).toString()
            }
            val deviceType: String?
            if (ZigBeeDeviceType.getByValue(endpoint.getDeviceId()) == null
                || ZigBeeProfileType.getByValue(endpoint.getProfileId()) == null
            ) {
                deviceType = String.format("%04X", endpoint.getDeviceId())
            } else {
                deviceType = ZigBeeDeviceType
                    .getByValue(ZigBeeProfileType.getByValue(endpoint.getProfileId()), endpoint.getDeviceId())
                    .toString()
            }
            val showManufacturerAndModel = endpoint.getParentNode().getNetworkAddress() != 0
            val endpointInfo = String.format(
                "%3d  %-25s  %-35s  %-20s  %-15s", endpoint.getEndpointId(),
                profileType, deviceType, if (showManufacturerAndModel) getManufacturer(endpoint) else "",
                if (showManufacturerAndModel) getModel(endpoint) else ""
            )

            val tableLine = String.format("%s %s", if (first) nodeInfo else nodeInfoPadding, endpointInfo)
            println(tableLine)

            first = false
        }

        // Print the node information if there are no known endpoints
        if (first) {
            println(nodeInfo)
        }
    }

    private fun getManufacturer(endpoint: ZigBeeEndpoint): String? {
        val cluster = getBasicCluster(endpoint)
        val attribute = if (cluster != null) cluster.getAttribute(ZclBasicCluster.ATTR_MANUFACTURERNAME) else null
        val lastValue = if (attribute != null) attribute.getLastValue() else null
        return if (lastValue != null) lastValue.toString() else ""
    }

    private fun getModel(endpoint: ZigBeeEndpoint): String? {
        val cluster = getBasicCluster(endpoint)
        val attribute = if (cluster != null) cluster.getAttribute(ZclBasicCluster.ATTR_MODELIDENTIFIER) else null
        val lastValue = if (attribute != null) attribute.getLastValue() else null
        return if (lastValue != null) lastValue.toString() else ""
    }

    private fun getBasicCluster(endpoint: ZigBeeEndpoint): ZclBasicCluster? {
        val cluster = endpoint.getInputCluster(0)
        if (cluster is ZclBasicCluster) {
            return cluster
        } else {
            return null
        }
    }
}

class NetworkStateListener : ZigBeeNetworkStateListener {
    override fun networkStateUpdated(state: ZigBeeNetworkState?) {
        println("Network state updated $state")
    }

}

class AnnounceListener : ZigBeeAnnounceListener {
    override fun deviceStatusUpdate(
        deviceStatus: ZigBeeNodeStatus?,
        networkAddress: Int?,
        ieeeAddress: IeeeAddress?
    ) {
        println("Device status update: $deviceStatus for device $ieeeAddress at network address $networkAddress")
    }


}


class CommandListener(val callback: (ZigBeeCommand) -> Unit) : ZigBeeCommandListener {
    override fun commandReceived(command: ZigBeeCommand?) {
        command?.let {
            println("ZigBeeCommand received $command")
            callback(command)
        }

        //ZigbeeManager.listNodes()
    }


}


class NodeListener : ZigBeeNetworkNodeListener {
    override fun nodeAdded(node: ZigBeeNode) {
        println("NEW NODE ADDED: ${node.ieeeAddress} at network address ${node.networkAddress}")
        ZigbeeManager.listNodes()
    }

    override fun nodeRemoved(node: ZigBeeNode) {
        println("NODE REMOVED: ${node.ieeeAddress}")
    }
}
