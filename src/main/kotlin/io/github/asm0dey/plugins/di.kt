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
package io.github.asm0dey.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.asm0dey.repository.Repository
import io.github.asm0dey.service.InfoService
import io.ktor.server.application.*
import org.jooq.ExecuteContext
import org.jooq.ExecuteListener
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.DefaultExecuteListenerProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.tinylog.kotlin.Logger
import javax.sql.DataSource

fun Application.ioc() {
    di {
        bindSingleton<DataSource> {
            val config = HikariConfig()
            config.poolName = "opdsko pool"
            config.jdbcUrl = environment.config.propertyOrNull("db.url")?.getString()
                ?: throw IllegalStateException("No db url defined")
            config.username = environment.config.propertyOrNull("db.username")?.getString()
            config.password = environment.config.propertyOrNull("db.password")?.getString()
            config.connectionTestQuery = "SELECT 1"
            config.maxLifetime = 60000
            config.idleTimeout = 45000
            config.maximumPoolSize = 3
            HikariDataSource(config)
        }
        bindSingleton {
            DSL.using(instance<DataSource>(), SQLDialect.SQLITE).apply {
                settings().apply {
                    isExecuteLogging = false
                }
                configuration().set(DefaultExecuteListenerProvider(CustomLogger()))
            }
        }
        bindSingleton { Repository(instance()) }
        bindSingleton { InfoService(instance()) }
    }
}

class CustomLogger : ExecuteListener {
    override fun executeStart(ctx: ExecuteContext) {
        val create =
            DSL.using(ctx.dialect(), Settings().withRenderFormatted(false))
        if (ctx.query() != null) {
            Logger.tag("JOOQ").info(create.renderInlined(ctx.query()))
        }
    }
}

