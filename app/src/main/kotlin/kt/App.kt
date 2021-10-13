package kt

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.server.netty.*
import io.ktor.util.pipeline.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import kt.Routes.*
import kt.Routes.Route
import kotlin.reflect.full.declaredMembers

object UserTable : IntIdTable() {
    val login    = varchar("login", 255)
    val password = varchar("password", 255)
}

class UserEntity(id: EntityID<Int>) : IntEntity(id), Principal {
    companion object : IntEntityClass<UserEntity>(UserTable)
    var login    by UserTable.login
    var password by UserTable.password
}


fun main(args: Array<String>) = EngineMain.main(args)
fun Application.module(testing: Boolean = false) {
    // database init
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    Database.connect("jdbc:sqlite:app/src/main/resources/data.db", "org.sqlite.JDBC")
    transaction { SchemaUtils.create(UserTable) }

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
            for (route in Core.getAuthRoutes()) {
                suspend fun func(call: ApplicationCall) {
                    call.respond(route.run(call.principal()!!, call))
                }

                when (route.method) {
                    Method.GET -> get(route.path) { func(call) }
                    Method.POST -> post(route.path) { func(call) }
                    Method.DELETE -> delete(route.path) { func(call) }
                }
            }
        }

        for (route in Core.getRoutes()) {
            suspend fun func(call: ApplicationCall) {
                call.respond(route.run(call))
            }

            when (route.method) {
                Method.GET -> get(route.path) { func(call) }
                Method.POST -> post(route.path) { func(call) }
                Method.DELETE -> delete(route.path) { func(call) }
            }
        }
    }
}