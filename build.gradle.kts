import java.util.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jooq.meta.jaxb.Logging

plugins {
    java
    application
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.com.github.johnrengelman.shadow)
    alias(libs.plugins.jooq)
    alias(libs.plugins.org.flywaydb.flyway)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
    alias(libs.plugins.io.ktor.plugin)
    alias(libs.plugins.graalvm)
}

group = "io.github.asm0dey"

version = "2.0.1"

application {
    mainClass.set("io.github.asm0dey.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
val tcnativeClassifier =
    when {
        osName.contains("win") -> "windows-x86_64"
        osName.contains("linux") -> "linux-x86_64"
        osName.contains("mac") -> "osx-x86_64"
        else -> null
    }

dependencies {
    implementation(libs.kotlinx.coroutines.reactor)
    // ktor deps
    implementation(libs.ktor.serialization.kotlinx.xml)
    implementation(libs.ktor.server.call.id.jvm)
    implementation(libs.ktor.server.call.logging.jvm)
    implementation(libs.ktor.server.compression.jvm)
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.host.common.jvm)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.ktor.server.openapi)
    implementation(libs.ktor.server.metrics.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.webjars)
    testImplementation(libs.ktor.server.tests.jvm)
    //cache
//    implementation(libs.caffeine)
//    implementation(libs.rocksdb)
    implementation(libs.zip4j)
    // http2
    implementation(libs.netty.tcnative)
    if (tcnativeClassifier != null) {
        implementation(
            "io.netty:netty-tcnative-boringssl-static:${libs.versions.netty.tcnative.get()}:$tcnativeClassifier"
        )
    } else {
        implementation(libs.netty.tcnative.boringssl.static)
    }
    // di
    implementation(libs.kodein.di.framework.ktor.server.controller.jvm)
    // database
    implementation(libs.hikaricp)
    implementation(libs.flyway.core)
    implementation(libs.jooq.kotlin)
    implementation(libs.sqlite.jdbc)
    jooqCodegen(libs.sqlite.jdbc)
    // utils
    implementation(libs.commons.codec)
    implementation(libs.kotlin.process)
    implementation(libs.ehcache) {
        capabilities {
            requireCapability("org.ehcache:ehcache-jakarta")
        }
    }
    // xml
    implementation(libs.jsoup)
    implementation(libs.jaxb.runtime)
    implementation(libs.xmlutil.serialization.jvm)
    implementation(libs.xmlutil.ktor)
    implementation(libs.kotlin.xml.builder)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.protobuf)
    // logging
    implementation(libs.tinylog.api.kotlin)
    implementation(libs.slf4j.tinylog)
    implementation(libs.tinylog.impl)
    // webjars
    implementation(libs.htmx.org)
    implementation(libs.hyperscript.org)
    implementation(libs.font.awesome)
    implementation(libs.bulma)
}

configure<SourceSetContainer> { named("main") { java.srcDir("src/main/kotlin") } }

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
        vendor.set(JvmVendorSpec.BELLSOFT)
    }
    compilerOptions { jvmTarget.set(JvmTarget.JVM_22) }
}

java {
    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22
    toolchain {
        languageVersion.set(JavaLanguageVersion.of("22"))
        vendor.set(JvmVendorSpec.BELLSOFT)
    }

}

val jooqDb = mapOf("url" to "jdbc:sqlite:$projectDir/build/db/opds.db")

flyway {
    url = jooqDb["url"]
    locations = arrayOf("classpath:db/migration")
    mixed = true
}

tasks.compileKotlin.configure { dependsOn(tasks.named("jooqCodegen")) }

sourceSets {
    // add a flyway sourceSet
    val flyway by creating {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }
    // main sourceSet depends on the output of flyway sourceSet
    main { output.dir(flyway.output) }
}

val migrationDirs =
    listOf(
        "$projectDir/src/flyway/resources/db/migration",
        // "$projectDir/src/flyway/kotlin/db/migration" // Uncomment if we'll add kotlin
        // migrations
    )

tasks.flywayMigrate {
    dependsOn("flywayClasses")
    migrationDirs.forEach { inputs.dir(it) }
    outputs.dirs(
        "${project.layout.buildDirectory}/generated/flyway",
        "${project.layout.buildDirectory}/db"
    )
    doFirst {
        logger.info("Deleting old")
        delete(outputs.files)
        logger.info(
            "Creating directory ${project.layout.buildDirectory}/db with result ${File("$projectDir/build/db").mkdirs()}"
        )
    }
}

jooq {
    configuration {
        logging = Logging.WARN
        jdbc { url = jooqDb["url"] }
        generator {
            name = "org.jooq.codegen.KotlinGenerator"
            generate {
                isDeprecated = false
                isRecords = true
                isImmutablePojos = true
                isFluentSetters = true
                isJavaTimeTypes = true
                isImmutableInterfaces = true
                isDaos = true
                isKotlinNotNullInterfaceAttributes = true
                isKotlinNotNullPojoAttributes = true
                isKotlinNotNullRecordAttributes = true
                isLinks = true
                isPojosAsKotlinDataClasses = true
                isKotlinDefaultedNullablePojoAttributes = false
                isKotlinDefaultedNullableRecordAttributes = false
            }
            target {
                packageName = "io.github.asm0dey.opdsko.jooq"
                directory = "src/main/kotlin"
            }
            database {
                forcedTypes {
                    forcedType {
                        name = "TIMESTAMP"
                        includeExpression = ".*\\.added"
                    }
                    forcedType {
                        name = "BIGINT"
                        includeExpression = ".*\\.id"
                    }
                    forcedType {
                        name = "TEXT"
                        includeExpression = ".*_fts\\..*"
                    }
                    forcedType {
                        name = "BIGINT"
                        includeExpression = ".*\\..*_id"
                    }
                }
                excludes = ".*(_fts_|flyway_).*"
            }
        }
    }
}

ktor {
    docker {
        localImageName.set("opdsko")
        jreVersion.set(JavaVersion.VERSION_21)
    }
}

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            useFatJar.set(false)
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of("22"))
                vendor.set(JvmVendorSpec.BELLSOFT)
            })
            fallback.set(false)
            verbose.set(true)
//            buildArgs.add("--initialize-at-build-time=io.ktor,kotlin")

            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")
            buildArgs.add("--initialize-at-build-time=org.sqlite.util.ProcessRunner")
            buildArgs.add("--initialize-at-build-time=io.netty.handler.codec.http.HttpResponseStatus")
            buildArgs.add("--enable-url-protocols=https")

            imageName.set("opdsko")
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
