package io.github.asm0dey.plugins

import gg.jte.TemplateEngine
import io.ktor.server.application.*
import io.ktor.server.jte.*
import io.ktor.server.webjars.*

fun Application.content() {
    install(Jte) {
        templateEngine = TemplateEngine.createPrecompiled(gg.jte.ContentType.Html)
    }
    install(Webjars)
}