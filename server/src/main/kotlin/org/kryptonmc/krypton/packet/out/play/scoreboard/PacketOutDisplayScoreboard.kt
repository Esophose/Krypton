package org.kryptonmc.krypton.packet.out.play.scoreboard

import io.netty.buffer.ByteBuf
import org.kryptonmc.krypton.extension.writeString
import org.kryptonmc.krypton.packet.state.PlayPacket
import org.kryptonmc.krypton.world.scoreboard.KryptonScoreboard
import org.kryptonmc.krypton.api.world.scoreboard.ScoreboardPosition
import org.kryptonmc.krypton.api.world.scoreboard.Team

class PacketOutDisplayScoreboard(
    private val scoreboard: KryptonScoreboard,
    private val team: Team? = null // used for team specific positioning
) : PlayPacket(0x43) {

    override fun write(buf: ByteBuf) {
        when (scoreboard.position) {
            ScoreboardPosition.TEAM_SPECIFIC -> {
                if (team == null) throw IllegalArgumentException("Team must be supplied if position is team specific!")
                buf.writeByte(3 + team.color.id)
            }
            else -> buf.writeByte(scoreboard.position.id)
        }
        buf.writeString(scoreboard.name, 16)
    }
}