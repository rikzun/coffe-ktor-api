package app

import io.github.classgraph.ClassGraph
import io.ktor.application.*
import io.ktor.http.*
import org.valiktor.ConstraintViolationException
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.kotlinFunction

@Target(AnnotationTarget.FUNCTION)
annotation class Route(val method: Method, val path: String, val auth: Boolean = true)
enum class Method {GET, POST, DELETE}

data class RouteContainer (
    val path: String,
    val method: Method,
    val auth: Boolean,
    val fn: KFunction<ApplicationCall>
)

class Core {
    companion object {
        fun getRoutes(auth: Boolean): List<RouteContainer> {
            val routes = ClassGraph()
                .enableAllInfo()
                .scan()
                .use {
                    it.getClassesWithMethodAnnotation(Route::class.java.canonicalName)
                        .flatMap { classInfo ->
                            classInfo.methodInfo
                                .filter { obj -> obj.hasAnnotation(Route::class.java) }
                                .mapNotNull { fn -> fn.loadClassAndGetMethod().kotlinFunction }
                                .map { fn ->
                                    val annotation = fn.findAnnotation<Route>()!!

                                    RouteContainer (
                                        annotation.path,
                                        annotation.method,
                                        annotation.auth,
                                        fn as KFunction<ApplicationCall>
                                    )
                                }
                        }
                }

            return if (auth) routes.filter { it.auth }
            else routes.filter { !it.auth }
        }

        fun convertMethod(method: Method) = when (method) {
            Method.GET -> HttpMethod.Get
            Method.POST -> HttpMethod.Post
            Method.DELETE -> HttpMethod.Delete
        }

        fun handleValidateError(e: ConstraintViolationException) = e.constraintViolations
            .groupBy { it.property }
            .map {
                it.key + ": " + it.value.map { it.constraint.name }.joinToString()
            }
            .joinToString("\n")
    }
}