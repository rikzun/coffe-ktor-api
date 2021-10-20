package app

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object JwtUtils {

    fun create(id: Int): String = JWT.create()
        .withClaim("id", id)
        .withIssuer(Config.issuer)
        .withAudience(Config.audience)
        .withSubject(Config.subject)
        .withExpiresAt(Date(System.currentTimeMillis() + 6000000))
        .sign(Algorithm.HMAC256(Config.secret))

    fun verify(): JWTVerifier? = JWT
        .require(Algorithm.HMAC256(Config.secret))
        .withIssuer(Config.issuer)
        .withAudience(Config.audience)
        .withSubject(Config.subject)
        .build()

    fun validate(cred: JWTCredential): Principal? = transaction {
        val id = cred.payload.getClaim("id")?.asInt() ?: return@transaction null
        return@transaction UserEntity.findById(id)
    }
}