package org.kryptonmc.krypton.channel

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import net.kyori.adventure.extra.kotlin.text
import net.kyori.adventure.extra.kotlin.translatable
import org.kryptonmc.krypton.*
import org.kryptonmc.krypton.extension.logger
import org.kryptonmc.krypton.packet.Packet
import org.kryptonmc.krypton.packet.out.login.PacketOutDisconnect
import org.kryptonmc.krypton.session.Session
import org.kryptonmc.krypton.session.SessionManager
import java.net.InetSocketAddress
import java.util.concurrent.TimeoutException

class ChannelHandler(private val sessionManager: SessionManager) : SimpleChannelInboundHandler<Packet>() {

    internal lateinit var session: Session

    override fun handlerAdded(ctx: ChannelHandlerContext) {
        LOGGER.debug("[+] Channel Connected: ${ctx.channel().remoteAddress()}")
        session = Session(ServerStorage.NEXT_ENTITY_ID.getAndIncrement(), ctx.channel())
        sessionManager.sessions += session
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) {
        LOGGER.debug("[-] Channel Disconnected: ${ctx.channel().remoteAddress()}")
        sessionManager.handleDisconnection(session)
        sessionManager.sessions -= session
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: Packet) {
        if (!ctx.channel().isOpen) return
        sessionManager.handle(session, msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (!ctx.channel().isOpen) return

        if (cause is TimeoutException) {
            val address = ctx.channel().remoteAddress() as InetSocketAddress
            LOGGER.debug("Connection from ${address.address}:${address.port} timed out. Reason: ", cause)
            session.sendPacket(PacketOutDisconnect(translatable { key("disconnect.timeout") }))
            session.disconnect()
        } else {
            val disconnectReason = translatable {
                key("disconnect.genericReason")
                args(text { content("Internal Exception: $cause") })
            }
            LOGGER.debug("Failed to send or received invalid packet! Cause: ", cause)
            session.sendPacket(PacketOutDisconnect(disconnectReason))
            ctx.channel().config().isAutoRead = false
        }
    }

    companion object {

        const val NETTY_NAME = "handler"

        private val LOGGER = logger<ChannelHandler>()
    }
}