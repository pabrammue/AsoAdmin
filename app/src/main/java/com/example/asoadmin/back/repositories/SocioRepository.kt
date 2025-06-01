package com.example.asoadmin.back.repositories

import android.content.Context
import com.example.asoadmin.DDBB.supabaseClient
import com.example.asoadmin.back.classes.Socio
import io.github.jan.supabase.postgrest.postgrest

class SocioRepository(private val context: Context) {
    
    private val client = supabaseClient(context).getClient()
    
    /**
     * Obtener todos los socios
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
     * Obtener socio por ID
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
     * Buscar socios por criterios
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
     * Obtener socio por número de socio
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
     * Obtener socio por DNI
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
    
    /**
     * Crear nuevo socio
     */
    suspend fun crearSocio(socio: Socio): Socio? {
        return try {
            val socioParaInsertar = Socio(
                id = null, // PostgreSQL auto-generará el ID
                nombre = socio.nombre,
                nSocio = socio.nSocio,
                dni = socio.dni,
                tSocio = socio.tSocio
            )
            
            val response = client.postgrest["Socio"]
                .insert(socioParaInsertar)
                .decodeList<Socio>()
            
            response.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Actualizar socio existente
     */
    suspend fun actualizarSocio(socio: Socio): Boolean {
        return try {
            val idSocio = socio.id ?: return false
            
            client.postgrest["Socio"]
                .update(socio) {
                    eq("id", idSocio)
                }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Eliminar socio
     */
    suspend fun eliminarSocio(id: Long): Boolean {
        return try {
            client.postgrest["Socio"]
                .delete {
                    eq("id", id)
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
} 