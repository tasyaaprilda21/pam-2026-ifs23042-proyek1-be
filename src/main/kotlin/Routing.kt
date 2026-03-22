package org.delcom

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.delcom.data.AppException
import org.delcom.data.ErrorResponse
import org.delcom.helpers.JWTConstants
import org.delcom.helpers.parseMessageToMap
import org.delcom.services.AuthService
import org.delcom.services.ReviewService
import org.delcom.services.UserService
import org.delcom.services.WisataService
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authService: AuthService by inject()
    val userService: UserService by inject()
    val wisataService: WisataService by inject()
    val reviewService: ReviewService by inject()

    install(StatusPages) {
        exception<AppException> { call, cause ->
            val dataMap: Map<String, List<String>> = parseMessageToMap(cause.message)
            call.respond(
                status = HttpStatusCode.fromValue(cause.code),
                message = ErrorResponse(
                    status = "fail",
                    message = if (dataMap.isEmpty()) cause.message else "Data yang dikirimkan tidak valid!",
                    data = if (dataMap.isEmpty()) null else dataMap.toString()
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.respond(
                status = HttpStatusCode.fromValue(500),
                message = ErrorResponse(
                    status = "error",
                    message = cause.message ?: "Unknown error",
                    data = ""
                )
            )
        }
    }

    routing {
        get("/") {
            call.respondText("API Wisata telah berjalan!")
        }

        // Auth
        route("/auth") {
            post("/register") { authService.postRegister(call) }
            post("/login") { authService.postLogin(call) }
            post("/refresh-token") { authService.postRefreshToken(call) }
            post("/logout") { authService.postLogout(call) }
        }

        // PUBLIC - tanpa token
        route("/wisata") {
            get { wisataService.getAll(call) }
            get("/{id}") { wisataService.getById(call) }
        }

        // PUBLIC - reviews
        route("/wisata/{wisataId}/reviews") {
            get { reviewService.getByWisataId(call) }
        }

        authenticate(JWTConstants.NAME) {
            // Users
            route("/users") {
                get("/me") { userService.getMe(call) }
                put("/me") { userService.putMe(call) }
                put("/me/password") { userService.putMyPassword(call) }
                put("/me/photo") { userService.putMyPhoto(call) }
            }

            // Wisata private
            route("/wisata") {
                post { wisataService.post(call) }
                put("/{id}") { wisataService.put(call) }
                put("/{id}/foto") { wisataService.putFoto(call) }
                delete("/{id}") { wisataService.delete(call) }
            }

            // Reviews private
            route("/wisata/{wisataId}/reviews") {
                post { reviewService.post(call) }
                put("/{reviewId}") { reviewService.put(call) }
                delete("/{reviewId}") { reviewService.delete(call) }
            }
        }
    }
}
