package kt.Routes

import io.ktor.application.*
import kt.Kekw
import kt.UserEntity
import org.valiktor.ConstraintViolationException
import org.valiktor.Validator
import org.valiktor.validate

enum class Method {GET, POST, DELETE}

sealed interface AuthRoute {
    val method: Method
    val path: String

    suspend fun run(call: ApplicationCall, user: UserEntity)
}

sealed interface Route {
    val method: Method
    val path: String

    suspend fun run(call: ApplicationCall)
}

class Core {
    companion object {
        fun getAuthRoutes(): List<AuthRoute> {
            return AuthRoute::class.sealedSubclasses
                .mapNotNull { it.objectInstance }
        }

        fun getRoutes(): List<Route> {
            return Route::class.sealedSubclasses
                .mapNotNull { it.objectInstance }
        }
    }
}

object tr {
    private val dictionary = mapOf(
        "NotNull" to "cannot_be_null",
        "NotBlank" to "cannot_be_blank",
        "NotEmpty" to "cannot_be_empty"
    )

    fun errors(error: ConstraintViolationException): String {
        return error.constraintViolations
            .groupBy { it.property }
            .map {
                it.key + ": " +
                it.value.map { this.dictionary[it.constraint.name] ?: it.constraint.name }.joinToString()
            }
            .joinToString("\n")
    }
}

@Kekw
fun rs(): Int {
    return 1
}

@Kekw
fun rs2(): Unit {}