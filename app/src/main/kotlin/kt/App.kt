package kt

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import kt.Routes.*

object UserTable : IntIdTable() {
    val login    = varchar("login", 255)
    val password = varchar("password", 255)
}

class UserEntity(id: EntityID<Int>) : IntEntity(id), Principal {
    companion object : IntEntityClass<UserEntity>(UserTable)
    var login    by UserTable.login
    var password by UserTable.password
}

data class UserResponse(val login: String, val password: String)

fun main(args: Array<String>) = EngineMain.main(args)
fun Application.module(testing: Boolean = false) {
    // database init
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    Database.connect("jdbc:sqlite:app/src/main/resources/data.db", "org.sqlite.JDBC")
    transaction { SchemaUtils.create(UserTable) }

    println('c' is Char)

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
            get("/hi") {
                val user = call.principal<UserEntity>()!!
                call.respond(UserResponse(user.login, user.password))
            }
        }

        get("/auth/login") {
            transaction {
                UserEntity.all().forEach {
                    println(it.id)
                    println(it.login)
                }
            }
            val token = JwtUtils.create(1)

            call.respond(token)
        }
    }
}