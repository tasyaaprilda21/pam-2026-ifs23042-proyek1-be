package org.delcom.dao

import org.delcom.tables.RefreshTokenTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class RefreshTokenDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RefreshTokenDAO>(RefreshTokenTable)

    var userId by RefreshTokenTable.userId
    var refreshToken by RefreshTokenTable.refreshToken
    var authToken by RefreshTokenTable.authToken
    var createdAt by RefreshTokenTable.createdAt
}