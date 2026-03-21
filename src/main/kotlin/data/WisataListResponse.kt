package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Wisata

@Serializable
data class WisataListResponse(
    val wisata: List<Wisata>,
    val total: Long,
    val page: Int,
    val limit: Int
)