package com.example.asoadmin.back.services

import android.content.Context
import com.example.asoadmin.back.classes.Socio
import com.example.asoadmin.back.repositories.SocioRepository

class SocioService(private val context: Context) {
    
    private val socioRepository = SocioRepository(context)
    
    /**
     * Obtiene todos los socios
     */
    suspend fun obtenerTodosLosSocios(): List<Socio> {
        return socioRepository.obtenerTodosLosSocios()
    }
} 