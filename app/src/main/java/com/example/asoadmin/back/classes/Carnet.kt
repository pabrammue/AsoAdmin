package com.example.asoadmin.back.classes

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class Carnet(
    @SerialName("id")
    val id: Long? = null,

    @SerialName("idSocio")
    val idSocio: Long,

    @SerialName("fechaEmision")
    val fechaEmision: String? = null
)
