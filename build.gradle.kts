import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jooq.meta.jaxb.Logging
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.util.*

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath(libs.testcontainers)
        classpath(libs.org.testcontainers.postgresql)
        classpath(libs.docker.java)
    }
}


plugins {
    java
    application
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.com.github.johnrengelman.shadow)
    alias(libs.plugins.jooq)
    alias(libs.plugins.org.liquibase.gradle)
    alias(libs.plugins.org.jetbrains.kotlin.plugin.serialization)
}

group = "io.github.asm0dey"
version = "1.0.0"
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
val tcnativeClassifier = when {
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
    implementation(libs.zip4j)
    // http2
    implementation(libs.netty.tcnative)
    if (tcnativeClassifier != null) {
        implementation("io.netty:netty-tcnative-boringssl-static:${libs.versions.netty.tcnative.get()}:$tcnativeClassifier")
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
    jooqCodegen(libs.postgres.jdbc)

    // utils
    implementation(libs.commons.codec)
    implementation(libs.kotlin.process)
    implementation(libs.ehcache)
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

    implementation(libs.liquibase.core)
    liquibaseRuntime(libs.liquibase.core)
    liquibaseRuntime(libs.picocli)
    liquibaseRuntime(libs.postgres.jdbc)
    runtimeOnly(libs.postgres.jdbc)
}

configure<SourceSetContainer> {
    named("main") {
        java.srcDir("src/main/kotlin")
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

val parade = PostgreSQLContainer(
    DockerImageName.parse("paradedb/paradedb:latest").asCompatibleSubstituteFor("postgres")
)

val start by tasks.registering(Container::class) {
    println("START CONTAINER")
    container = parade
    start()
}

val stop by tasks.registering {
    println("STOP CONTAINER CONFIG")
    doLast {
        println("STOP CONTAINER")
        parade.stop()
        parade.close()
        println("PARADE STOPPED")
    }
}

liquibase {
    activities.register("main") {
        val container = start.get().container
        this.arguments = mapOf(
            "logLevel" to "debug",
            "classpath" to "${project.rootDir}/src/main/",
            "changeLogFile" to "resources/db/changelog-main.xml",
            "url" to container.get().getJdbcUrl(),
            "username" to container.get().username,
            "password" to container.get().password,
            "driver" to "org.postgresql.Driver"
        )
    }
    runList = "main"
}
jooq {
    configuration {
        logging = Logging.WARN

        jdbc {
            val container = start.get().container
            driver = "org.postgresql.Driver"
            url = container.get().getJdbcUrl()
            username = container.get().username
            password = container.get().password
        }
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
                isLinks = true
                isPojosAsKotlinDataClasses = true
                isDaos = true
                isKotlinNotNullInterfaceAttributes = true
                isKotlinNotNullPojoAttributes = true
                isKotlinNotNullRecordAttributes = true
                isImplicitJoinPathsAsKotlinProperties = true
                isKotlinDefaultedNullablePojoAttributes = true
                isKotlinDefaultedNullableRecordAttributes = true
            }
            target {
                packageName = "io.github.asm0dey.opdsko.jooq"
                directory = "src/main/java"
            }
            database {
                schemata {
                    schema {
                        inputSchema = "public"
                    }
                    schema {
                        inputSchema = "book_ngr_idx"
                    }
                }
                excludes = """
                   databasechangelog.*
                   | vector.*
                   | svector.*
                   | l1_.*
                   | l2_.*
                   | array_.*
                   | cosine.*
                   | avg
                   | hnsw.*
                   | inner.*
                   | sum.*
                   | ivf.*
                   | shns.*
                   | gin_.*
                   | gtrgm_.*
                   | strict_.*
                   | word_.*
                   | show_.*
                   | similarity_.*
                """.trimIndent()
            }
        }

    }
}

abstract class Container : DefaultTask() {
    @get:Input
    abstract val container: Property<PostgreSQLContainer<*>>

    @TaskAction
    fun start() {
        container.get().start()
    }

    fun stop() {
        container.get().stop()
    }
}

tasks.named("update").configure {
    dependsOn(start)
}

tasks.jooqCodegen.configure {
    dependsOn(tasks.named("update"))
    finalizedBy(stop)
}

tasks.compileKotlin.configure {
    dependsOn(tasks.jooqCodegen)
}