import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    kotlin("plugin.serialization")
    `java-library`
    id("com.github.johnrengelman.shadow") version "6.1.0"
    application
    maven
}

application.mainClassName = "org.kryptonmc.krypton.KryptonKt"

dependencies {
    implementation(project(":krypton-api"))

    // Netty
    implementation("io.netty:netty-buffer:4.1.60.Final")
    implementation("io.netty:netty-handler:4.1.60.Final")
    implementation("io.netty:netty-transport:4.1.60.Final")

    // Netty native transport
    implementation("io.netty:netty-transport-native-epoll:4.1.60.Final")
    implementation("io.netty:netty-transport-native-kqueue:4.1.60.Final")
    implementation("io.netty.incubator:netty-incubator-transport-native-io_uring:0.0.4.Final")

    // Adventure
    implementation("net.kyori:adventure-text-serializer-gson:4.6.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.6.0")
    implementation("net.kyori:adventure-nbt:4.6.0")

    // Logging
    runtimeOnly("org.apache.logging.log4j:log4j-core:2.14.1")
    implementation("net.minecrell:terminalconsoleappender:1.2.0")
    runtimeOnly("org.jline:jline-terminal-jansi:3.19.0")

    // HTTP
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    // Caching
    implementation("com.github.ben-manes.caffeine:caffeine:2.8.8")
}

tasks {
    withType<ShadowJar> {
        archiveFileName.set("Krypton-${rootProject.extra["globalVersion"]}.jar")
        transform(Log4j2PluginsCacheFileTransformer::class.java)
    }
    withType<ProcessResources> {
        val tokens = mapOf("version" to rootProject.extra["globalVersion"])
        filter<ReplaceTokens>("tokens" to tokens)
    }
}