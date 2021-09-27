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
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
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

    transaction {
        SchemaUtils.create(Users)

        User.new {
            login = "admin"
            password = "admin"
        }


        for (user in User.all()) {
            println(user.login)
        }

    }
}

//fun Application.module(testing: Boolean = false) {
//    install(ContentNegotiation) { gson() }
//    install(Authentication) { jwt {} }
//
////    var db = initFirebase()
//
//    routing {
//        post("/login") {
//            val requiredKeys = listOf("login", "password")
//            val query = call.receive<HashMap<String, Any>>()
//
//            if (!query.keys.containsAll(requiredKeys)) {
//                call.respond(HttpStatusCode.BadRequest)
//            }

////            if (user == null) call.respond(HttpStatusCode.NotFound)
////            val token = JWT.create()
////                .withClaim("username", user?.get(0)?.login)
////                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
////                .sign(Algorithm.HMAC256("secret"))
////
////            println(token)
//
//            call.respond("2")
//        }
//    }
//}
