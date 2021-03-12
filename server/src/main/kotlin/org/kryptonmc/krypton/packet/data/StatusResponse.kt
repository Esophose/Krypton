package org.kryptonmc.krypton.packet.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.kryptonmc.krypton.serializers.ComponentSerializer
import org.kryptonmc.krypton.serializers.UUIDSerializer
import java.util.*

@Serializable
data class StatusResponse(
    val version: ServerVersion,
    val players: Players,
    @Serializable(with = ComponentSerializer::class) val description: Component
)

@Serializable
data class ServerVersion(
    val name: String,
    val protocol: Int
)

@Serializable
data class Players(
    val max: Int,
    val online: Int,
    val sample: Set<PlayerInfo> = emptySet()
)

@Serializable
data class PlayerInfo(
    val name: String,
    @SerialName("id") @Serializable(with = UUIDSerializer::class) val uuid: UUID
)