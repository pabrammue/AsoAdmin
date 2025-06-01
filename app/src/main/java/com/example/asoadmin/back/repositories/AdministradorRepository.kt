package com.example.asoadmin.back.repositories

import android.content.Context
import com.example.asoadmin.DDBB.supabaseClient
import com.example.asoadmin.back.classes.Administrador
import io.github.jan.supabase.postgrest.postgrest

class AdministradorRepository(private val context: Context) {
    private val client = supabaseClient(context).getClient()

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

    suspend fun obtenerPorId(id: Long): Administrador? {
        return try {
            client.postgrest["Administrador"]
                .select {
                    eq("id", id)
                }
                .decodeSingleOrNull<Administrador>()
        } catch (e: Exception) {
            println("Error al obtener administrador por ID: ${e.message}")
            null
        }
    }

    suspend fun obtenerPorNombreUsuario(nombreUsuario: String): Administrador? {
        return try {
            client.postgrest["Administrador"]
                .select {
                    eq("nombre", nombreUsuario)
                }
                .decodeSingleOrNull<Administrador>()
        } catch (e: Exception) {
            println("Error al obtener administrador por nombre de usuario: ${e.message}")
            null
        }
    }

    suspend fun autenticar(nombreUsuario: String, password: String): Administrador? {
        return try {
            client.postgrest["Administrador"]
                .select {
                    eq("nombre", nombreUsuario)
                    eq("contraseña", password)
                }
                .decodeSingleOrNull<Administrador>()
        } catch (e: Exception) {
            println("Error al autenticar administrador: ${e.message}")
            null
        }
    }
} 