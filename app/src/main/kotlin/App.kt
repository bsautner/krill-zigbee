package com.github.bsautner.app

import com.github.bsautner.utils.Printer
import com.github.bsautner.utils.io.github.bsautner.DongleName

import com.github.bsautner.utils.io.github.bsautner.ZigbeeManager
import com.zsmartsystems.zigbee.ZigBeeStatus
import com.zsmartsystems.zigbee.transport.ZigBeePort.FlowControl
import com.zsmartsystems.zigbee.zcl.clusters.general.ReportAttributesCommand
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val port = "/dev/serial/by-id/usb-Silicon_Labs_HubZ_Smart_Home_Controller_51600441-if01-port0" //"ttyUSB2"

    runBlocking {
        val zigbeeManager = ZigbeeManager(port)

            zigbeeManager.start(

            serialBaud = 57600,
            dongleName = DongleName.EMBER,
            power = 8
        ) { command  ->
            println("Command Received: $command")
            when (command) {
                is ReportAttributesCommand -> println("*********Attributes received: $command")
            }
        }

        // Wait for network to stabilize
        delay(5000)

        // List all nodes to see your smart plug
        zigbeeManager.listNodes()

        // Example: Control your smart plug with network address 0x0FF9
        val smartPlugAddress = 0x0FF9



        while (true) {
            println("\n=== Testing Smart Plug Control ===")
            zigbeeManager.listNodes()
            // Turn ON the smart plug
            println("Turning ON smart plug...")
            val onResult = zigbeeManager.turnOn(smartPlugAddress)
            println("Turn ON result: $onResult")

            delay(3000) // Wait 3 seconds

            // Turn OFF the smart plug
            println("Turning OFF smart plug...")
            val offResult = zigbeeManager.turnOff(smartPlugAddress)
            println("Turn OFF result: $offResult")

            // Alternative: You can also use hex string addresses
            println("\nAlternative using hex string:")
            delay(2000)
            zigbeeManager.turnOnByHex("0FF9")
            delay(2000)
            zigbeeManager.turnOffByHex("0FF9")
        }
    }
}
