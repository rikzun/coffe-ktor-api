package app.Routes

import app.*
import app.Models.UserEntity
import app.Models.UserTable
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

data class AuthResponse(
    val token: String,
    val tokenLifetime: Int
)

@Route(Method.POST, "/auth/login", auth = false)
suspend fun authLogin(call: ApplicationCall) {
    val params = call.receiveAndValid<LoginData> {
        validate(LoginData::login).isNotEmpty().isNotBlank().isNotNull()
        validate(LoginData::password).isNotEmpty().isNotBlank().isNotNull()
    } ?: return

    call.respond(transaction {
        val users = UserEntity.find {
            UserTable.login eq params.login and (
                UserTable.password eq params.password
            )
        }
        if (users.empty()) return@transaction HttpStatusCode.NotFound

        return@transaction AuthResponse(
            JwtUtils.create(users.first().id.value),
            Config.lifetime
        )
    })
}

@Route(Method.POST, "/auth/register", auth = false)
suspend fun authRegister(call: ApplicationCall) {
    val params = call.receiveAndValid<LoginData> {
        validate(LoginData::login).isNotEmpty().isNotBlank().isNotNull()
        validate(LoginData::password).isNotEmpty().isNotBlank().isNotNull()
    } ?: return

    call.respond(transaction {
        val users = UserEntity.find { UserTable.login eq params.login }
        if (!users.empty()) return@transaction HttpStatusCode.NotAcceptable

        val user = UserEntity.new {
            login = params.login
            password = params.password
        }

        return@transaction AuthResponse(
            JwtUtils.create(user.id.value),
            Config.lifetime
        )
    })
}