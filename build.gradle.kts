plugins {
    kotlin("jvm") version "2.2.20"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "it.ethereallabs"
version = "1.0.2"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("libs/HytaleServer.jar"))
    implementation("org.yaml:snakeyaml:2.3")
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

tasks.shadowJar {
    archiveClassifier.set("")
}