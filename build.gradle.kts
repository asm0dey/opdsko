import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Logging
import java.nio.file.Paths

plugins {
    java
    application
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.com.github.johnrengelman.shadow)
    alias(libs.plugins.nu.studer.jooq)
    alias(libs.plugins.org.flywaydb.flyway)
    alias(libs.plugins.gg.jte.gradle)
}

group = "io.github.asm0dey"
version = "0.0.12"
application {
    mainClass.set("io.github.asm0dey.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
    maven { url = uri("https://jitpack.io") }

}
val osName = System.getProperty("os.name").toLowerCase()
val tcnative_classifier = when {
    osName.contains("win") -> "windows-x86_64"
    osName.contains("linux") -> "linux-x86_64"
    osName.contains("mac") -> "osx-x86_64"
    else -> null
}

dependencies {
    implementation(KotlinX.coroutines.reactor)
    // ktor deps
    implementation(Ktor.plugins.serialization.kotlinx.xml)
    implementation(libs.ktor.server.call.id.jvm)
    implementation(libs.ktor.server.call.logging.jvm)
    implementation(libs.ktor.server.compression.jvm)
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.host.common.jvm)
    implementation(Ktor.server.htmlBuilder)
    implementation(Ktor.server.jte)
    implementation(libs.ktor.server.metrics.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(Ktor.server.resources)
    implementation(Ktor.server.webjars)
    testImplementation(libs.ktor.server.tests.jvm)
    // http2
    implementation(libs.netty.tcnative)
    if (tcnative_classifier != null) {
        implementation("io.netty:netty-tcnative-boringssl-static:${libs.versions.netty.tcnative.get()}:$tcnative_classifier")
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
    jooqGenerator(libs.sqlite.jdbc)
    // utils
    implementation(libs.commons.codec)
    implementation(libs.jte.kotlin)
    implementation(libs.kotlin.process)
    // xml
    implementation(libs.jsoup)
    implementation(libs.jaxb.runtime)
    implementation(libs.xmlutil.serialization.jvm)
    implementation(libs.xmlutil.ktor)
    implementation(libs.kotlin.xml.builder)
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

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/kotlin")
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val jooqDb = mapOf("url" to "jdbc:sqlite:$projectDir/build/db/opds.db")

flyway {
    url = jooqDb["url"]
    locations = arrayOf("classpath:db/migration")
}


sourceSets {
    //add a flyway sourceSet
    val flyway by creating {
        compileClasspath += sourceSets.main.get().compileClasspath
        runtimeClasspath += sourceSets.main.get().runtimeClasspath
    }
    //main sourceSet depends on the output of flyway sourceSet
    main {
        output.dir(flyway.output)
    }
}
val migrationDirs = listOf(
    "$projectDir/src/flyway/resources/db/migration",
    // "$projectDir/src/flyway/kotlin/db/migration" // Uncomment if we'll add kotlin migrations
)
tasks.flywayMigrate {
    dependsOn("flywayClasses")
    migrationDirs.forEach { inputs.dir(it) }
    outputs.dirs("${project.buildDir}/generated/flyway", "${project.buildDir}/db")
    doFirst {
        logger.info("Deleting old")
        delete(outputs.files)
        logger.info("Creating directory ${project.buildDir}/db with result ${File("$projectDir/build/db").mkdirs()}")

    }
}

jooq {
    configurations {
        create("main") {
            jooqConfiguration.apply {

                logging = Logging.WARN
                jdbc.apply {
                    url = jooqDb["url"]
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
                                    name = "TEXT"
                                    includeExpression = ".*_fts\\..*"
                                },
                                ForcedType().apply {
                                    name = "BIGINT"
                                    includeExpression = ".*\\..*_id"
                                },
                            )
                        )
                        excludes = ".*(_fts_|flyway_).*"
                    }
                }
            }
        }
    }
}

val generateJooq by project.tasks
generateJooq.dependsOn(tasks.flywayMigrate)
//generateJooq.doLast { File("$projectDir/build/db/opds.db").delete() }


jte {
    generate()
    sourceDirectory.set(Paths.get("templates"))
    contentType.set(gg.jte.ContentType.Html)
    binaryStaticContent.set(true)
    trimControlStructures.set(true)
}
