package com.example.asoadmin.classes

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Carnet(
    @SerialName("id")
    val id: Int,

    @SerialName("idSocio")
    val idSocio: Int,

    @SerialName("fechaEmision")
    val fechaEmision: String? = null
)
