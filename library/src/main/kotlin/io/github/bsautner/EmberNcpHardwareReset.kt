package io.github.bsautner

import com.zsmartsystems.zigbee.dongle.ember.EmberNcpResetProvider
import com.zsmartsystems.zigbee.serial.ZigBeeSerialPort
import com.zsmartsystems.zigbee.transport.ZigBeePort

class EmberNcpHardwareReset : EmberNcpResetProvider {
    override fun emberNcpReset(port: ZigBeePort?) {
        val serialPort = port as ZigBeeSerialPort

        try {
            serialPort.setRts(false)
            serialPort.setDtr(false)
            Thread.sleep(50)
            serialPort.setRts(true)
            serialPort.setDtr(true)
        } catch (e: InterruptedException) {
        }
    }
}