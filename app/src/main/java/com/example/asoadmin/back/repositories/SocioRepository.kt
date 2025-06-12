package com.example.asoadmin.back.repositories

import android.content.Context
import com.example.asoadmin.DDBB.supabaseClient
import com.example.asoadmin.back.classes.Socio
import io.github.jan.supabase.postgrest.postgrest

class SocioRepository(private val context: Context) {
    
    private val client = supabaseClient(context).getClient()
    
    /**
     * Obtiene todos los socios
     */
    suspend fun obtenerTodosLosSocios(): List<Socio> {
        return try {
            client.postgrest["Socio"]
                .select()
                .decodeList<Socio>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Obtiene socio por ID
     */
    suspend fun obtenerSocioPorId(id: Long): Socio? {
        return try {
            val socios = client.postgrest["Socio"]
                .select {
                    eq("id", id)
                }
                .decodeList<Socio>()
            socios.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Filtra los socios por criterios para la búsqueda
     */
    suspend fun buscarSocios(query: String): List<Socio> {
        return try {
            val todosLosSocios = obtenerTodosLosSocios()
            
            if (query.isBlank()) {
                todosLosSocios
            } else {
                todosLosSocios.filter { socio ->
                    socio.nombre.contains(query, ignoreCase = true) ||
                    socio.nSocio?.toString()?.contains(query) == true ||
                    socio.dni?.contains(query, ignoreCase = true) == true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Obtiene un socio por su número de socio
     */
    suspend fun obtenerSocioPorNumero(nSocio: Long): Socio? {
        return try {
            val socios = client.postgrest["Socio"]
                .select {
                    eq("nSocio", nSocio)
                }
                .decodeList<Socio>()
            socios.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Obtiene un socio por su DNI
     */
    suspend fun obtenerSocioPorDni(dni: String): Socio? {
        return try {
            val socios = client.postgrest["Socio"]
                .select {
                    eq("dni", dni)
                }
                .decodeList<Socio>()
            socios.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
} 