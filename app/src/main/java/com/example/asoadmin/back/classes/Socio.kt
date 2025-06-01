package com.example.asoadmin.back.classes

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Socio(
    @SerialName("id")
    val id: Long? = null,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("nSocio")
    val nSocio: Long? = null,

    @SerialName("dni")
    val dni: String? = null,

    @SerialName("tSocio")
    val tSocio: String? = null
)
