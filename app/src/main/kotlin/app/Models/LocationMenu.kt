package app.Models

import io.ktor.auth.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object LocationMenuTable : IntIdTable("location_menus") {
    val locationID = integer("location_id")

    val name       = text("name")
    val imageURL   = text("image_url")
    val price      = integer("price")
}

class LocationMenuEntity(id: EntityID<Int>) : IntEntity(id), Principal {
    companion object : IntEntityClass<LocationMenuEntity>(LocationMenuTable)

    val locationID by LocationMenuTable.locationID

    val name       by LocationMenuTable.name
    val imageURL   by LocationMenuTable.imageURL
    val price      by LocationMenuTable.price

    fun toRespondInfo() = LocationMenuRespond(
        id       = this.id.value,
        name     = this.name,
        imageURL = this.imageURL,
        price    = this.price
    )
}

data class LocationMenuRespond(
    val id: Int,
    val name: String,
    val imageURL: String,
    val price: Int
)