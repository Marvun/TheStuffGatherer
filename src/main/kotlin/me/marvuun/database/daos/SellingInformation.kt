package me.marvuun.database.daos

import me.marvuun.database.tables.SellingInformations
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class SellingInformation(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, SellingInformation>(SellingInformations)

    var short by SellingInformations.id
    var baseAmount by SellingInformations.baseAmount
    var basePrice by SellingInformations.basePrice
    var unlockedAtLevel by SellingInformations.unlockedAtLevel
}
