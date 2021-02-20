package org.kryptonmc.krypton

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.kryptonmc.krypton.auth.GameProfile
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

object SessionStorage {

    val sessions: MutableSet<Session> = ConcurrentHashMap.newKeySet()

    val profiles: Cache<String, GameProfile> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofHours(6))
        .build()
}