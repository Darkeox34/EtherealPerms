import java.util.Properties
import java.io.FileInputStream

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "it.ethereallabs"
version = "1.0.8"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    implementation("org.yaml:snakeyaml:2.3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    implementation("org.mongodb:mongodb-driver-sync:5.1.1")

    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
}

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
}


tasks.processResources {
    filesMatching("manifest.json") {
        expand(
            "version" to project.version,
            "name" to project.name
        )
    }
}

tasks.jar {
    archiveClassifier.set("plain")
}

val env = Properties().apply {
    val envFile = file(".env")
    if (envFile.exists()) {
        load(FileInputStream(envFile))
    }
}

val modsPath = env.getProperty("HYTALE_MODS_DIR") ?: "build/libs"

tasks.shadowJar {
    archiveClassifier.set("")
    destinationDirectory.set(file(modsPath))

    relocate("org.bson", "it.ethereallabs.internal.bson")
    relocate("com.mongodb", "it.ethereallabs.internal.mongodb")
}