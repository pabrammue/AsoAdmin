package com.example.asoadmin.back.classes

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Administrador(
    @SerialName("id")
    val id: Long,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("contraseña")
    val contraseña: String
)