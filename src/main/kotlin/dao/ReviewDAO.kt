package org.delcom.dao

import org.delcom.tables.ReviewTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class ReviewDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ReviewDAO>(ReviewTable)

    var wisataId by ReviewTable.wisataId
    var userId by ReviewTable.userId
    var rating by ReviewTable.rating
    var komentar by ReviewTable.komentar
    var createdAt by ReviewTable.createdAt
    var updatedAt by ReviewTable.updatedAt
}