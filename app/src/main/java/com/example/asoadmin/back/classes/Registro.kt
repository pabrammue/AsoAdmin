package com.example.asoadmin.back.classes

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@OptIn(InternalSerializationApi::class)
data class Registro(
    val id: Long? = null,
    val id_socio: Long? = null,
    val fechaYHora: String? = null, //Se registra en Supabase como timestamp
    val id_evento: Long? = null
) 