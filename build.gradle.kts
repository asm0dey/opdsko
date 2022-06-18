import org.jetbrains.kotlin.cli.jvm.main
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Logging

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val tinylog_version: String by project

plugins {
    java
    application
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.serialization") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("nu.studer.jooq") version "7.1.1"
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
    implementation("org.redundent:kotlin-xml-builder:1.7.4")
    implementation("com.sun.xml.bind:jaxb-impl:2.3.6")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    jooqGenerator("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("org.jsoup:jsoup:1.15.1")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.tinylog:tinylog-api-kotlin:$tinylog_version")
    implementation("org.tinylog:slf4j-tinylog:$tinylog_version")
    implementation("org.tinylog:tinylog-impl:$tinylog_version")
    implementation("io.github.pdvrieze.xmlutil:serialization-jvm:0.84.2")
    implementation("io.github.pdvrieze.xmlutil:ktor:0.84.2")
    implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("org.flywaydb:flyway-core:8.5.13")


}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/kotlin")
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

jooq {
    configurations {
        create("main") {
            jooqConfiguration.apply {

                logging = Logging.WARN
                jdbc.apply {
                    url = "jdbc:sqlite:referencedb/opds.db"
                }
                generator.apply {
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                        isJavaTimeTypes = true
                        isImmutableInterfaces = true
                        isDaos = true
                    }
                    target.apply {
                        packageName = "io.github.asm0dey.opdsko.jooq"
                        directory = "src/main/java"
                    }
                    database.apply {
                        forcedTypes.addAll(
                            listOf(
                                ForcedType().apply {
                                    name = "TIMESTAMP"
                                    includeExpression = ".*\\.added"
                                },
                                ForcedType().apply {
                                    name = "BIGINT"
                                    includeExpression = ".*\\.id"
                                },
                                ForcedType().apply {
                                    name = "BIGINT"
                                    includeExpression = ".*\\..*_id"
                                },
                            )
                        )
                        excludes = "gen_.*"
                        isIncludeExcludeColumns = true
                    }
                }
            }
        }
    }
}
