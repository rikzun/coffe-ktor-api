package app.Routes

import app.Core
import app.JwtUtils
import app.Method
import app.Models.UserEntity
import app.Models.UserTable
import app.Route
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.valiktor.ConstraintViolationException
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isNotEmpty
import org.valiktor.functions.isNotNull
import org.valiktor.validate
import kotlin.reflect.full.declaredMemberProperties

data class LoginData (
    val login: String,
    val password: String
)

@Route(Method.POST, "/auth/{data}/login", auth = false)
suspend fun loginController(call: ApplicationCall) {
    val params = call.receiveOrNull<LoginData>()
        ?: return call.respond(HttpStatusCode.BadRequest)

    try {
        validate(params) {
            validate(LoginData::login).isNotEmpty().isNotBlank().isNotNull()
            validate(LoginData::password).isNotEmpty().isNotBlank().isNotNull()
        }
    } catch(e: ConstraintViolationException) {
        return call.respond(HttpStatusCode.BadRequest, Core.handleValidateError(e))
    }

    call.respond(transaction {
        val users = UserEntity.find {
            UserTable.login eq params.login and (
                UserTable.password eq params.password
                )
        }
        if (users.empty()) return@transaction HttpStatusCode.NotFound
        return@transaction JwtUtils.create(users.first().id.value)
    })
}

//object AuthReg : Route {
//    override val method = Method.POST
//    override val path = "/auth/register"
//
//    override suspend fun run(call: ApplicationCall): Any {
//        val params = call.receiveOrNull<LoginData>() ?: return HttpStatusCode.BadRequest
//
//        return transaction {
//            val users = UserEntity.find { UserTable.login eq params.login }
//            if (!users.empty()) return@transaction HttpStatusCode.NotAcceptable
//
//            val user = UserEntity.new {
//                login = params.login
//                password = params.password
//            }
//
//            return@transaction JwtUtils.create(user.id.value)
//        }
//    }
//}