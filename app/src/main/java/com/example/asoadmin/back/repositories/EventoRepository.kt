package com.example.asoadmin.back.repositories

import android.content.Context
import com.example.asoadmin.DDBB.supabaseClient
import com.example.asoadmin.back.classes.Evento
import io.github.jan.supabase.postgrest.postgrest
import java.text.SimpleDateFormat
import java.util.Locale

class EventoRepository(private val context: Context) {
    
    private val client = supabaseClient(context).getClient()
    
    // Función auxiliar para formatear fecha para la base de datos
    private fun formatearFechaParaDB(fechaString: String): String {
        return try {
            // Si la fecha ya está en formato YYYY-MM-DD, devolverla tal como está
            if (fechaString.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                fechaString
            } else {
                // Intentar parsear diferentes formatos comunes y convertir a YYYY-MM-DD
                val formatosEntrada = listOf(
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                    SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                )
                val formatoSalida = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                
                for (formato in formatosEntrada) {
                    try {
                        val fecha = formato.parse(fechaString)
                        if (fecha != null) {
                            return formatoSalida.format(fecha)
                        }
                    } catch (e: Exception) {
                        // Continuar con el siguiente formato
                    }
                }
                
                // Si no se puede parsear, devolver la fecha tal como está
                fechaString
            }
        } catch (e: Exception) {
            // En caso de error, devolver la fecha original
            fechaString
        }
    }
    
    /**
     * Obtener todos los eventos
     */
    suspend fun obtenerTodosLosEventos(): List<Evento> {
        return try {
            client.postgrest["Evento"]
                .select()
                .decodeList<Evento>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Obtener evento por ID
     */
    suspend fun obtenerEventoPorId(id: Long): Evento? {
        return try {
            val eventos = client.postgrest["Evento"]
                .select {
                    eq("id", id)
                }
                .decodeList<Evento>()
            eventos.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Crear nuevo evento
     */
    suspend fun crearEvento(evento: Evento): Evento? {
        return try {
            val eventoParaInsertar = Evento(
                id = null, // PostgreSQL auto-generará el ID
                nombre = evento.nombre,
                descripcion = evento.descripcion,
                fecha = formatearFechaParaDB(evento.fecha),
                ubicacion = evento.ubicacion
            )
            
            val response = client.postgrest["Evento"]
                .insert(eventoParaInsertar)
                .decodeList<Evento>()
            
            response.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Actualizar evento existente
     */
    suspend fun actualizarEvento(evento: Evento): Boolean {
        return try {
            val idEvento = evento.id ?: return false
            
            val eventoParaActualizar = Evento(
                id = idEvento,
                nombre = evento.nombre,
                descripcion = evento.descripcion,
                fecha = formatearFechaParaDB(evento.fecha),
                ubicacion = evento.ubicacion
            )
            
            client.postgrest["Evento"]
                .update(eventoParaActualizar) {
                    eq("id", idEvento)
                }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Eliminar evento
     */
    suspend fun eliminarEvento(id: Long): Boolean {
        return try {
            client.postgrest["Evento"]
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