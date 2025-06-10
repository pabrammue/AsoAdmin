package com.example.asoadmin.back.services

import android.content.Context
import com.example.asoadmin.back.classes.Carnet
import com.example.asoadmin.back.classes.Socio
import com.example.asoadmin.back.repositories.CarnetRepository
import com.example.asoadmin.back.repositories.SocioRepository
import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.nio.charset.StandardCharsets

class CarnetService(private val context: Context) {
    
    private val carnetRepository = CarnetRepository(context)
    private val socioRepository = SocioRepository(context)
    
    // Salt específico de la aplicación para mayor seguridad
    private val APP_SALT = "AsoAdmin_2024_SecureKey"
    
    /**
     * Crear un carnet para un socio
     */
    suspend fun crearCarnetParaSocio(idSocio: Long): ResultadoOperacion<Carnet> {
        return try {
            // Verificar que el socio existe
            val socio = socioRepository.obtenerSocioPorId(idSocio)
            if (socio == null) {
                return ResultadoOperacion.Error("El socio con ID $idSocio no existe")
            }
            
            // Verificar que el socio no tenga ya un carnet
            if (carnetRepository.socioTieneCarnet(idSocio)) {
                return ResultadoOperacion.Error("El socio ${socio.nombre} ya tiene un carnet")
            }
            
            // Crear el carnet
            val carnet = carnetRepository.crearCarnet(idSocio)
            if (carnet != null) {
                ResultadoOperacion.Exito(carnet, "Carnet creado exitosamente para ${socio.nombre}")
            } else {
                ResultadoOperacion.Error("No se pudo crear el carnet")
            }
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al crear carnet: ${e.message}")
        }
    }
    
    /**
     * Obtener información completa de un socio con su carnet
     */
    suspend fun obtenerSocioConCarnet(idSocio: Long): SocioConCarnet? {
        return try {
            val socio = socioRepository.obtenerSocioPorId(idSocio)
            val carnet = carnetRepository.obtenerCarnetPorIdSocio(idSocio)
            
            if (socio != null) {
                SocioConCarnet(socio, carnet)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Obtener todos los socios con información de si tienen carnet
     */
    suspend fun obtenerTodosSociosConEstadoCarnet(): List<SocioConCarnet> {
        return try {
            val socios = socioRepository.obtenerTodosLosSocios()
            val carnets = carnetRepository.obtenerTodosLosCarnets()
            
            // Crear un mapa para acceso rápido a carnets por idSocio
            val carnetsPorSocio = carnets.associateBy { it.idSocio }
            
            socios.map { socio ->
                val carnet = carnetsPorSocio[socio.id]
                SocioConCarnet(socio, carnet)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Verificar si un socio puede tener un carnet
     */
    suspend fun puedeCrearCarnet(idSocio: Long): ResultadoOperacion<Boolean> {
        return try {
            val socio = socioRepository.obtenerSocioPorId(idSocio)
            if (socio == null) {
                ResultadoOperacion.Error("El socio no existe")
            } else if (carnetRepository.socioTieneCarnet(idSocio)) {
                ResultadoOperacion.Error("El socio ya tiene un carnet")
            } else {
                ResultadoOperacion.Exito(true, "El socio puede tener un carnet")
            }
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al verificar: ${e.message}")
        }
    }
    
    /**
     * Eliminar carnet de un socio
     */
    suspend fun eliminarCarnetDeSocio(idSocio: Long): ResultadoOperacion<Boolean> {
        return try {
            val carnet = carnetRepository.obtenerCarnetPorIdSocio(idSocio)
            if (carnet == null) {
                ResultadoOperacion.Error("El socio no tiene carnet")
            } else {
                val eliminado = carnetRepository.eliminarCarnet(carnet.id)
                if (eliminado) {
                    ResultadoOperacion.Exito(true, "Carnet eliminado exitosamente")
                } else {
                    ResultadoOperacion.Error("No se pudo eliminar el carnet")
                }
            }
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al eliminar carnet: ${e.message}")
        }
    }
    
    /**
     * Generar clave encriptada basada en DNI
     */
    private fun generarClaveEncriptada(dni: String?): String {
        if (dni.isNullOrBlank()) {
            return "INVALID_DNI"
        }
        
        return try {
            // Combinar DNI con salt de la aplicación
            val dataToHash = "$dni$APP_SALT"
            
            // Generar hash SHA-256
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(dataToHash.toByteArray(StandardCharsets.UTF_8))
            
            // Convertir a hexadecimal y tomar solo los primeros 16 caracteres para que sea más manejable
            hashBytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
        } catch (e: Exception) {
            e.printStackTrace()
            "ERROR_HASH"
        }
    }
    
    /**
     * Generar datos para escribir en tarjeta NFC (solo clave encriptada)
     */
    fun generarDatosNFC(socio: Socio, carnet: Carnet): DatosCarnetNFC {
        val claveEncriptada = generarClaveEncriptada(socio.dni)
        
        return DatosCarnetNFC(
            claveEncriptada = claveEncriptada,
            fechaEmision = carnet.fechaEmision,
            app = "AsoAdmin",
            version = "1.0"
        )
    }
    
    /**
     * Verificar si una clave encriptada corresponde a un DNI específico
     */
    fun verificarClaveEncriptada(claveEncriptada: String, dni: String?): Boolean {
        if (dni.isNullOrBlank()) return false
        
        val claveGenerada = generarClaveEncriptada(dni)
        return claveGenerada == claveEncriptada
    }
    
    /**
     * Buscar socio por clave encriptada
     */
    suspend fun buscarSocioPorClaveEncriptada(claveEncriptada: String): Socio? {
        return try {
            val todosLosSocios = socioRepository.obtenerTodosLosSocios()
            
            // Buscar el socio cuyo DNI genere la misma clave encriptada
            todosLosSocios.find { socio ->
                verificarClaveEncriptada(claveEncriptada, socio.dni)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Clase de datos serializable para escribir en tarjetas NFC (solo clave encriptada)
 */
@Serializable
data class DatosCarnetNFC(
    val claveEncriptada: String,
    val fechaEmision: String? = null,
    val app: String,
    val version: String
)

/**
 * Clase de datos para combinar información de socio y carnet
 */
data class SocioConCarnet(
    val socio: Socio,
    val carnet: Carnet? = null
) {
    val tieneCarnet: Boolean get() = carnet != null
    val nombreCompleto: String get() = socio.nombre
    val numeroSocio: Long? get() = socio.nSocio
    val dni: String? get() = socio.dni
    val tipoSocio: String? get() = socio.tSocio
    val fechaEmisionCarnet: String? get() = carnet?.fechaEmision
} 