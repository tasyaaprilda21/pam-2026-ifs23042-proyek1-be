package org.delcom.services

import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.AuthRequest
import org.delcom.data.DataResponse
import org.delcom.data.UserResponse
import org.delcom.helpers.ServiceHelper
import org.delcom.helpers.ValidatorHelper
import org.delcom.helpers.hashPassword
import org.delcom.helpers.verifyPassword
import org.delcom.repositories.IRefreshTokenRepository
import org.delcom.repositories.IUserRepository
import java.io.File
import java.util.*

class UserService(
    private val userRepo: IUserRepository,
    private val refreshTokenRepo: IRefreshTokenRepository,
) {
    suspend fun getMe(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val response = DataResponse(
            "success",
            "Berhasil mengambil informasi akun saya",
            mapOf(
                "user" to UserResponse(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                    urlPhoto = user.urlPhoto,
                    createdAt = user.createdAt,
                    updatedAt = user.updatedAt,
                )
            )
        )
        call.respond(response)
    }

    suspend fun putMe(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama tidak boleh kosong")
        validator.required("email", "Email tidak boleh kosong")
        validator.email("email", "Format email tidak valid")
        validator.validate()

        val existUser = userRepo.getByEmail(request.email)
        if (existUser != null && existUser.email != user.email) {
            throw AppException(409, "Akun dengan email ini sudah terdaftar!")
        }

        user.name = request.name
        user.email = request.email
        val isUpdated = userRepo.update(user.id, user)
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data profile!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah data profile",
            null
        )
        call.respond(response)
    }

    suspend fun putMyPhoto(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        var newPhoto: String? = null
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/users/$fileName"

                    val file = File(filePath)
                    file.parentFile.mkdirs()

                    part.provider().copyAndClose(file.writeChannel())
                    newPhoto = filePath
                }
                else -> {}
            }
            part.dispose()
        }

        if (newPhoto == null) {
            throw AppException(404, "Photo profile tidak tersedia!")
        }

        val newFile = File(newPhoto!!)
        if (!newFile.exists()) {
            throw AppException(404, "Photo profile gagal diunggah!")
        }

        val oldPhoto = user.photo
        user.photo = newPhoto

        val isUpdated = userRepo.update(user.id, user)
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui photo profile!")
        }

        if (oldPhoto != null) {
            val oldFile = File(oldPhoto)
            if (oldFile.exists()) oldFile.delete()
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah photo profile",
            null
        )
        call.respond(response)
    }

    suspend fun putMyPassword(call: ApplicationCall) {
        val user = ServiceHelper.getAuthUser(call, userRepo)

        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("password", "Kata sandi lama tidak boleh kosong")
        validator.required("newPassword", "Kata sandi baru tidak boleh kosong")
        validator.minLength("newPassword", 6, "Kata sandi baru minimal 6 karakter")
        validator.validate()

        val validPassword = verifyPassword(request.password, user.password)
        if (!validPassword) {
            throw AppException(404, "Kata sandi lama tidak valid!")
        }

        user.password = hashPassword(request.newPassword)
        val isUpdated = userRepo.update(user.id, user)
        if (!isUpdated) {
            throw AppException(400, "Gagal mengubah kata sandi!")
        }

        refreshTokenRepo.deleteByUserId(user.id)

        val response = DataResponse(
            "success",
            "Berhasil mengubah kata sandi",
            null
        )
        call.respond(response)
    }
}