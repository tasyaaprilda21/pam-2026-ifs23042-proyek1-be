package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.User

@Serializable
data class AuthRequest(
    var name: String = "",
    var email: String = "",
    var password: String = "",
    var newPassword: String = "",
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "email" to email,
            "password" to password,
            "newPassword" to newPassword
        )
    }

    fun toEntity(): User {
        return User(
            name = name,
            email = email,
            password = password,
            updatedAt = Clock.System.now()
        )
    }
}