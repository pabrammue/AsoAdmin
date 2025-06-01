package com.example.asoadmin.back.services

import android.content.Context
import com.example.asoadmin.back.classes.Evento
import com.example.asoadmin.back.classes.Socio
import com.example.asoadmin.back.classes.Asistencia
import com.example.asoadmin.back.repositories.EventoRepository
import com.example.asoadmin.back.repositories.AsistenciaRepository
import com.example.asoadmin.back.repositories.SocioRepository

class EventoService(private val context: Context) {
    
    private val eventoRepository = EventoRepository(context)
    private val asistenciaRepository = AsistenciaRepository(context)
    private val socioRepository = SocioRepository(context)
    
    /**
     * Obtener todos los eventos
     */
    suspend fun obtenerTodosLosEventos(): List<Evento> {
        return eventoRepository.obtenerTodosLosEventos()
    }
    
    /**
     * Obtener evento con detalles de asistencias
     */
    suspend fun obtenerEventoConAsistencias(eventoId: Long): EventoConAsistencias? {
        val evento = eventoRepository.obtenerEventoPorId(eventoId) ?: return null
        val asistencias = asistenciaRepository.obtenerAsistenciasPorEvento(eventoId)
        val sociosParticipantes = asistencias.mapNotNull { asistencia ->
            asistencia.idSocio?.let { socioId ->
                socioRepository.obtenerSocioPorId(socioId)
            }
        }
        
        return EventoConAsistencias(
            evento = evento,
            asistencias = asistencias,
            sociosParticipantes = sociosParticipantes
        )
    }
    
    /**
     * Crear evento con participantes
     */
    suspend fun crearEventoConParticipantes(
        evento: Evento, 
        sociosSeleccionados: Set<Long>
    ): ResultadoOperacion<Evento> {
        try {
            // Crear el evento
            val eventoCreado = eventoRepository.crearEvento(evento)
                ?: return ResultadoOperacion.Error("No se pudo crear el evento")
            
            val eventoId = eventoCreado.id 
                ?: return ResultadoOperacion.Error("El evento creado no tiene ID")
            
            // Crear las asistencias
            val asistenciasCreadas = asistenciaRepository.crearAsistenciasMasivas(
                eventoId, 
                sociosSeleccionados, 
                evento.fecha
            )
            
            return ResultadoOperacion.Exito(
                eventoCreado,
                "Evento creado exitosamente con $asistenciasCreadas participantes"
            )
        } catch (e: Exception) {
            return ResultadoOperacion.Error("Error al crear evento: ${e.message}")
        }
    }
    
    /**
     * Actualizar evento (solo datos básicos)
     */
    suspend fun actualizarEvento(evento: Evento): ResultadoOperacion<Boolean> {
        return try {
            val resultado = eventoRepository.actualizarEvento(evento)
            if (resultado) {
                ResultadoOperacion.Exito(true, "Evento actualizado exitosamente")
            } else {
                ResultadoOperacion.Error("No se pudo actualizar el evento")
            }
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al actualizar evento: ${e.message}")
        }
    }
    
    /**
     * Eliminar evento con todas sus asistencias
     */
    suspend fun eliminarEventoCompleto(eventoId: Long): ResultadoOperacion<Boolean> {
        return try {
            // Primero eliminar todas las asistencias
            asistenciaRepository.eliminarAsistenciasPorEvento(eventoId)
            
            // Luego eliminar el evento
            val resultado = eventoRepository.eliminarEvento(eventoId)
            
            if (resultado) {
                ResultadoOperacion.Exito(true, "Evento y asistencias eliminados exitosamente")
            } else {
                ResultadoOperacion.Error("No se pudo eliminar el evento")
            }
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al eliminar evento: ${e.message}")
        }
    }
    
    /**
     * Agregar participante a evento
     */
    suspend fun agregarParticipante(
        eventoId: Long, 
        socioId: Long, 
        fechaEvento: String
    ): ResultadoOperacion<Boolean> {
        return try {
            // Verificar que no esté ya registrado
            if (asistenciaRepository.verificarAsistencia(eventoId, socioId)) {
                return ResultadoOperacion.Error("El socio ya está registrado en este evento")
            }
            
            val resultado = asistenciaRepository.crearAsistencia(eventoId, socioId, fechaEvento)
            
            if (resultado) {
                ResultadoOperacion.Exito(true, "Participante agregado exitosamente")
            } else {
                ResultadoOperacion.Error("No se pudo agregar el participante")
            }
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al agregar participante: ${e.message}")
        }
    }
    
    /**
     * Remover participante de evento
     */
    suspend fun removerParticipante(eventoId: Long, socioId: Long): ResultadoOperacion<Boolean> {
        return try {
            val resultado = asistenciaRepository.eliminarAsistencia(eventoId, socioId)
            
            if (resultado) {
                ResultadoOperacion.Exito(true, "Participante removido exitosamente")
            } else {
                ResultadoOperacion.Error("No se pudo remover el participante")
            }
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al remover participante: ${e.message}")
        }
    }
    
    /**
     * Obtener IDs de socios participantes en un evento
     */
    suspend fun obtenerSociosParticipantes(eventoId: Long): Set<Long> {
        return try {
            val asistencias = asistenciaRepository.obtenerAsistenciasPorEvento(eventoId)
            asistencias.mapNotNull { it.idSocio }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}

/**
 * Data class para representar un evento con sus asistencias
 */
data class EventoConAsistencias(
    val evento: Evento,
    val asistencias: List<Asistencia>,
    val sociosParticipantes: List<Socio>
)

/**
 * Sealed class para representar resultados de operaciones
 */
sealed class ResultadoOperacion<T> {
    data class Exito<T>(val datos: T, val mensaje: String) : ResultadoOperacion<T>()
    data class Error<T>(val mensaje: String) : ResultadoOperacion<T>()
} 