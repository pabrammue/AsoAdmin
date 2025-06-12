package com.example.asoadmin.back.classes

import kotlinx.serialization.Serializable

@Serializable
data class Registro(
    val id: Long? = null,
    val id_socio: Long? = null,
    val fechaYHora: String? = null, // Timestamp como string para compatibilidad con Supabase
    val id_evento: Long? = null
) 