package com.example.asoadmin.back.repositories

import android.content.Context
import com.example.asoadmin.DDBB.supabaseClient
import com.example.asoadmin.back.classes.Registro
import io.github.jan.supabase.postgrest.postgrest

class RegistroRepository(private val context: Context) {
    
    private val client = supabaseClient(context).getClient()
    
    /**
     * Crea un nuevo registro de lectura de tarjeta
     */
    suspend fun crearRegistro(idSocio: Long, idEvento: Long): Registro? {
        return try {
            val registroParaInsertar = Registro(
                id = null, // PostgreSQL auto-generará el ID
                id_socio = idSocio,
                fechaYHora = null, // PostgreSQL usará now() por defecto
                id_evento = idEvento
            )
            
            val result = client.postgrest["Registro"]
                .insert(registroParaInsertar)
                .decodeList<Registro>()
            
            result.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Obtiene todos los registros de un evento
     */
    suspend fun obtenerRegistrosPorEvento(idEvento: Long): List<Registro> {
        return try {
            client.postgrest["Registro"]
                .select {
                    eq("id_evento", idEvento)
                }
                .decodeList<Registro>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Obtiene todos los registros de un socio
     */
    suspend fun obtenerRegistrosPorSocio(idSocio: Long): List<Registro> {
        return try {
            client.postgrest["Registro"]
                .select {
                    eq("id_socio", idSocio)
                }
                .decodeList<Registro>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    
    /**
     * Verifica si ya existe un registro para un socio en un evento concreto
     */
    suspend fun existeRegistro(idSocio: Long, idEvento: Long): Boolean {
        return try {
            val registros = client.postgrest["Registro"]
                .select {
                    eq("id_socio", idSocio)
                    eq("id_evento", idEvento)
                }
                .decodeList<Registro>()
            
            registros.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    

} 