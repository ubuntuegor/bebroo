import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "to.bnt.draw"
version = "1.0-SNAPSHOT"

dependencies {
    val ktorVersion: String by project
    val exposedVersion: String by project
    implementation(project(":shared"))
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-freemarker:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("ch.qos.logback:logback-classic:1.2.7")
    implementation("org.postgresql:postgresql:42.3.1")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("com.google.api-client:google-api-client:1.32.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks{
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "io.ktor.server.netty.EngineMain"))
        }
        val webProject = project(":web")
        val webpackTask = project.tasks.getByPath(":web:browserProductionWebpack") as org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
        dependsOn(webpackTask)
        from(File(webpackTask.destinationDirectory, webpackTask.outputFileName)) {
            into("assets")
        }
        from(webProject.fileTree("src/main/resources/assets")) {
            into("assets")
        }
    }
}
