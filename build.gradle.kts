val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    java
    application
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"
    id("com.squareup.sqldelight") version "1.5.3"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.github.asm0dey"
version = "0.0.1"
application {
    mainClass.set("io.github.asm0dey.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-compression-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-id-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-resources:$ktor_version")
    implementation("io.ktor:ktor-server-metrics-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.redundent:kotlin-xml-builder:1.7.4")
    implementation("com.github.ajalt.clikt:clikt:3.4.2")
    implementation("com.sun.xml.bind:jaxb-impl:2.3.6")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("org.jsoup:jsoup:1.15.1")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.squareup.sqldelight:sqlite-driver:1.5.3")
    implementation("com.squareup.sqldelight:coroutines-extensions:1.5.3")
    implementation("org.tinylog:tinylog-api-kotlin:2.5.0-M2")
    implementation("org.tinylog:tinylog-impl:2.5.0-M2")

}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/kotlin")
    }
}

sqldelight {
    database("OpdsDb") {
        packageName = "opdsko.db"
        dialect = "sqlite:3.25"
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}



