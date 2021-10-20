package app

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.routing.*
import io.ktor.routing.Route
import io.ktor.server.netty.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.reflect.AccessibleObject
import java.sql.Connection
import kotlin.reflect.full.callSuspend

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
fun Application.module() {

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
        for (route in Core.getRoutes()) {
            val fn = suspend {
                route.fn.callSuspend()
            }

            val routeFn = {
                when (route.method) {
                    Method.GET -> get(route.path) { fn() }
                    Method.POST -> post(route.path) { fn() }
                    Method.DELETE -> delete(route.path) { fn() }
                }
            }

            if (route.auth) {
                authenticate {
                    routeFn.run {  }
                }
            } else {
                routeFn.run {}
            }
        }
    }


//    routing {
//        authenticate("auth-jwt") {
//            for (route in Core.getAuthRoutes()) when (route.method) {
//                Method.GET -> get(route.path) { route.run(call, call.principal()!!) }
//                Method.POST -> post(route.path) { route.run(call, call.principal()!!) }
//                Method.DELETE -> delete(route.path) { route.run(call, call.principal()!!) }
//            }
//        }
//
//        for (route in Core.getRoutes()) when (route.method) {
//            Method.GET -> get(route.path) { route.run(call) }
//            Method.POST -> post(route.path) { route.run(call) }
//            Method.DELETE -> delete(route.path) { route.run(call) }
//        }
//    }
}