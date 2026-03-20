package org.delcom.dao

import org.delcom.tables.UserTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class UserDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDAO>(UserTable)

    var name by UserTable.name
    var email by UserTable.email
    var password by UserTable.password
    var photo by UserTable.photo
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt
}