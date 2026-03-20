package org.delcom.repositories

import org.delcom.dao.WisataDAO
import org.delcom.entities.Wisata
import org.delcom.helpers.suspendTransaction
import org.delcom.helpers.wisataDAOToModel
import org.delcom.tables.WisataTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

class WisataRepository(private val baseUrl: String) : IWisataRepository {
    override suspend fun getAll(
        search: String,
        kategori: String,
        page: Int,
        limit: Int
    ): List<Wisata> = suspendTransaction {
        var query = WisataTable.selectAll()

        if (search.isNotBlank() && kategori.isNotBlank()) {
            query = WisataTable.selectAll().where {
                (WisataTable.nama.lowerCase() like "%${search.lowercase()}%") and
                        (WisataTable.kategori eq kategori)
            }
        } else if (search.isNotBlank()) {
            query = WisataTable.selectAll().where {
                WisataTable.nama.lowerCase() like "%${search.lowercase()}%"
            }
        } else if (kategori.isNotBlank()) {
            query = WisataTable.selectAll().where {
                WisataTable.kategori eq kategori
            }
        }

        query
            .orderBy(WisataTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map { row ->
                wisataDAOToModel(WisataDAO.wrapRow(row), baseUrl)
            }
    }

    override suspend fun getById(wisataId: String): Wisata? = suspendTransaction {
        WisataDAO
            .find { WisataTable.id eq UUID.fromString(wisataId) }
            .limit(1)
            .map { wisataDAOToModel(it, baseUrl) }
            .firstOrNull()
    }

    override suspend fun create(wisata: Wisata): String = suspendTransaction {
        val dao = WisataDAO.new {
            userId = UUID.fromString(wisata.userId)
            nama = wisata.nama
            lokasi = wisata.lokasi
            deskripsi = wisata.deskripsi
            kategori = wisata.kategori
            foto = wisata.foto
            createdAt = wisata.createdAt
            updatedAt = wisata.updatedAt
        }
        dao.id.value.toString()
    }

    override suspend fun update(userId: String, wisataId: String, newWisata: Wisata): Boolean = suspendTransaction {
        val dao = WisataDAO
            .find {
                (WisataTable.id eq UUID.fromString(wisataId)) and
                        (WisataTable.userId eq UUID.fromString(userId))
            }
            .limit(1)
            .firstOrNull()

        if (dao != null) {
            dao.nama = newWisata.nama
            dao.lokasi = newWisata.lokasi
            dao.deskripsi = newWisata.deskripsi
            dao.kategori = newWisata.kategori
            dao.foto = newWisata.foto
            dao.updatedAt = newWisata.updatedAt
            true
        } else false
    }

    override suspend fun delete(userId: String, wisataId: String): Boolean = suspendTransaction {
        val rowsDeleted = WisataTable.deleteWhere {
            (WisataTable.id eq UUID.fromString(wisataId)) and
                    (WisataTable.userId eq UUID.fromString(userId))
        }
        rowsDeleted >= 1
    }

    override suspend fun getTotalCount(search: String, kategori: String): Long = suspendTransaction {
        var query = WisataTable.selectAll()

        if (search.isNotBlank() && kategori.isNotBlank()) {
            query = WisataTable.selectAll().where {
                (WisataTable.nama.lowerCase() like "%${search.lowercase()}%") and
                        (WisataTable.kategori eq kategori)
            }
        } else if (search.isNotBlank()) {
            query = WisataTable.selectAll().where {
                WisataTable.nama.lowerCase() like "%${search.lowercase()}%"
            }
        } else if (kategori.isNotBlank()) {
            query = WisataTable.selectAll().where {
                WisataTable.kategori eq kategori
            }
        }

        query.count()
    }
}