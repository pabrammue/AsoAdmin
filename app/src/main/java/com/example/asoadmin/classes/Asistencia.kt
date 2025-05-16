package com.example.asoadmin.classes

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Asistencia(
    @SerialName("id")
    val id: Int,

    @SerialName("idEvento")
    val idEvento: Int,

    @SerialName("idSocio")
    val idSocio: Int? = null,

    @SerialName("fechaEvento")
    val fechaEvento: String? = null
)
