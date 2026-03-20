package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var urlPhoto: String = "",
    @Contextual
    var createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)