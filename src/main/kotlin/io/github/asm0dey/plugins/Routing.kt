/*
 * opdsko
 * Copyright (C) 2022  asm0dey
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@file:Suppress("HttpUrlsUsage")

package io.github.asm0dey.plugins

import io.github.asm0dey.controllers.Api
import io.github.asm0dey.controllers.Opds
import io.github.asm0dey.controllers.OpdsBuilder
import io.github.asm0dey.opdsko.jooq.Tables.BOOK
import io.github.asm0dey.scan
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jooq.DSLContext
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.controller.controller
import java.io.File
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

val dtf: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

fun Application.routes() {
    routing {
        static("/") {
            staticBasePackage = "static"
            resources(".")
            defaultResource("index.html")
        }
        post("/scan") {
            thread(start = true, isDaemon = true, name = "Scanner", priority = 1) {
                val dir =
                    this@routes.environment.config.propertyOrNull("ktor.indexer.path")?.getString() ?: return@thread
                val create by closestDI().instance<DSLContext>()
                scan(dir, create)
                val pathsToDelete = create.select(BOOK.ID, BOOK.PATH).from(BOOK)
                    .mapNotNull { r -> r[BOOK.PATH].takeIf { !File(it).exists() }?.let { r[BOOK.ID] } }
                create.deleteFrom(BOOK).where(BOOK.ID.`in`(pathsToDelete)).execute()
            }
            call.respondText("Scan started")
        }
        controller { Opds(instance()) }
        controller { Api(instance()) }
        controller { OpdsBuilder(instance()) }
    }
}


@Suppress("SpellCheckingInspection")
const val OPDSKO_JDBC = "jdbc:sqlite:opds.db"






