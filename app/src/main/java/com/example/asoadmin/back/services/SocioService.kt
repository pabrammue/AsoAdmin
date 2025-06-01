package com.example.asoadmin.back.services

import android.content.Context
import com.example.asoadmin.back.classes.Socio
import com.example.asoadmin.back.repositories.SocioRepository

class SocioService(private val context: Context) {
    
    private val socioRepository = SocioRepository(context)
    
    /**
     * Obtener todos los socios
     */
    suspend fun obtenerTodosLosSocios(): List<Socio> {
        return socioRepository.obtenerTodosLosSocios()
    }
    
    /**
     * Buscar socios por criterios
     */
    suspend fun buscarSocios(query: String): List<Socio> {
        return socioRepository.buscarSocios(query)
    }
    
    /**
     * Obtener socio por ID
     */
    suspend fun obtenerSocioPorId(id: Long): Socio? {
        return socioRepository.obtenerSocioPorId(id)
    }
    
    /**
     * Validar datos de socio antes de crear/actualizar
     */
    private suspend fun validarSocio(socio: Socio, excluyendoId: Long? = null): List<String> {
        val errores = mutableListOf<String>()
        
        // Validar nombre
        if (socio.nombre.isBlank()) {
            errores.add("El nombre es obligatorio")
        }
        
        // Validar número de socio único (si se proporciona)
        socio.nSocio?.let { nSocio ->
            val socioExistente = socioRepository.obtenerSocioPorNumero(nSocio)
            if (socioExistente != null && socioExistente.id != excluyendoId) {
                errores.add("Ya existe un socio con el número $nSocio")
            }
        }
        
        // Validar DNI único (si se proporciona)
        socio.dni?.let { dni ->
            if (dni.isNotBlank()) {
                val socioExistente = socioRepository.obtenerSocioPorDni(dni)
                if (socioExistente != null && socioExistente.id != excluyendoId) {
                    errores.add("Ya existe un socio con el DNI $dni")
                }
            }
        }
        
        return errores
    }
    
    /**
     * Crear nuevo socio con validaciones
     */
    suspend fun crearSocio(socio: Socio): ResultadoOperacion<Socio> {
        return try {
            // Validar datos
            val errores = validarSocio(socio)
            if (errores.isNotEmpty()) {
                return ResultadoOperacion.Error(errores.joinToString(", "))
            }
            
            // Crear socio
            val socioCreado = socioRepository.crearSocio(socio)
                ?: return ResultadoOperacion.Error("No se pudo crear el socio")
            
            ResultadoOperacion.Exito(socioCreado, "Socio creado exitosamente")
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al crear socio: ${e.message}")
        }
    }
    
    /**
     * Actualizar socio con validaciones
     */
    suspend fun actualizarSocio(socio: Socio): ResultadoOperacion<Boolean> {
        return try {
            val idSocio = socio.id ?: return ResultadoOperacion.Error("El socio debe tener un ID válido")
            
            // Validar datos (excluyendo el propio socio)
            val errores = validarSocio(socio, idSocio)
            if (errores.isNotEmpty()) {
                return ResultadoOperacion.Error(errores.joinToString(", "))
            }
            
            // Actualizar socio
            val resultado = socioRepository.actualizarSocio(socio)
            
            if (resultado) {
                ResultadoOperacion.Exito(true, "Socio actualizado exitosamente")
            } else {
                ResultadoOperacion.Error("No se pudo actualizar el socio")
            }
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al actualizar socio: ${e.message}")
        }
    }
    
    /**
     * Eliminar socio
     */
    suspend fun eliminarSocio(id: Long): ResultadoOperacion<Boolean> {
        return try {
            val resultado = socioRepository.eliminarSocio(id)
            
            if (resultado) {
                ResultadoOperacion.Exito(true, "Socio eliminado exitosamente")
            } else {
                ResultadoOperacion.Error("No se pudo eliminar el socio")
            }
        } catch (e: Exception) {
            ResultadoOperacion.Error("Error al eliminar socio: ${e.message}")
        }
    }
} 