val ktorVersion: String by project
val logbackVersion: String by project
val logbackJsonClassicVersion: String by project
val logbackJacksonVersion: String by project
val exposedVersion: String by project
val hikariVersion: String by project
val postgresqlDriverVersion: String by project
val awsSdkVersion: String by project
val spaceSdkVersion: String by project
val kotlinxSerializationVersion: String by project
val kotlinxHtmlJvmVersion: String by project
val kotlinxCoroutinesSlf4jVersion: String by project
val nimbusVersion: String by project
val neo4jOgm: String by project

val spaceUsername: String? by extra
val spacePassword: String? by extra

plugins {
    application
    kotlin("jvm") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    kotlin("plugin.noarg") version "1.7.20"
    id("docker-compose")
    id("io.ktor.plugin") version "2.1.3"
}

noArg {
annotations("org.neo4j.ogm.annotation.NodeEntity", "org.neo4j.ogm.annotation.RelationshipEntity")
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://maven.pkg.jetbrains.space/public/p/space/maven")
}

val neo4jOgmVersion = "3.2.38"
dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-locations-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    implementation("org.jetbrains:space-sdk-jvm:$spaceSdkVersion")

    implementation("com.nimbusds:nimbus-jose-jwt:$nimbusVersion")

    implementation("org.neo4j:neo4j-ogm-core:$neo4jOgmVersion")
    implementation("org.neo4j:neo4j-ogm-http-driver:$neo4jOgmVersion")
    implementation("org.neo4j:neo4j-ogm-bolt-driver:$neo4jOgmVersion")
    implementation("org.neo4j:neo4j-ogm-embedded-driver:$neo4jOgmVersion")

    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.19.0")
    implementation(platform("com.google.cloud:libraries-bom:26.1.3"))

    implementation("com.google.cloud:google-cloud-secretmanager")

    testImplementation(kotlin("test"))
}

kotlin.sourceSets.all {
    languageSettings {
        optIn("kotlin.time.ExperimentalTime")
        optIn("io.ktor.server.locations.KtorExperimentalLocationsAPI")
        optIn("space.jetbrains.api.ExperimentalSpaceSdkApi")
    }
}

sourceSets {
    main {
        resources {
            srcDirs("build/client")
        }
    }
}

dockerCompose {
    projectName = "voyager"
    removeContainers = false
    removeVolumes = false
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("com.achugr.voyager.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("voyager.jar")
    }
    application {
        mainClass.set("com.achugr.voyager.ApplicationKt")
    }
}

//this is to make it possible to have few main classes in the app
jib {
    container {
        mainClass = "com.achugr.voyager.ApplicationKt"
    }
}