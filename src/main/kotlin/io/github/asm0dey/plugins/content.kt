package io.github.asm0dey.plugins

import gg.jte.TemplateEngine
import io.ktor.http.*
import io.ktor.serialization.kotlinx.xml.*
import io.ktor.server.application.*
import io.ktor.server.jte.*
import io.ktor.server.plugins.contentnegotiation.*
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML

fun Application.content() {
    install(ContentNegotiation) {
        ignoreType<JteContent>()
        xml(contentType = ContentType.parse("application/atom+xml;profile=opds-catalog;charset=utf-8"), format = XML {
            xmlVersion = nl.adaptivity.xmlutil.core.XmlVersion.XML10
            xmlDeclMode = XmlDeclMode.Charset
            autoPolymorphic = true
//            isCollectingNSAttributes = true
            repairNamespaces = true
            indent = 2
        })
    }
    install(Jte) {
        templateEngine = TemplateEngine.createPrecompiled(gg.jte.ContentType.Html)
    }
}