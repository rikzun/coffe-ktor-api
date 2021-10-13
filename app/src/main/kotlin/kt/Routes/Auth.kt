package kt.Routes

import com.google.gson.internal.LinkedTreeMap
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import kt.JwtUtils
import kt.UserEntity
import kt.UserTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

data class LoginData (
    val login: String,
    val password: String
)

object AuthLogin : Route {
    override val method = Method.POST
    override val path = "/auth/login"

    override suspend fun run(call: ApplicationCall): Any {
        val params = call.receiveOrNull<LoginData>() ?: return HttpStatusCode.BadRequest

        return transaction {
            val users = UserEntity.find {
                UserTable.login eq params.login and(
                    UserTable.password eq params.password
                )
            }
            if (users.empty()) return@transaction HttpStatusCode.NotFound
            return@transaction JwtUtils.create(users.first().id.value)
        }
    }
}

object AuthReg : Route {
    override val method = Method.POST
    override val path = "/auth/register"

    override suspend fun run(call: ApplicationCall): Any {
        val params = call.receiveOrNull<LoginData>() ?: return HttpStatusCode.BadRequest

        return transaction {
            val users = UserEntity.find { UserTable.login eq params.login }
            if (!users.empty()) return@transaction HttpStatusCode.NotAcceptable

            val user = UserEntity.new {
                login = params.login
                password = params.password
            }

            return@transaction JwtUtils.create(user.id.value)
        }
    }
}
