package com.example.asoadmin.classes

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
@Serializable
data class Evento(
    @SerialName("id")
    val id: Int,

    @SerialName("nombre")
    val nombre: String,

    @SerialName("descripcion")
    val descripcion: String,

    @SerialName("fecha")
    val fecha: String,

    @SerialName("ubicacion")
    val ubicacion: String
)