package org.delcom.repositories

import org.delcom.entities.Wisata

interface IWisataRepository {
    suspend fun getAll(
        search: String,
        kategori: String,
        page: Int,
        limit: Int
    ): List<Wisata>
    suspend fun getById(wisataId: String): Wisata?
    suspend fun create(wisata: Wisata): String
    suspend fun update(userId: String, wisataId: String, newWisata: Wisata): Boolean
    suspend fun delete(userId: String, wisataId: String): Boolean
    suspend fun getTotalCount(search: String, kategori: String): Long
}