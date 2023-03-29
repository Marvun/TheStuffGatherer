package me.marvuun.database.daos

import me.marvuun.database.tables.PlayerSites
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class PlayerSite(id: EntityID<ULong>) : Entity<ULong>(id) {
    companion object : EntityClass<ULong, PlayerSite>(PlayerSites)

    var userId by PlayerSites.id
    var mineId by PlayerSites.mineId
    var lakeId by PlayerSites.lakeId
    var riverId by PlayerSites.riverId
    var forestId by PlayerSites.forestId
    var meadowId by PlayerSites.meadowId

    fun deleteColumnWithUUID(uuid: UUID) =
        transaction {
            when (uuid) {
                mineId -> mineId = null
                lakeId -> lakeId = null
                riverId -> riverId = null
                forestId -> forestId = null
                meadowId -> meadowId = null
                else -> throw Exception("Could not delete player site with uuid $uuid, since it didn't match anything.")
            }
        }
}
