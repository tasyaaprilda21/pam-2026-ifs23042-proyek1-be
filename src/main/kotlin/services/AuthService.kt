package org.delcom.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.delcom.data.AppException
import org.delcom.data.AuthRequest
import org.delcom.data.DataResponse
import org.delcom.data.RefreshTokenRequest
import org.delcom.entities.RefreshToken
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.ValidatorHelper
import org.delcom.helpers.hashPassword
import org.delcom.helpers.verifyPassword
import org.delcom.repositories.IRefreshTokenRepository
import org.delcom.repositories.IUserRepository
import java.util.*

class AuthService(
    private val jwtSecret: String,
    private val userRepository: IUserRepository,
    private val refreshTokenRepository: IRefreshTokenRepository,
) {
    suspend fun postRegister(call: ApplicationCall) {
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("name", "Nama tidak boleh kosong")
        validator.required("email", "Email tidak boleh kosong")
        validator.email("email", "Format email tidak valid")
        validator.required("password", "Password tidak boleh kosong")
        validator.minLength("password", 6, "Password minimal 6 karakter")
        validator.validate()

        val existUser = userRepository.getByEmail(request.email)
        if (existUser != null) {
            throw AppException(409, "Akun dengan email ini sudah terdaftar!")
        }

        request.password = hashPassword(request.password)
        val userId = userRepository.create(request.toEntity())

        val response = DataResponse(
            "success",
            "Berhasil melakukan pendaftaran",
            mapOf(Pair("userId", userId))
        )
        call.respond(response)
    }

    suspend fun postLogin(call: ApplicationCall) {
        val request = call.receive<AuthRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("email", "Email tidak boleh kosong")
        validator.required("password", "Password tidak boleh kosong")
        validator.validate()

        val existUser = userRepository.getByEmail(request.email)
            ?: throw AppException(404, "Kredensial yang digunakan tidak valid!")

        val validPassword = verifyPassword(request.password, existUser.password)
        if (!validPassword) {
            throw AppException(404, "Kredensial yang digunakan tidak valid!")
        }

        val authToken = JWT.create()
            .withAudience(JWTConstants.AUDIENCE)
            .withIssuer(JWTConstants.ISSUER)
            .withClaim("userId", existUser.id)
            .withExpiresAt(Date(System.currentTimeMillis() + 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(jwtSecret))

        refreshTokenRepository.deleteByUserId(existUser.id)

        val strRefreshToken = UUID.randomUUID().toString()
        refreshTokenRepository.create(
            RefreshToken(
                userId = existUser.id,
                authToken = authToken,
                refreshToken = strRefreshToken
            )
        )

        val response = DataResponse(
            "success",
            "Berhasil melakukan login",
            mapOf(
                Pair("authToken", authToken),
                Pair("refreshToken", strRefreshToken)
            )
        )
        call.respond(response)
    }

    suspend fun postRefreshToken(call: ApplicationCall) {
        val request = call.receive<RefreshTokenRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("refreshToken", "Refresh Token tidak boleh kosong")
        validator.required("authToken", "Auth Token tidak boleh kosong")
        validator.validate()

        val existRefreshToken = refreshTokenRepository.getByToken(
            refreshToken = request.refreshToken,
            authToken = request.authToken
        )

        refreshTokenRepository.delete(request.authToken)

        if (existRefreshToken == null) {
            throw AppException(401, "Token tidak valid!")
        }

        val userId = existRefreshToken.userId
        val user = userRepository.getById(userId)
            ?: throw AppException(404, "User tidak valid!")

        val authToken = JWT.create()
            .withAudience(JWTConstants.AUDIENCE)
            .withIssuer(JWTConstants.ISSUER)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + 60 * 60 * 1000))
            .sign(Algorithm.HMAC256(jwtSecret))

        val strRefreshToken = UUID.randomUUID().toString()
        refreshTokenRepository.create(
            RefreshToken(
                userId = user.id,
                authToken = authToken,
                refreshToken = strRefreshToken
            )
        )

        val response = DataResponse(
            "success",
            "Berhasil melakukan refresh token",
            mapOf(
                Pair("authToken", authToken),
                Pair("refreshToken", strRefreshToken)
            )
        )
        call.respond(response)
    }

    suspend fun postLogout(call: ApplicationCall) {
        val request = call.receive<RefreshTokenRequest>()

        val validator = ValidatorHelper(request.toMap())
        validator.required("authToken", "Auth Token tidak boleh kosong")
        validator.validate()

        val decodedJWT = JWT.require(Algorithm.HMAC256(jwtSecret))
            .build()
            .verify(request.authToken)

        val userId = decodedJWT
            .getClaim("userId")
            .asString() ?: throw AppException(401, "Token tidak valid")

        refreshTokenRepository.delete(request.authToken)
        refreshTokenRepository.deleteByUserId(userId)

        val response = DataResponse(
            "success",
            "Berhasil logout",
            null,
        )
        call.respond(response)
    }
}