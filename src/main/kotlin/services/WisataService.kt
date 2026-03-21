package org.delcom.services

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.WisataRequest
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IReviewRepository
import org.delcom.repositories.IUserRepository
import org.delcom.repositories.IWisataRepository
import java.io.File
import java.util.*
import org.delcom.data.WisataListResponse

class WisataService(
    private val userRepo: IUserRepository,
    private val wisataRepo: IWisataRepository,
    private val reviewRepo: IReviewRepository,
) {
    suspend fun getAll(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""
        val kategori = call.request.queryParameters["kategori"] ?: ""
        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

        val wisataList = wisataRepo.getAll(search, kategori, page, limit)
        val total = wisataRepo.getTotalCount(search, kategori)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar wisata",
            WisataListResponse(
                wisata = wisataList,
                total = total,
                page = page,
                limit = limit
            )
        )
        call.respond(response)
    }

    suspend fun getById(call: ApplicationCall) {
        val wisataId = call.parameters["id"]
            ?: throw AppException(400, "Data wisata tidak valid!")

        val wisata = wisataRepo.getById(wisataId)
            ?: throw AppException(404, "Data wisata tidak tersedia!")

        val avgRating = reviewRepo.getAverageRating(wisataId)
        val reviews = reviewRepo.getByWisataId(wisataId)

        call.respond(
            mapOf(
                "status" to "success",
                "message" to "Berhasil mengambil data wisata",
                "data" to mapOf(
                    "wisata" to wisata,
                    "rataRating" to avgRating,
                    "reviews" to reviews
                )
            )
        )
    }

    suspend fun post(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<WisataRequest>()
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("nama", "Nama wisata tidak boleh kosong")
        validator.required("lokasi", "Lokasi tidak boleh kosong")
        validator.required("deskripsi", "Deskripsi tidak boleh kosong")
        validator.required("kategori", "Kategori tidak boleh kosong")
        validator.validate()

        val wisataId = wisataRepo.create(request.toEntity())

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data wisata",
            mapOf(Pair("wisataId", wisataId))
        )
        call.respond(response)
    }

    suspend fun put(call: ApplicationCall) {
        val wisataId = call.parameters["id"]
            ?: throw AppException(400, "Data wisata tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<WisataRequest>()
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("nama", "Nama wisata tidak boleh kosong")
        validator.required("lokasi", "Lokasi tidak boleh kosong")
        validator.required("deskripsi", "Deskripsi tidak boleh kosong")
        validator.required("kategori", "Kategori tidak boleh kosong")
        validator.validate()

        val oldWisata = wisataRepo.getById(wisataId)
            ?: throw AppException(404, "Data wisata tidak tersedia!")

        request.foto = oldWisata.foto

        val isUpdated = wisataRepo.update(user.id, wisataId, request.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data wisata!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah data wisata",
            null
        )
        call.respond(response)
    }

    suspend fun putFoto(call: ApplicationCall) {
        val wisataId = call.parameters["id"]
            ?: throw AppException(400, "Data wisata tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = WisataRequest()
        request.userId = user.id

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/wisata/$fileName"

                    val file = File(filePath)
                    file.parentFile.mkdirs()

                    part.provider().copyAndClose(file.writeChannel())
                    request.foto = filePath
                }
                else -> {}
            }
            part.dispose()
        }

        if (request.foto == null) {
            throw AppException(400, "Foto wisata tidak tersedia!")
        }

        val oldWisata = wisataRepo.getById(wisataId)
            ?: throw AppException(404, "Data wisata tidak tersedia!")

        request.nama = oldWisata.nama
        request.lokasi = oldWisata.lokasi
        request.deskripsi = oldWisata.deskripsi
        request.kategori = oldWisata.kategori

        val isUpdated = wisataRepo.update(user.id, wisataId, request.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui foto wisata!")
        }

        if (oldWisata.foto != null) {
            val oldFile = File(oldWisata.foto!!)
            if (oldFile.exists()) oldFile.delete()
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah foto wisata",
            null
        )
        call.respond(response)
    }

    suspend fun delete(call: ApplicationCall) {
        val wisataId = call.parameters["id"]
            ?: throw AppException(400, "Data wisata tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val oldWisata = wisataRepo.getById(wisataId)
            ?: throw AppException(404, "Data wisata tidak tersedia!")

        val isDeleted = wisataRepo.delete(user.id, wisataId)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data wisata!")
        }

        if (oldWisata.foto != null) {
            val oldFile = File(oldWisata.foto!!)
            if (oldFile.exists()) oldFile.delete()
        }

        val response = DataResponse(
            "success",
            "Berhasil menghapus data wisata",
            null
        )
        call.respond(response)
    }
}