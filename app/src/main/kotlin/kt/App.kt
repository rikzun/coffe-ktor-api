package kt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileInputStream
import java.sql.Connection
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.full.memberProperties

object Users : IntIdTable() {
    val login = varchar("login", 255)
    val password = varchar("password", 255)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)
    var login     by Users.login
    var password  by Users.password
}

fun main(args: Array<String>) {
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    Database.connect("jdbc:sqlite:app/src/main/resources/data.db", "org.sqlite.JDBC")

    EngineMain.main(args)
}

fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) { gson() }

    routing {
        post("/login") {
            val requiredKeys = listOf("login", "password")
            val query = call.receive<HashMap<String, String>>()

            if (!query.keys.containsAll(requiredKeys)) {
                return@post call.respond(HttpStatusCode.BadRequest)
            }

            var user: User

            transaction {
                user = User.find {Users.login eq query["login"]!! and (Users.password eq query["password"]!!) }.first()
            }

            println(user)



            call.respond("2")
        }
    }
}