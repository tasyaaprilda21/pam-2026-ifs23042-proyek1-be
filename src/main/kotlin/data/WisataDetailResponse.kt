package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Review
import org.delcom.entities.Wisata

@Serializable
data class WisataDetailResponse(
    val wisata: Wisata,
    val rataRating: Double,
    val reviews: List<Review>
)