package kt

import io.github.classgraph.*
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
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.jvm.kotlinFunction
import kotlin.reflect.jvm.reflect

object UserTable : IntIdTable() {
    val login    = varchar("login", 255)
    val password = varchar("password", 255)
}

class UserEntity(id: EntityID<Int>) : IntEntity(id), Principal {
    companion object : IntEntityClass<UserEntity>(UserTable)
    var login    by UserTable.login
    var password by UserTable.password
}

annotation class Kekw

data class CL(
    val path: String,
    val fn: Any
)

fun main(args: Array<String>) {
    val annotation = Kekw::class.java
    val annotationName = annotation.canonicalName

    val a = ClassGraph()
        .enableAllInfo()
        .scan().use {
            it.getClassesWithMethodAnnotation(annotationName).map {
                CL(it.name, it.methodInfo.filter { it.hasAnnotation(annotation) })
            }
        }

    println(a)
}
//fun main(args: Array<String>) = EngineMain.main(args)
//fun Application.module(testing: Boolean = false) {
//    // database init
//    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
//    Database.connect("jdbc:sqlite:app/src/main/resources/data.db", "org.sqlite.JDBC")
//    transaction { SchemaUtils.create(UserTable) }
//
//    Config.init(environment.config)
//
//    install(ContentNegotiation) { gson() }
//    install(Authentication) {
//        jwt("auth-jwt") {
//            verifier { JwtUtils.verify() }
//            validate { JwtUtils.validate(it) }
//        }
//    }
//
//    println(Core.getRoutes())
//
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
//}