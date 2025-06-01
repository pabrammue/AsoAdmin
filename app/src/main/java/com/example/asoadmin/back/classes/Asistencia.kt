package com.example.asoadmin.back.classes

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Asistencia(
    @SerialName("id")
    val id: Long? = null,

    @SerialName("idEvento")
    val idEvento: Long,

    @SerialName("idSocio")
    val idSocio: Long? = null,

    @SerialName("fechaEvento")
    val fechaEvento: String? = null
)
