package kt.Routes

import io.ktor.application.*
import kt.UserEntity

enum class Method {GET, POST, DELETE}

sealed interface AuthRoute {
    val method: Method
    val path: String

    suspend fun run(user: UserEntity, call: ApplicationCall): Any
}

sealed interface Route {
    val method: Method
    val path: String

    suspend fun run(call: ApplicationCall): Any
}

class Core {
    companion object {
        fun getAuthRoutes(): List<AuthRoute> {
            return AuthRoute::class.sealedSubclasses
                .filter { it.objectInstance !== null }
                .map { it.objectInstance!! }
        }

        fun getRoutes(): List<Route> {
            return Route::class.sealedSubclasses
                .filter { it.objectInstance !== null }
                .map { it.objectInstance!! }
        }
    }
}