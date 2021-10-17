plugins {
    id("com.android.application")
    kotlin("android")
}

group = "to.bnt.draw"
version = "1.0-SNAPSHOT"

repositories {
    google()
}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "to.bnt.draw.app"
        minSdkVersion(24)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}