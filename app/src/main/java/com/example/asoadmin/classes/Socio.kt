package com.example.asoadmin.classes

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Socio(
    @SerialName("id")
    val id: Int,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("nSocio")
    val nSocio: Int? = null,

    @SerialName("dni")
    val dni: String? = null,

    @SerialName("tSocio")
    val tSocio: String? = null
)
