package kt.Routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import kt.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.valiktor.ConstraintViolationException
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isNotEmpty
import org.valiktor.functions.isNotNull
import org.valiktor.validate

data class LoginData (
    val login: String,
    val password: String
)

object AuthLogin : Route {
    override val method = Method.POST
    override val path = "/auth/login"

    override suspend fun run(call: ApplicationCall) {
        val params = call.receiveOrNull<LoginData>() ?: return call.respond(HttpStatusCode.BadRequest)
        println("23124")
        try {
            validate(params) {
                validate(LoginData::login).isNotEmpty().isNotBlank().isNotNull()
                validate(LoginData::password).isNotEmpty().isNotBlank().isNotNull()
            }
        } catch(e: ConstraintViolationException) {
            call.respond(tr.errors(e))
            return
        }

        val tr = transaction {
            val users = UserEntity.find {
                UserTable.login eq params.login and (
                    UserTable.password eq params.password
                )
            }
            if (users.empty()) return@transaction HttpStatusCode.NotFound
            return@transaction JwtUtils.create(users.first().id.value)
        }
        call.respond(tr)
    }
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
