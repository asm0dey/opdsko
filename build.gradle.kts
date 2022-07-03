import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Logging
import java.nio.file.Paths

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val tinylog_version: String by project

plugins {
    java
    application
    kotlin("jvm") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("nu.studer.jooq") version "7.1.1"
    id("org.flywaydb.flyway") version "8.5.13"
    id("gg.jte.gradle") version "2.1.1"

}

group = "io.github.asm0dey"
version = "0.0.7"
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.2")
    // ktor deps
    implementation("io.ktor:ktor-serialization-kotlinx-xml:$ktor_version")
    implementation("io.ktor:ktor-server-call-id-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-compression-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    implementation("io.ktor:ktor-server-jte:$ktor_version")
    implementation("io.ktor:ktor-server-metrics-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-resources:$ktor_version")
    implementation("io.ktor:ktor-server-webjars:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    // database
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.flywaydb:flyway-core:8.5.13")
    implementation("org.jooq:jooq-kotlin:3.16.7")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    jooqGenerator("org.xerial:sqlite-jdbc:3.36.0.3")
    // utils
    implementation("commons-codec:commons-codec:1.15")
    implementation("gg.jte:jte-kotlin:2.1.1")
    // xml
    implementation("org.jsoup:jsoup:1.15.1")
    implementation("org.glassfish.jaxb:jaxb-runtime:4.0.0")
    implementation("io.github.pdvrieze.xmlutil:serialization-jvm:0.84.2")
    implementation("io.github.pdvrieze.xmlutil:ktor:0.84.2")
    // logging
    implementation("org.tinylog:tinylog-api-kotlin:$tinylog_version")
    implementation("org.tinylog:slf4j-tinylog:$tinylog_version")
    implementation("org.tinylog:tinylog-impl:$tinylog_version")
    // webjars
    implementation("org.webjars.npm:htmx.org:1.7.0")
    implementation("org.webjars.npm:hyperscript.org:0.9.5")
    implementation("org.webjars:font-awesome:6.1.1")
    implementation("org.webjars.npm:bulma:0.9.4")
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
