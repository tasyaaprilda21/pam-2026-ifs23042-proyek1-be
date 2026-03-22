package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Review

@Serializable
data class ReviewListResponse(
    val reviews: List<Review>,
    val rataRating: Double
)