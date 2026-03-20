package org.delcom.dao

import org.delcom.tables.WisataTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class WisataDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<WisataDAO>(WisataTable)

    var userId by WisataTable.userId
    var nama by WisataTable.nama
    var lokasi by WisataTable.lokasi
    var deskripsi by WisataTable.deskripsi
    var kategori by WisataTable.kategori
    var foto by WisataTable.foto
    var createdAt by WisataTable.createdAt
    var updatedAt by WisataTable.updatedAt
}