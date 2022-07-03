package io.github.asm0dey.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.asm0dey.repository.Repository
import io.github.asm0dey.service.InfoService
import io.ktor.server.application.*
import org.jooq.ExecuteContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.impl.DefaultExecuteListener
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
            config.jdbcUrl = OPDSKO_JDBC
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

class CustomLogger : DefaultExecuteListener() {
    override fun executeStart(ctx: ExecuteContext) {
        val create =
            DSL.using(ctx.dialect(), Settings().withRenderFormatted(false))
        if (ctx.query() != null) {
            Logger.tag("JOOQ").info(create.renderInlined(ctx.query()))
        }
    }
}

