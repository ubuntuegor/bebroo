buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath("com.android.tools.build:gradle:4.2.2")
    }
}

group = "to.bnt.draw"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}