plugins {
    kotlin("js")
}

group = "to.bnt.draw"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    js(LEGACY) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }
}