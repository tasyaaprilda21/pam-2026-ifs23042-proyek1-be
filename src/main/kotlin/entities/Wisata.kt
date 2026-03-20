package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Wisata(
    var id: String = UUID.randomUUID().toString(),
    var userId: String,
    var nama: String,
    var lokasi: String,
    var deskripsi: String,
    var kategori: String,
    var foto: String? = null,
    var urlFoto: String = "",

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)