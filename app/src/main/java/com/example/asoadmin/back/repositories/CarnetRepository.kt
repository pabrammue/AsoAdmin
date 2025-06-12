package com.example.asoadmin.back.repositories

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.asoadmin.DDBB.supabaseClient
import com.example.asoadmin.back.classes.Carnet
import io.github.jan.supabase.postgrest.postgrest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CarnetRepository(private val context: Context) {
    
    private val client = supabaseClient(context).getClient()
    
    /**
     * Crea un nuevo carnet
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun crearCarnet(idSocio: Long): Carnet? {
        return try {
            val fechaEmision = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            
            val carnetParaInsertar = Carnet(
                id = null, // PostgreSQL auto-generará el ID
                idSocio = idSocio,
                fechaEmision = fechaEmision
            )
            
            val result = client.postgrest["Carnet"]
                .insert(carnetParaInsertar)
                .decodeList<Carnet>()
            
            result.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Obtiene el carnet por ID de socio
     */
    suspend fun obtenerCarnetPorIdSocio(idSocio: Long): Carnet? {
        return try {
            val carnets = client.postgrest["Carnet"]
                .select {
                    eq("idSocio", idSocio)
                }
                .decodeList<Carnet>()
            carnets.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Verifica si un socio ya tiene carnet
     */
    suspend fun socioTieneCarnet(idSocio: Long): Boolean {
        return obtenerCarnetPorIdSocio(idSocio) != null
    }
    
    /**
     * Obtiene todos los carnets
     */
    suspend fun obtenerTodosLosCarnets(): List<Carnet> {
        return try {
            client.postgrest["Carnet"]
                .select()
                .decodeList<Carnet>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Elimina un carnet por ID
     */
    suspend fun eliminarCarnet(id: Long?): Boolean {
        return try {
            if (id == null) {
                return false
            }
            
            client.postgrest["Carnet"]
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