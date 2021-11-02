package app.Routes

import app.*
import app.Models.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import org.jetbrains.exposed.sql.transactions.transaction

@Route(Method.GET, "/locations")
suspend fun locationsList(call: ApplicationCall, principal: Principal) = call.respond(
    transaction {
        return@transaction LocationEntity.all()
            .map { it.toRespondInfo() }
    }
)

@Route(Method.GET, "/location/{id}/menu")
suspend fun locationMenuList(call: ApplicationCall, principal: Principal) {
    val locationID = call.parameters["id"]
        ?: return call.respond(HttpStatusCode.BadRequest)

    call.respond(transaction {
        return@transaction LocationMenuEntity
            .find { LocationMenuTable.locationID eq locationID.toInt() }
            .map { it.toRespondInfo() }
    })
}