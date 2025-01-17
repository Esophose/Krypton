package org.kryptonmc.krypton.packet.out.play

import io.netty.buffer.ByteBuf
import org.kryptonmc.krypton.api.registry.NamespacedKey
import org.kryptonmc.krypton.extension.writeString
import org.kryptonmc.krypton.extension.writeVarInt
import org.kryptonmc.krypton.packet.state.PlayPacket
import org.kryptonmc.krypton.registry.*
import org.kryptonmc.krypton.registry.tags.Tag
import org.kryptonmc.krypton.registry.tags.TagManager

class PacketOutTags(
    private val registryManager: RegistryManager,
    private val tagManager: TagManager
) : PlayPacket(0x5B) {

    override fun write(buf: ByteBuf) {
        buf.writeVarInt(tagManager.blockTags.size) // number of block tags
        buf.writeBlockTags() // block tags

        buf.writeVarInt(tagManager.entityTypeTags.size) // number of entity tags
        buf.writeEntityTags()

        buf.writeVarInt(tagManager.fluidTags.size) // number of fluid tags
        buf.writeFluidTags()

        buf.writeVarInt(tagManager.itemTags.size)
        buf.writeItemTags()
    }

    private fun ByteBuf.writeBlockTags() {
        val blockRegistry = registryManager.registries.blocks
        for (tag in tagManager.blockTags) {
            writeString(tag.name)
            writeVarInt(tag.data.values.size)

            writeTags(tagManager.getDeepTags(tag), blockRegistry)
        }
    }

    private fun ByteBuf.writeEntityTags() {
        val entityTypeRegistry = registryManager.registries.entityTypes
        for (tag in tagManager.entityTypeTags) {
            writeString(tag.name)
            writeVarInt(tag.data.values.size)

            writeTags(tagManager.getDeepTags(tag), entityTypeRegistry)
        }
    }

    private fun ByteBuf.writeFluidTags() {
        val fluidRegistry = registryManager.registries.fluids
        for (tag in tagManager.fluidTags) {
            writeString(tag.name)
            writeVarInt(tag.data.values.size)

            writeTags(tagManager.getDeepTags(tag), fluidRegistry)
        }
    }

    private fun ByteBuf.writeItemTags() {
        val itemRegistry = registryManager.registries.items
        for (tag in tagManager.itemTags) {
            writeString(tag.name)
            writeVarInt(tag.data.values.size)

            writeTags(tagManager.getDeepTags(tag), itemRegistry)
        }
    }

    private fun ByteBuf.writeTags(tags: List<Tag>, registry: Registry) {
        tags.forEach { writeVarInt(registry.entries.getValue(NamespacedKey(value = it.name)).protocolId) }
    }

    private fun ByteBuf.writeTags(tags: List<Tag>, registry: DefaultedRegistry) =
        writeTags(tags, Registry(registry.protocolId, registry.entries))
}