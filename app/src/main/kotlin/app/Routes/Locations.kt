package app.Routes

import app.*
import app.Models.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isNotEmpty
import org.valiktor.functions.isNotNull
import java.math.BigDecimal

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

data class LocationCreateParams(
    val name: String,
    val latitude: BigDecimal,
    val longitude: BigDecimal
)

@Route(Method.POST, "/location/create")
suspend fun locationCreate(call: ApplicationCall, principal: Principal) {
    val params = call.receiveAndValid<LocationCreateParams> {
        validate(LocationCreateParams::name).isNotEmpty().isNotBlank().isNotNull()
        validate(LocationCreateParams::latitude).isNotNull()
        validate(LocationCreateParams::longitude).isNotNull()
    } ?: return

    val target = transaction {
        return@transaction LocationEntity.new {
            name = params.name
            pointX = params.latitude
            pointY = params.longitude
        }
    }

    call.respond(target.toRespondInfo())
}

@Route(Method.DELETE, "/location/{id}/delete")
suspend fun locationDelete(call: ApplicationCall, principal: Principal) {
    val locationID = call.parameters["id"]
        ?: return call.respond(HttpStatusCode.BadRequest)

    transaction {
        return@transaction LocationEntity
            .findById(locationID.toInt())
            ?.delete()
    } ?: return call.respond(HttpStatusCode.NotFound)

    call.respond(HttpStatusCode.OK)
}