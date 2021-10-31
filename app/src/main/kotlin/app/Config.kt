package app
import io.ktor.config.*

object Config {
    private lateinit var env: ApplicationConfig

    val issuer   by lazy { env.property("jwt.issuer").getString() }
    val audience by lazy { env.property("jwt.audience").getString() }
    val subject  by lazy { env.property("jwt.subject").getString() }
    val secret   by lazy { env.property("jwt.secret").getString() }
    val lifetime by lazy { env.property("jwt.lifetime").getString().toInt() }

    fun init(env: ApplicationConfig) {
        this.env = env
    }
}