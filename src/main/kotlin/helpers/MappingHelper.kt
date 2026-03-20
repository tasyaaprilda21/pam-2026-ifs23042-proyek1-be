package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.RefreshTokenDAO
import org.delcom.dao.UserDAO
import org.delcom.dao.WisataDAO
import org.delcom.dao.ReviewDAO
import org.delcom.entities.RefreshToken
import org.delcom.entities.User
import org.delcom.entities.Wisata
import org.delcom.entities.Review
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun userDAOToModel(dao: UserDAO, baseUrl: String) = User(
    id = dao.id.value.toString(),
    name = dao.name,
    email = dao.email,
    password = dao.password,
    photo = dao.photo,
    urlPhoto = buildImageUrl(baseUrl, dao.photo ?: "/uploads/defaults/user.png"),
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    id = dao.id.value.toString(),
    userId = dao.userId.toString(),
    refreshToken = dao.refreshToken,
    authToken = dao.authToken,
    createdAt = dao.createdAt,
)

fun wisataDAOToModel(dao: WisataDAO, baseUrl: String) = Wisata(
    id = dao.id.value.toString(),
    userId = dao.userId.toString(),
    nama = dao.nama,
    lokasi = dao.lokasi,
    deskripsi = dao.deskripsi,
    kategori = dao.kategori,
    foto = dao.foto,
    urlFoto = buildImageUrl(baseUrl, dao.foto ?: "/uploads/defaults/wisata.png"),
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

fun reviewDAOToModel(dao: ReviewDAO) = Review(
    id = dao.id.value.toString(),
    wisataId = dao.wisataId.toString(),
    userId = dao.userId.toString(),
    rating = dao.rating,
    komentar = dao.komentar,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

fun buildImageUrl(baseUrl: String, pathGambar: String): String {
    val relativePath = pathGambar.removePrefix("uploads/")
    return "$baseUrl/static/$relativePath"
}