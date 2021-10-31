package app.Routes

import app.*
import app.Models.*
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

@Route(Method.GET, "/locations")
suspend fun locationsList(call: ApplicationCall) = call.respond(
    transaction {
        return@transaction LocationEntity.all()
            .map { it.toRespondInfo() }
    }
)

@Route(Method.GET, "/locations/{id}/menu")
suspend fun locationMenuList(call: ApplicationCall) {
    val locationID = call.parameters["id"]
        ?: return call.respond(HttpStatusCode.BadRequest)

    call.respond(transaction {
        return@transaction LocationMenuEntity.find { LocationMenuTable.locationID eq locationID.toInt() }
            .map { it.toRespondInfo() }
    })
}