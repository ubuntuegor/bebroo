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
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.20.2")

    val composeVersion: String by project
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.animation:animation:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    implementation("androidx.navigation:navigation-compose:2.4.0-beta02")

    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "to.bnt.draw.app"
        minSdk = 24
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        val composeVersion: String by project
        kotlinCompilerExtensionVersion = composeVersion
    }
}