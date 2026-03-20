package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Wisata

@Serializable
data class WisataRequest(
    var userId: String = "",
    var nama: String = "",
    var lokasi: String = "",
    var deskripsi: String = "",
    var kategori: String = "",
    var foto: String? = null,
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nama" to nama,
            "lokasi" to lokasi,
            "deskripsi" to deskripsi,
            "kategori" to kategori,
        )
    }

    fun toEntity(): Wisata {
        return Wisata(
            userId = userId,
            nama = nama,
            lokasi = lokasi,
            deskripsi = deskripsi,
            kategori = kategori,
            foto = foto,
            updatedAt = Clock.System.now()
        )
    }
}