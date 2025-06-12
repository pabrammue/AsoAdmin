package com.example.asoadmin.back.repositories

import android.content.Context
import com.example.asoadmin.DDBB.supabaseClient
import com.example.asoadmin.back.classes.Administrador
import io.github.jan.supabase.postgrest.postgrest

class AdministradorRepository(private val context: Context) {
    private val client = supabaseClient(context).getClient()

    /**
     * Recibe todos los administradores de la base de datos para comprobar si las credenciales son correctas en la pantalla de LogIn
     */
    suspend fun obtenerTodos(): List<Administrador> {
        return try {
            client.postgrest["Administrador"]
                .select()
                .decodeList<Administrador>()
        } catch (e: Exception) {
            println("Error al obtener administradores: ${e.message}")
            emptyList()
        }
    }
} 