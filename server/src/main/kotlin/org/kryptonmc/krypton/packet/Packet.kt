package org.kryptonmc.krypton.packet

import io.netty.buffer.ByteBuf
import org.kryptonmc.krypton.packet.state.PacketState

interface Packet {

    val info: PacketInfo

    fun read(buf: ByteBuf) {
        throw UnsupportedOperationException("$javaClass does not support reading")
    }

    fun write(buf: ByteBuf) {
        throw UnsupportedOperationException("$javaClass does not support writing")
    }
}

data class PacketInfo(val id: Int, val state: PacketState)