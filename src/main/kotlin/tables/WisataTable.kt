package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object WisataTable : UUIDTable("wisata") {
    val userId = uuid("user_id")
    val nama = varchar("nama", 200)
    val lokasi = varchar("lokasi", 255)
    val deskripsi = text("deskripsi")
    val kategori = varchar("kategori", 100)
    val foto = text("foto").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}