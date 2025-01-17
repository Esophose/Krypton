package org.kryptonmc.krypton.command.argument

import com.mojang.brigadier.arguments.*
import io.netty.buffer.ByteBuf
import org.kryptonmc.krypton.api.registry.NamespacedKey
import org.kryptonmc.krypton.api.registry.toNamespacedKey
import org.kryptonmc.krypton.command.argument.serializer.ArgumentSerializer
import org.kryptonmc.krypton.command.argument.serializer.EmptyArgumentSerializer
import org.kryptonmc.krypton.command.argument.serializer.brigadier.*
import org.kryptonmc.krypton.extension.logger
import org.kryptonmc.krypton.extension.writeKey
import kotlin.reflect.KClass

object ArgumentTypes {

    private val LOGGER = logger<ArgumentTypes>()
    private val BY_CLASS = mutableMapOf<KClass<*>, Entry<*>>()
    private val BY_NAME = mutableMapOf<NamespacedKey, Entry<*>>()

    init {
        // Brigadier types and serialisers
        register("brigadier:bool", BoolArgumentType::class, EmptyArgumentSerializer())
        register("brigadier:float", FloatArgumentType::class, FloatArgumentSerializer())
        register("brigadier:double", DoubleArgumentType::class, DoubleArgumentSerializer())
        register("brigadier:integer", IntegerArgumentType::class, IntegerArgumentSerializer())
        register("brigadier:long", LongArgumentType::class, LongArgumentSerializer())
        register("brigadier:string", StringArgumentType::class, StringArgumentSerializer())
    }

    operator fun get(key: NamespacedKey) = BY_NAME[key]

    @Suppress("UNCHECKED_CAST") // this should never fail
    operator fun <T : ArgumentType<*>> get(type: ArgumentType<*>) = BY_CLASS[type::class] as? Entry<T>

    fun <T : ArgumentType<*>> ByteBuf.writeArgumentType(argument: T) {
        val entry = get<T>(argument)
        if (entry == null) {
            LOGGER.error("Could not serialise $argument (Class ${argument::class})! Will not be sent to client!")
            writeKey(NamespacedKey(value = ""))
            return
        }
        writeKey(entry.name)
        entry.serializer.write(argument, this)
    }

    private fun <T : ArgumentType<*>> register(name: String, kClass: KClass<T>, serialiser: ArgumentSerializer<T>) {
        val key = name.toNamespacedKey()
        if (kClass in BY_CLASS) throw IllegalArgumentException("Class ${kClass.simpleName} already has a serialiser!")
        if (key in BY_NAME) throw IllegalArgumentException("'$name' is already a registered serialiser!")

        val entry = Entry(kClass, serialiser, key)
        BY_CLASS[kClass] = entry
        BY_NAME[key] = entry
    }

    data class Entry<T : ArgumentType<*>>(
        val kClass: KClass<T>,
        val serializer: ArgumentSerializer<T>,
        val name: NamespacedKey
    )
}