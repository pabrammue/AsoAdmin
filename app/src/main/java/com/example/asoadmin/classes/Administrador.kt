package com.example.asoadmin.classes

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Administrador(
    @SerialName("id")
    val id: Int,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("contraseña")
    val contraseña: String
)