package org.kryptonmc.krypton.world

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.kryptonmc.krypton.config.WorldConfig
import org.kryptonmc.krypton.entity.Gamemode
import org.kryptonmc.krypton.extension.logger
import org.kryptonmc.krypton.registry.toNamespacedKey
import org.kryptonmc.krypton.space.Position
import org.kryptonmc.krypton.world.bossbar.Bossbar
import org.kryptonmc.krypton.world.dimension.Dimension
import org.kryptonmc.krypton.world.generation.Generator
import org.kryptonmc.krypton.world.generation.WorldGenerationSettings
import org.kryptonmc.krypton.world.generation.toGenerator
import java.io.File
import java.net.URI
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.system.exitProcess

@Suppress("MemberVisibilityCanBePrivate")
class WorldManager(config: WorldConfig) {

    val worlds = mutableListOf<World>()

    init {
        LOGGER.debug("Loading world ${config.name}...")
        val folder = File(Path.of("").toAbsolutePath().toFile(), config.name)
        if (!folder.exists()) {
            LOGGER.error("ERROR: World with name ${config.name} does not exist!")
            exitProcess(0)
        }

        worlds += loadWorld(File(folder, "level.dat"))
        LOGGER.debug("World loaded!")
    }

    fun loadWorld(file: File): World {
        val nbt = BinaryTagIO.unlimitedReader().read(file.toPath(), BinaryTagIO.Compression.GZIP).getCompound("Data")

        val bossbars = nbt.getCompound("CustomBossEvents").map { (key, bossbar) ->
            val nbtBossbar = bossbar as CompoundBinaryTag
            val players = nbtBossbar.getList("Players").map {
                val nbtPlayerUUID = it as CompoundBinaryTag
                UUID(nbtPlayerUUID.getLong("M"), nbtPlayerUUID.getLong("L"))
            }

            val flags = mutableSetOf<BossBar.Flag>()
            if (nbtBossbar.getBoolean("CreateWorldFog")) flags += BossBar.Flag.CREATE_WORLD_FOG
            if (nbtBossbar.getBoolean("DarkenScreen")) flags += BossBar.Flag.DARKEN_SCREEN
            if (nbtBossbar.getBoolean("PlayBossMusic")) flags += BossBar.Flag.PLAY_BOSS_MUSIC

            Bossbar(
                key.toNamespacedKey(),
                GsonComponentSerializer.gson().deserialize(nbtBossbar.getString("Name")),
                BossBar.Color.NAMES.value(nbtBossbar.getString("Color")) ?: BossBar.Color.WHITE,
                BossBar.Overlay.NAMES.value(nbtBossbar.getString("Overlay")) ?: BossBar.Overlay.PROGRESS,
                nbtBossbar.getInt("Value").toFloat() / nbtBossbar.getInt("Max").toFloat(),
                flags,
                nbtBossbar.getBoolean("Visible"),
                players
            )
        }

        val worldBorder = WorldBorder(
            nbt.getDouble("BorderSize"),
            nbt.getDouble("BorderCenterX"),
            nbt.getDouble("BorderCenterZ"),
            nbt.getDouble("BorderDamagePerBlock"),
            nbt.getDouble("BorderSafeZone"),
            nbt.getDouble("BorderSizeLerpTarget"),
            nbt.getLong("BorderSizeLerpTime"),
            nbt.getDouble("BorderWarningBlocks"),
            nbt.getDouble("BorderWarningTime")
        )

//        val endDimensionData = nbt.getCompound("DimensionData")
//            .getCompound("1")
//            .getCompound("DragonFight")
//            .let { dragonFight ->
//                val exitPortal = dragonFight.getCompound("ExitPortalLocation").let {
//                    BlockPosition(it.getByte("X").toInt(), it.getByte("Y").toInt(), it.getByte("Z").toInt())
//                }
//                val gateways = dragonFight.getList("Gateways").map { (it as IntBinaryTag).value() }
//                EndDimensionData(
//                    exitPortal,
//                    gateways,
//                    dragonFight.getBoolean("DragonKilled"),
//                    UUID(dragonFight.getLong("DragonUUIDMost"), dragonFight.getLong("DragonUUIDLeast")),
//                    dragonFight.getBoolean("PreviouslyKilled")
//                )
//            }

//        val gamerules = nbt.getCompound("GameRules").map { (key, value) ->
//            Gamerule(GameruleType.valueOf(key), value)
//        }

        val worldGenSettings = nbt.getCompound("WorldGenSettings").let { settings ->
            val dimensions = settings.getCompound("dimensions").associate { (key, value) ->
                key.toNamespacedKey() to (value as CompoundBinaryTag).let {
                    Dimension(it.getString("type").toNamespacedKey(), it.getCompound("generator").toGenerator())
                }
            }

            WorldGenerationSettings(
                settings.getLong("seed"),
                settings.getBoolean("generate_features"),
                dimensions
            )
        }

        val spawnLocation = Position(
            nbt.getInt("SpawnX"),
            nbt.getInt("SpawnY"),
            nbt.getInt("SpawnZ")
        )

        return World(
            nbt.getString("LevelName"),
            bossbars,
            nbt.getBoolean("allowCommands"),
            worldBorder,
            nbt.getInt("clearWeatherTime"),
            nbt.getLong("DayTime"),
            Difficulty.fromId(nbt.getByte("Difficulty").toInt()),
            nbt.getBoolean("DifficultyLocked"),
            worldGenSettings,
            Gamemode.fromId(nbt.getInt("GameType")),
            nbt.getBoolean("hardcore"),
            LocalDateTime.ofInstant(Instant.ofEpochMilli(nbt.getLong("LastPlayed")), ZoneOffset.UTC),
            nbt.getBoolean("raining"),
            nbt.getBoolean("MapFeatures"),
            nbt.getInt("rainTime"),
            nbt.getLong("RandomSeed"),
            spawnLocation,
            nbt.getBoolean("thundering"),
            nbt.getInt("thunderTime"),
            nbt.getLong("Time"),
            nbt.getInt("version"),
            nbt.getCompound("Version").let {
                WorldVersion(it.getInt("Id"), it.getString("Name"), it.getBoolean("Snapshot"))
            }
        )
    }

    companion object {

        private val LOGGER = logger<WorldManager>()
    }
}