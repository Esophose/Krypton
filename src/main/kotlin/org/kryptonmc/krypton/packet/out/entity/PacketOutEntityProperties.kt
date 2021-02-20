package org.kryptonmc.krypton.packet.out.entity

import io.netty.buffer.ByteBuf
import org.kryptonmc.krypton.entity.Attribute
import org.kryptonmc.krypton.extension.writeString
import org.kryptonmc.krypton.extension.writeUUID
import org.kryptonmc.krypton.extension.writeVarInt
import org.kryptonmc.krypton.packet.state.PlayPacket

class PacketOutEntityProperties(
    val entityId: Int,
    val properties: List<Attribute> = emptyList()
) : PlayPacket(0x58) {

    override fun write(buf: ByteBuf) {
        buf.writeVarInt(entityId)
        buf.writeInt(properties.size)

        properties.forEach { property ->
            buf.writeString(property.key.key.toString())
            buf.writeDouble(property.value)

            buf.writeVarInt(property.modifiers.size)
            property.modifiers.forEach {
                buf.writeUUID(it.uuid)
                buf.writeDouble(it.amount)
                buf.writeByte(it.operation.id)
            }
        }
    }
}