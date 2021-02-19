package me.bristermitten.minekraft.extension

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.bardy.komponent.Component
import me.bristermitten.minekraft.entity.*
import me.bristermitten.minekraft.entity.cardinal.Angle
import me.bristermitten.minekraft.entity.cardinal.Direction
import me.bristermitten.minekraft.entity.cardinal.Rotation
import me.bristermitten.minekraft.entity.cardinal.Position
import me.bristermitten.minekraft.entity.entities.villager.VillagerData
import me.bristermitten.minekraft.entity.entities.villager.VillagerProfession
import me.bristermitten.minekraft.entity.entities.villager.VillagerType
import me.bristermitten.minekraft.entity.metadata.MetadataType
import me.bristermitten.minekraft.entity.metadata.Pose
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*
import kotlin.experimental.and

// Allows us to write a byte without having to convert it to an integer every time
fun ByteBuf.writeByte(byte: Byte) {
    writeByte(byte.toInt())
}

fun ByteBuf.writeUByte(byte: UByte) {
    writeByte(byte.toInt())
}

fun ByteBuf.writeShort(short: Short) {
    writeShort(short.toInt())
}

fun ByteBuf.readVarInt(): Int {
    var numRead = 0
    var result = 0
    var read: Byte
    do {
        read = readByte()
        val value = (read and 127).toInt()
        result = result or (value shl 7 * numRead)
        numRead++
        if (numRead > 5) {
            throw RuntimeException("VarInt is too big")
        }
    } while (read and 128.toByte() != 0.toByte())

    return result
}

fun ByteBuf.writeVarInt(varInt: Int) {
    var i = varInt
    while (i and -128 != 0) {
        writeByte(i and 127 or 128)
        i = i ushr 7
    }

    writeByte(i)
}

fun ByteBuf.readString(maxLength: Short = Short.MAX_VALUE): String {
    val length = this.readVarInt()

    return when {
        length > maxLength * 4 -> {
            throw IOException("The received encoded string buffer length is longer than maximum allowed (" + length + " > " + maxLength * 4 + ")")
        }
        length < 0 -> {
            throw IOException("The received encoded string buffer length is less than zero! Weird string!")
        }
        else -> {
            val array = ByteArray(length)
            this.readBytes(array)
            val s = String(array, UTF_8)
            if (s.length > maxLength) {
                throw IOException("The received string length is longer than maximum allowed ($length > $maxLength)")
            } else {
                s
            }
        }
    }
}

fun ByteBuf.writeString(s: String, maxLength: Short = Short.MAX_VALUE) {
    val bytes = s.toByteArray(Charsets.UTF_8)
    if (bytes.size > maxLength) {
        throw EncoderException("String too big (was " + bytes.size + " bytes encoded, max " + maxLength + ")")
    } else {
        writeVarInt(bytes.size)
        writeBytes(bytes)
    }
}

fun ByteBuf.readVarIntByteArray(): ByteArray {
    val length = readVarInt()
    val readable = readableBytes()
    if (length > readable) {
        throw DecoderException("Not enough bytes to read. Expected Length: $length, actual length: $readable")
    }
    val bytes = ByteArray(length)
    readBytes(bytes)
    return bytes
}

fun ByteBuf.readAllAvailableBytes(): ByteArray {
    val length = readableBytes()
    return readBytes(length).array()
}


fun ByteBuf.writeUUID(uuid: UUID) {
    writeLong(uuid.mostSignificantBits)
    writeLong(uuid.leastSignificantBits)
}

fun ByteBuf.writeOptionalUUID(uuid: UUID?) {
    if (uuid != null) {
        writeBoolean(true)
        writeUUID(uuid)
    } else {
        writeBoolean(false)
    }
}

fun ByteBuf.writeNBTCompound(tag: CompoundBinaryTag) {
    val outputStream = ByteArrayOutputStream()
    BinaryTagIO.writer().write(tag, outputStream)
    writeBytes(outputStream.toByteArray())
}

fun ByteBuf.writeLongs(array: LongArray) {
    for (element in array) writeLong(element)
}

fun ByteBuf.writeChat(component: Component) {
    writeString(Json {}.encodeToString(component))
}

fun ByteBuf.writeOptionalChat(component: Component?) {
    if (component != null) {
        writeBoolean(true)
        writeChat(component)
    } else {
        writeBoolean(false)
    }
}

fun ByteBuf.writeSlot(slot: Slot) {
    writeBoolean(slot.isPresent)
    if (slot.isPresent) {
        writeVarInt(slot.itemId)
        writeByte(slot.itemCount)
        writeNBTCompound(slot.nbt)
    }
}

fun ByteBuf.writeRotation(rotation: Rotation) {
    writeFloat(rotation.x)
    writeFloat(rotation.y)
    writeFloat(rotation.z)
}

fun ByteBuf.writePosition(position: Position) {
    writeLong(position.toProtocol())
}

fun ByteBuf.writeOptionalPosition(position: Position?) {
    if (position != null) {
        writeBoolean(true)
        writePosition(position)
    } else {
        writeBoolean(false)
    }
}

fun ByteBuf.writeParticle(particle: Particle) {
    writeVarInt(particle.type.id)

    when (particle.type) {
        ParticleType.BLOCK -> writeVarInt((particle as BlockParticle).state)
        ParticleType.DUST -> {
            particle as DustParticle
            writeFloat(particle.red)
            writeFloat(particle.green)
            writeFloat(particle.blue)
            writeFloat(particle.scale)
        }
        ParticleType.FALLING_DUST -> writeVarInt((particle as FallingDustParticle).state)
        ParticleType.ITEM -> writeSlot((particle as ItemParticle).slot)
        else -> Unit
    }
}

fun ByteBuf.writeVillagerData(data: VillagerData) {
    writeVarInt(data.type.id)
    writeVarInt(data.profession.id)
    writeVarInt(data.level)
}

fun ByteBuf.writeAngle(angle: Angle) {
    writeUByte(angle.value)
}

private fun UUID.toIntArray(): IntArray {
    val mostSig = mostSignificantBits
    val leastSig = leastSignificantBits
    return intArrayOf((mostSig shr 32).toInt(), mostSig.toInt(), (leastSig shr 32).toInt(), leastSig.toInt())
}