package app

import app.Models.LocationMenuTable
import app.Models.LocationTable
import app.Models.UserTable
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import kotlin.reflect.full.callSuspend

fun main(args: Array<String>) = EngineMain.main(args)
fun Application.module() {

    // database init
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    Database.connect("jdbc:sqlite:app/src/main/resources/data.db", "org.sqlite.JDBC")
    transaction {
        SchemaUtils.create(UserTable)
        SchemaUtils.create(LocationTable)
        SchemaUtils.create(LocationMenuTable)
    }

    Config.init(environment.config)

    install(ContentNegotiation) { gson() }
    install(Authentication) {
        jwt("auth-jwt") {
            verifier { JwtUtils.verify() }
            validate { JwtUtils.validate(it) }
        }
    }

    routing {
        authenticate("auth-jwt") {
            for (route in Core.getRoutes(auth = true)) {
                route(route.path, Core.convertMethod(route.method)) {
                    handle { route.fn.callSuspend(call, call.principal()!!) }
                }
            }
        }

        for (route in Core.getRoutes(auth = false)) {
            route(route.path, Core.convertMethod(route.method)) {
                handle { route.fn.callSuspend(call) }
            }
        }
    }
}