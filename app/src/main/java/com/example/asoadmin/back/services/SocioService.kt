package com.example.asoadmin.back.services

import android.content.Context
import com.example.asoadmin.back.classes.Socio
import com.example.asoadmin.back.repositories.SocioRepository

class SocioService(private val context: Context) {
    
    private val socioRepository = SocioRepository(context)
    
    /**
     * Obtener todos los socios
     */
    suspend fun obtenerTodosLosSocios(): List<Socio> {
        return socioRepository.obtenerTodosLosSocios()
    }
    
    /**
     * Buscar socios por criterios
     */
    suspend fun buscarSocios(query: String): List<Socio> {
        return socioRepository.buscarSocios(query)
    }
    
    /**
     * Obtener socio por ID
     */
    suspend fun obtenerSocioPorId(id: Long): Socio? {
        return socioRepository.obtenerSocioPorId(id)
    }
    
    /**
     * Obtener socio por número de socio
     */
    suspend fun obtenerSocioPorNumero(nSocio: Long): Socio? {
        return socioRepository.obtenerSocioPorNumero(nSocio)
    }
    
    /**
     * Obtener socio por DNI
     */
    suspend fun obtenerSocioPorDni(dni: String): Socio? {
        return socioRepository.obtenerSocioPorDni(dni)
    }
} 