package org.kryptonmc.krypton.world.generation

import net.kyori.adventure.nbt.CompoundBinaryTag
import org.kryptonmc.krypton.api.registry.NamespacedKey
import org.kryptonmc.krypton.api.registry.toNamespacedKey

abstract class Generator(val id: NamespacedKey)

object DebugGenerator : Generator(NamespacedKey(value = "debug"))

// TODO: Add support for generators when world generation exists
fun CompoundBinaryTag.toGenerator() = when (val type = getString("type").toNamespacedKey()) {
    NamespacedKey(value = "flat") -> FlatGenerator(FlatGeneratorSettings.fromNBT(getCompound("settings")))
    NamespacedKey(value = "noise") -> NoiseGenerator(
        getInt("seed"),
        getString("settings").toNamespacedKey(),
        BiomeGenerator.fromNBT(getCompound("biome_source"))
    )
    NamespacedKey(value = "debug") -> DebugGenerator
    else -> throw IllegalArgumentException("Unsupported generator type $type")
}