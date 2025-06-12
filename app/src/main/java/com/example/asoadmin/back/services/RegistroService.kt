package com.example.asoadmin.back.services

import android.content.Context
import com.example.asoadmin.back.classes.Registro
import com.example.asoadmin.back.classes.Socio
import com.example.asoadmin.back.classes.Evento
import com.example.asoadmin.back.repositories.RegistroRepository
import com.example.asoadmin.back.repositories.SocioRepository
import com.example.asoadmin.back.repositories.EventoRepository

class RegistroService(private val context: Context) {
    
    private val registroRepository = RegistroRepository(context)
    private val socioRepository = SocioRepository(context)
    private val eventoRepository = EventoRepository(context)
    
    /**
     * Registrar la lectura de una tarjeta NFC
     */
    suspend fun registrarLecturaCarnet(idSocio: Long, idEvento: Long): ResultadoOperacion<Registro> {
        return try {
            // Verificar que el socio existe
            val socio = socioRepository.obtenerSocioPorId(idSocio)
            if (socio == null) {
                return ResultadoOperacion.Error("El socio con ID $idSocio no existe")
            }
            
            // Verificar que el evento existe
            val evento = eventoRepository.obtenerEventoPorId(idEvento)
            if (evento == null) {
                return ResultadoOperacion.Error("El evento con ID $idEvento no existe")
            }
            
            // Crear el registro
            val registro = registroRepository.crearRegistro(idSocio, idEvento)
            if (registro != null) {
                ResultadoOperacion.Exito(
                    registro, 
                    "Registro de lectura creado para ${socio.nombre} en evento ${evento.nombre}"
                )
            } else {
                ResultadoOperacion.Error("No se pudo crear el registro de lectura")
            }
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al registrar lectura: ${e.message}")
        }
    }
    
    /**
     * Obtener historial de registros de un evento
     */
    suspend fun obtenerHistorialEvento(idEvento: Long): ResultadoOperacion<List<RegistroConDetalles>> {
        return try {
            val registros = registroRepository.obtenerRegistrosPorEvento(idEvento)
            val registrosConDetalles = mutableListOf<RegistroConDetalles>()
            
            for (registro in registros) {
                val socio = registro.id_socio?.let { socioRepository.obtenerSocioPorId(it) }
                val evento = registro.id_evento?.let { eventoRepository.obtenerEventoPorId(it) }
                
                registrosConDetalles.add(
                    RegistroConDetalles(
                        registro = registro,
                        socio = socio,
                        evento = evento
                    )
                )
            }
            
            ResultadoOperacion.Exito(registrosConDetalles, "Historial obtenido exitosamente")
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al obtener historial: ${e.message}")
        }
    }
    
    /**
     * Obtener historial de registros de un socio
     */
    suspend fun obtenerHistorialSocio(idSocio: Long): ResultadoOperacion<List<RegistroConDetalles>> {
        return try {
            val registros = registroRepository.obtenerRegistrosPorSocio(idSocio)
            val registrosConDetalles = mutableListOf<RegistroConDetalles>()
            
            for (registro in registros) {
                val socio = registro.id_socio?.let { socioRepository.obtenerSocioPorId(it) }
                val evento = registro.id_evento?.let { eventoRepository.obtenerEventoPorId(it) }
                
                registrosConDetalles.add(
                    RegistroConDetalles(
                        registro = registro,
                        socio = socio,
                        evento = evento
                    )
                )
            }
            
            ResultadoOperacion.Exito(registrosConDetalles, "Historial obtenido exitosamente")
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al obtener historial: ${e.message}")
        }
    }
    
    /**
     * Verificar si ya existe un registro para un socio en un evento
     */
    suspend fun yaExisteRegistro(idSocio: Long, idEvento: Long): Boolean {
        return try {
            registroRepository.existeRegistro(idSocio, idEvento)
        } catch (e: Exception) {
            false
        }
    }
    

    
    /**
     * Obtener estadísticas de registros de un evento
     */
    suspend fun obtenerEstadisticasEvento(idEvento: Long): ResultadoOperacion<EstadisticasRegistro> {
        return try {
            val registros = registroRepository.obtenerRegistrosPorEvento(idEvento)
            val sociosUnicos = registros.mapNotNull { it.id_socio }.distinct()
            
            val estadisticas = EstadisticasRegistro(
                totalRegistros = registros.size,
                sociosUnicos = sociosUnicos.size,
                primeraLectura = registros.minByOrNull { it.fechaYHora ?: "" }?.fechaYHora,
                ultimaLectura = registros.maxByOrNull { it.fechaYHora ?: "" }?.fechaYHora
            )
            
            ResultadoOperacion.Exito(estadisticas, "Estadísticas calculadas exitosamente")
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al calcular estadísticas: ${e.message}")
        }
    }
}

/**
 * Clase de datos para combinar registro con información de socio y evento
 */
data class RegistroConDetalles(
    val registro: Registro,
    val socio: Socio? = null,
    val evento: Evento? = null
) {
    val nombreSocio: String get() = socio?.nombre ?: "Socio desconocido"
    val numeroSocio: Long? get() = socio?.nSocio
    val nombreEvento: String get() = evento?.nombre ?: "Evento desconocido"
    val fechaHoraRegistro: String? get() = registro.fechaYHora
}

/**
 * Clase de datos para estadísticas de registros
 */
data class EstadisticasRegistro(
    val totalRegistros: Int,
    val sociosUnicos: Int,
    val primeraLectura: String?,
    val ultimaLectura: String?
) 