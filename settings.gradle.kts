pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
    
}
rootProject.name = "bebroo"


include(":app")
include(":shared")
include(":server")
include(":web")

