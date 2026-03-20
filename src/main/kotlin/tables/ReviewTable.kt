package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ReviewTable : UUIDTable("reviews") {
    val wisataId = uuid("wisata_id")
    val userId = uuid("user_id")
    val rating = integer("rating")
    val komentar = text("komentar")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}