package org.kryptonmc.krypton.packet.out.play.entity

import io.netty.buffer.ByteBuf
import org.kryptonmc.krypton.extension.writeVarInt
import org.kryptonmc.krypton.packet.state.PlayPacket

class PacketOutEntityDestroy(private val entityIds: List<Int>) : PlayPacket(0x36) {

    override fun write(buf: ByteBuf) {
        buf.writeVarInt(entityIds.size)
        entityIds.forEach { buf.writeVarInt(it) }
    }
}