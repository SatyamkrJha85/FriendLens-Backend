plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("io.ktor.plugin") version "3.4.0"
}

group = "com.friendlens"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

dependencies {

    // --- Ktor Core ---
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-config-yaml")

    // --- HTTP Features ---
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-compression")
    implementation("io.ktor:ktor-server-caching-headers")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-forwarded-header")

    // --- Security ---
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")

    // --- API Docs ---
    implementation("io.ktor:ktor-openapi-schema")
    implementation("io.ktor:ktor-server-openapi")
    implementation("io.ktor:ktor-server-swagger")

    // --- Caching ---
    implementation("com.ucasoft.ktor:ktor-simple-cache:0.57.7")
    implementation("com.ucasoft.ktor:ktor-simple-memory-cache:0.57.7")
    implementation("com.ucasoft.ktor:ktor-simple-redis-cache:0.57.7")

    // --- AsyncAPI (exclude logback) ---
    implementation("org.openfolder:kotlin-asyncapi-ktor:3.1.3") {
        exclude(group = "ch.qos.logback")
    }

    // --- Logging ---
    implementation("ch.qos.logback:logback-classic:1.5.16")

    // --- Serialization ---
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-content-negotiation")

    // --- Database (Exposed + Postgres + HikariCP) ---
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.jetbrains.exposed:exposed-core:0.55.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.55.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.55.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.55.0")

    // --- AWS S3 & Dotenv ---
    implementation("software.amazon.awssdk:s3:2.25.11")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // --- Testing ---
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation(kotlin("test"))
}
