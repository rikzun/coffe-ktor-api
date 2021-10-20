package app

import io.github.classgraph.ClassGraph
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.kotlinFunction

annotation class Route(val method: Method, val auth: Boolean = true)
enum class Method {GET, POST, DELETE}

data class RouteFunction (
    val path: String,
    val method: Method,
    val auth: Boolean,
    val fn: KFunction<*>
)

class Core {
    companion object {
        fun getRoutes() = ClassGraph()
            .enableAllInfo()
            .scan()
            .use {
                it.getClassesWithMethodAnnotation(Route::class.java.canonicalName)
                    .flatMap { classInfo ->
                        classInfo.methodInfo
                            .filter { fn -> fn.hasAnnotation(Route::class.java) }
                            .mapNotNull { fn -> fn.loadClassAndGetMethod().kotlinFunction }
                            .map { fn ->
                                val annotation = fn.findAnnotation<Route>()!!

                                RouteFunction(
                                    toRoutePath(classInfo.name + "." + fn.name),
                                    annotation.method,
                                    annotation.auth,
                                    fn
                                )
                            }
                    }
            }

        private fun toRoutePath(str: String): String {
            if (!str.contains(Config.rspath)) throw Exception("Wrong route path: $str")
            return "/" + str.substringAfter(Config.rspath).replace(Regex("Kt\\.|\\."), "/").lowercase()
        }
    }
}