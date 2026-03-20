package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Review

@Serializable
data class ReviewRequest(
    var wisataId: String = "",
    var userId: String = "",
    var rating: Int = 0,
    var komentar: String = "",
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "rating" to rating,
            "komentar" to komentar,
        )
    }

    fun toEntity(): Review {
        return Review(
            wisataId = wisataId,
            userId = userId,
            rating = rating,
            komentar = komentar,
            updatedAt = Clock.System.now()
        )
    }
}