pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }

    val kotlinVersion = "1.6.0"

    plugins {
        kotlin("android") version kotlinVersion
        kotlin("jvm") version kotlinVersion
        kotlin("js") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        kotlin("test") version kotlinVersion
    }
}
rootProject.name = "bebroo"

include(":app")
include(":shared")
include(":server")
include(":web")

