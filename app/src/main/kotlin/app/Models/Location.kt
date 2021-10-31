package app.Models

import io.ktor.auth.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import java.math.BigDecimal

object LocationTable : IntIdTable("locations") {
    val name   = text("name")
    val pointX = decimal("latitude", 16, 14)
    val pointY = decimal("longitude", 16, 14)
}

class LocationEntity(id: EntityID<Int>) : IntEntity(id), Principal {
    companion object : IntEntityClass<LocationEntity>(LocationTable)
    var name   by LocationTable.name
    var pointX by LocationTable.pointX
    var pointY by LocationTable.pointY

    fun toRespondInfo() = LocationRespond(
        id    = this.id.value,
        name  = this.name,
        point = Point(this.pointX, this.pointX)
    )
}

data class LocationRespond(
    val id: Int,

    val name: String,
    val point: Point
)

data class Point(
    val latitude: BigDecimal,
    val longitude: BigDecimal
)