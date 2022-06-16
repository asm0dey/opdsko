package io.github.asm0dey.plugins

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.compression.*

fun Application.configureHTTP() {
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }
    install(ShutDownUrl.ApplicationCallPlugin) {
        shutDownUrl = "/shutdownNow"
        exitCodeSupplier = { 0 }
    }

}
