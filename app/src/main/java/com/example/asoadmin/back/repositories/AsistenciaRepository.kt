package com.example.asoadmin.back.repositories

import android.content.Context
import com.example.asoadmin.DDBB.supabaseClient
import com.example.asoadmin.back.classes.Asistencia
import io.github.jan.supabase.postgrest.postgrest
import java.text.SimpleDateFormat
import java.util.Locale

class AsistenciaRepository(private val context: Context) {
    
    private val client = supabaseClient(context).getClient()
    
    /**
     * Función auxiliar para formatear fecha para la base de datos
     */

    private fun formatearFechaParaDB(fechaString: String): String {
        return try {
            //Si la fecha ya está en formato YYYY-MM-DD, devolverla tal como está
            if (fechaString.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                fechaString
            } else {
                //Intentar parsear diferentes formatos comunes y convertir a YYYY-MM-DD
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
                    }
                }
                
                fechaString
            }
        } catch (e: Exception) {
            fechaString
        }
    }
    
    /**
     * Reccibe las asistencias asociadas a un evento
     */
    suspend fun obtenerAsistenciasPorEvento(eventoId: Long): List<Asistencia> {
        return try {
            client.postgrest["Asistencia"]
                .select {
                    eq("idEvento", eventoId)
                }
                .decodeList<Asistencia>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    
    /**
     * Crea una nueva asistencia a un evento
     */
    suspend fun crearAsistencia(eventoId: Long, socioId: Long, fechaEvento: String): Boolean {
        return try {
            val nuevaAsistencia = Asistencia(
                id = null, // PostgreSQL auto-generará el ID
                idEvento = eventoId,
                idSocio = socioId,
                fechaEvento = formatearFechaParaDB(fechaEvento)
            )
            
            client.postgrest["Asistencia"]
                .insert(nuevaAsistencia)
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Elimina una asistencia específica de un socio a un evento
     */
    suspend fun eliminarAsistencia(eventoId: Long, socioId: Long): Boolean {
        return try {
            client.postgrest["Asistencia"]
                .delete {
                    eq("idEvento", eventoId)
                    eq("idSocio", socioId)
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Elimina todas las asistencias de un evento
     */
    suspend fun eliminarAsistenciasPorEvento(eventoId: Long): Boolean {
        return try {
            client.postgrest["Asistencia"]
                .delete {
                    eq("idEvento", eventoId)
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Verifica si un socio está registrado en un evento
     */
    suspend fun verificarAsistencia(eventoId: Long, socioId: Long): Boolean {
        return try {
            val asistencias = client.postgrest["Asistencia"]
                .select {
                    eq("idEvento", eventoId)
                    eq("idSocio", socioId)
                }
                .decodeList<Asistencia>()
            
            asistencias.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Crea múltiples asistencias de una vez
     */
    suspend fun crearAsistenciasMasivas(eventoId: Long, sociosIds: Set<Long>, fechaEvento: String): Int {
        var exitosas = 0
        sociosIds.forEach { socioId ->
            if (crearAsistencia(eventoId, socioId, fechaEvento)) {
                exitosas++
            }
        }
        return exitosas
    }
} 