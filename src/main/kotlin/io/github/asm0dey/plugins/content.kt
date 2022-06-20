package io.github.asm0dey.plugins

import io.ktor.http.*
import io.ktor.serialization.kotlinx.xml.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML

fun Application.content() {
    install(ContentNegotiation) {
        xml(contentType = ContentType.parse("application/atom+xml;profile=opds-catalog;kind=navigation; charset=utf-8"), format = XML {
            xmlVersion = nl.adaptivity.xmlutil.core.XmlVersion.XML10
            xmlDeclMode = XmlDeclMode.Charset
            autoPolymorphic = true
            isCollectingNSAttributes = true
            repairNamespaces = true
            indent = 2
        })
    }
}