package org.delcom.services

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.ReviewRequest
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IReviewRepository
import org.delcom.repositories.IUserRepository
import org.delcom.repositories.IWisataRepository

class ReviewService(
    private val userRepo: IUserRepository,
    private val wisataRepo: IWisataRepository,
    private val reviewRepo: IReviewRepository,
) {
    suspend fun getByWisataId(call: ApplicationCall) {
        val wisataId = call.parameters["wisataId"]
            ?: throw AppException(400, "Data wisata tidak valid!")

        wisataRepo.getById(wisataId)
            ?: throw AppException(404, "Data wisata tidak tersedia!")

        val reviews = reviewRepo.getByWisataId(wisataId)
        val avgRating = reviewRepo.getAverageRating(wisataId)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar review",
            mapOf(
                "reviews" to reviews,
                "rataRating" to avgRating
            )
        )
        call.respond(response)
    }

    suspend fun post(call: ApplicationCall) {
        val wisataId = call.parameters["wisataId"]
            ?: throw AppException(400, "Data wisata tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        wisataRepo.getById(wisataId)
            ?: throw AppException(404, "Data wisata tidak tersedia!")

        val request = call.receive<ReviewRequest>()
        request.wisataId = wisataId
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("komentar", "Komentar tidak boleh kosong")
        validator.validate()

        if (request.rating < 1 || request.rating > 5) {
            throw AppException(400, "Rating harus antara 1 sampai 5!")
        }

        val reviewId = reviewRepo.create(request.toEntity())

        val response = DataResponse(
            "success",
            "Berhasil menambahkan review",
            mapOf(Pair("reviewId", reviewId))
        )
        call.respond(response)
    }

    suspend fun put(call: ApplicationCall) {
        val reviewId = call.parameters["reviewId"]
            ?: throw AppException(400, "Data review tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<ReviewRequest>()
        request.userId = user.id

        val validator = ValidatorHelper(request.toMap())
        validator.required("komentar", "Komentar tidak boleh kosong")
        validator.validate()

        if (request.rating < 1 || request.rating > 5) {
            throw AppException(400, "Rating harus antara 1 sampai 5!")
        }

        val oldReview = reviewRepo.getById(reviewId)
            ?: throw AppException(404, "Data review tidak tersedia!")

        request.wisataId = oldReview.wisataId

        val isUpdated = reviewRepo.update(user.id, reviewId, request.toEntity())
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui review!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah review",
            null
        )
        call.respond(response)
    }

    suspend fun delete(call: ApplicationCall) {
        val reviewId = call.parameters["reviewId"]
            ?: throw AppException(400, "Data review tidak valid!")

        val user = ServiceHelper.getAuthUser(call, userRepo)

        reviewRepo.getById(reviewId)
            ?: throw AppException(404, "Data review tidak tersedia!")

        val isDeleted = reviewRepo.delete(user.id, reviewId)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus review!")
        }

        val response = DataResponse(
            "success",
            "Berhasil menghapus review",
            null
        )
        call.respond(response)
    }
}