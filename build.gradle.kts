plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "pl.szczerbal.myeconomy"

val targetVersion = project.properties["mcVersion"]?.toString() ?: "1.20.1"

val supportedVersions = mapOf(
    "1.20" to "1.20-R0.1-SNAPSHOT",
    "1.20.1" to "1.20.1-R0.1-SNAPSHOT",
    "1.20.2" to "1.20.2-R0.1-SNAPSHOT",
    "1.20.4" to "1.20.4-R0.1-SNAPSHOT",
    "1.20.6" to "1.20.6-R0.1-SNAPSHOT",
    "1.21" to "1.21.1-R0.1-SNAPSHOT",
    "1.21.1" to "1.21.1-R0.1-SNAPSHOT",
    "1.21.3" to "1.21.3-R0.1-SNAPSHOT",
    "1.21.4" to "1.21.4-R0.1-SNAPSHOT",
    "1.21.5" to "1.21.5-R0.1-SNAPSHOT"
)

val selectedApi = supportedVersions[targetVersion]
    ?: supportedVersions["1.20.1"]
    ?: error("Unsupported version: $targetVersion. Supported: ${supportedVersions.keys}")

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:$selectedApi")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    jar {
        archiveFileName.set("MyEconomy-${project.version}.jar")
    }

    processResources {
        val props = mapOf(
            "version" to project.version,
            "apiVersion" to "1.20"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    runServer {
        minecraftVersion(targetVersion)
        jvmArgs("-Xms2G", "-Xmx2G", "-XX:+UseG1GC")
    }
}
