package com.example.asoadmin.front

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.asoadmin.back.classes.Socio
import com.example.asoadmin.back.repositories.AsistenciaRepository
import com.example.asoadmin.back.services.CarnetService
import com.example.asoadmin.back.services.DatosCarnetNFC
import com.example.asoadmin.ui.theme.AsoAdminTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class LectorCarnetActivity : ComponentActivity() {
    
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null
    private var techListsArray: Array<Array<String>>? = null
    
    private lateinit var carnetService: CarnetService
    private lateinit var asistenciaRepository: AsistenciaRepository
    
    // Datos del evento
    private var eventoId: Long = -1L
    private var eventoNombre: String = ""
    private var eventoDescripcion: String = ""
    private var eventoFecha: String = ""
    private var eventoUbicacion: String = ""
    
    private var escaneandoNFC by mutableStateOf(false)
    private var ultimoCarnetLeido by mutableStateOf<DatosCarnetNFC?>(null)
    private var socioEncontrado by mutableStateOf<Socio?>(null)
    private var tieneAsistencia by mutableStateOf<Boolean?>(null)
    private var mensajeEstado by mutableStateOf("Acerca una tarjeta NFC para verificar el carnet")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Obtener datos del evento desde el Intent
        eventoId = intent.getLongExtra("evento_id", -1L)
        eventoNombre = intent.getStringExtra("evento_nombre") ?: "Evento no especificado"
        eventoDescripcion = intent.getStringExtra("evento_descripcion") ?: ""
        eventoFecha = intent.getStringExtra("evento_fecha") ?: ""
        eventoUbicacion = intent.getStringExtra("evento_ubicacion") ?: ""
        
        android.util.Log.d("NFC_DEBUG", "Evento recibido - ID: $eventoId, Nombre: $eventoNombre")
        
        // Inicializar servicios
        carnetService = CarnetService(this)
        asistenciaRepository = AsistenciaRepository(this)
        
        // Configurar NFC
        configurarNFC()
        
        setContent {
            AsoAdminTheme(
                darkTheme = false // Forzar modo claro
            ) {
                LectorCarnetScreen()
            }
        }
    }
    
    private fun configurarNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        if (nfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta NFC", Toast.LENGTH_LONG).show()
            mensajeEstado = "Dispositivo sin soporte NFC"
            return
        }
        
        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "Por favor, habilita NFC en la configuración", Toast.LENGTH_LONG).show()
            mensajeEstado = "NFC deshabilitado"
            return
        }
        
        // Configurar intent para capturar tags NFC
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        
        // Filtro para NDEF con MIME type application/json
        val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("application/json")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
        }
        
        // Filtro para TAG_DISCOVERED como fallback
        val tagFilter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        
        // Filtro para TECH_DISCOVERED
        val techFilter = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        
        intentFiltersArray = arrayOf(ndefFilter, tagFilter, techFilter)
        techListsArray = arrayOf(arrayOf(Ndef::class.java.name))
    }
    
    override fun onResume() {
        super.onResume()
        android.util.Log.d("NFC_DEBUG", "onResume() llamado")
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
        escaneandoNFC = true
        mensajeEstado = "Acerca una tarjeta NFC para verificar el carnet"
        android.util.Log.d("NFC_DEBUG", "escaneandoNFC establecido a: $escaneandoNFC")
    }
    
    override fun onPause() {
        super.onPause()
        android.util.Log.d("NFC_DEBUG", "onPause() llamado")
        nfcAdapter?.disableForegroundDispatch(this)
        escaneandoNFC = false
        android.util.Log.d("NFC_DEBUG", "escaneandoNFC establecido a: $escaneandoNFC")
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        // Debug log
        android.util.Log.d("NFC_DEBUG", "onNewIntent llamado con acción: ${intent.action}")
        android.util.Log.d("NFC_DEBUG", "Estado actual escaneandoNFC: $escaneandoNFC")
        
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            
            android.util.Log.d("NFC_DEBUG", "Intent NFC detectado: ${intent.action}")
            
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            
            if (tag != null) {
                android.util.Log.d("NFC_DEBUG", "Tag encontrado, forzando lectura independientemente del estado de escaneo")
                // Log adicional de información del tag
                android.util.Log.d("NFC_DEBUG", "Tag ID: ${tag.id?.contentToString()}")
                android.util.Log.d("NFC_DEBUG", "Tecnologías soportadas: ${tag.techList?.joinToString()}")
                
                // Forzar el estado de escaneo a true antes de leer
                if (!escaneandoNFC) {
                    android.util.Log.d("NFC_DEBUG", "Forzando escaneandoNFC a true")
                    escaneandoNFC = true
                }
                
                leerCarnetNFC(tag)
            } else {
                android.util.Log.d("NFC_DEBUG", "Tag es nulo")
            }
        } else {
            android.util.Log.d("NFC_DEBUG", "Intent no relacionado con NFC: ${intent.action}")
        }
    }
    
    private fun leerCarnetNFC(tag: Tag) {
        android.util.Log.d("NFC_DEBUG", "Iniciando lectura de tarjeta NFC")
        val tiempoInicio = System.currentTimeMillis()
        
        lifecycleScope.launch {
            try {
                val ndef = Ndef.get(tag)
                
                if (ndef == null) {
                    android.util.Log.e("NFC_DEBUG", "Tag no soporta NDEF")
                    android.util.Log.d("NFC_DEBUG", "Tecnologías disponibles: ${tag.techList?.joinToString()}")
                    runOnUiThread {
                        mensajeEstado = "La tarjeta no es compatible"
                        Toast.makeText(this@LectorCarnetActivity, "Tarjeta no compatible", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                
                android.util.Log.d("NFC_DEBUG", "NDEF disponible, intentando conectar...")
                android.util.Log.d("NFC_DEBUG", "Tamaño máximo: ${ndef.maxSize} bytes")
                android.util.Log.d("NFC_DEBUG", "Es escribible: ${ndef.isWritable}")
                android.util.Log.d("NFC_DEBUG", "Puede ser de solo lectura: ${ndef.canMakeReadOnly()}")
                
                ndef.connect()
                android.util.Log.d("NFC_DEBUG", "Conexión NDEF establecida exitosamente")
                android.util.Log.d("NFC_DEBUG", "Está conectado: ${ndef.isConnected}")
                
                val ndefMessage = ndef.ndefMessage
                android.util.Log.d("NFC_DEBUG", "NDEF Message obtenido: ${ndefMessage != null}")
                
                if (ndefMessage != null) {
                    android.util.Log.d("NFC_DEBUG", "Número de registros: ${ndefMessage.records.size}")
                    // Log de información de cada registro
                    ndefMessage.records.forEachIndexed { index, record ->
                        android.util.Log.d("NFC_DEBUG", "Registro $index - TNF: ${record.tnf}, Tipo: ${String(record.type, Charsets.UTF_8)}, Tamaño payload: ${record.payload.size}")
                    }
                }
                
                ndef.close()
                android.util.Log.d("NFC_DEBUG", "Conexión NDEF cerrada")
                
                if (ndefMessage == null || ndefMessage.records.isEmpty()) {
                    android.util.Log.e("NFC_DEBUG", "Mensaje NDEF vacío o nulo")
                    runOnUiThread {
                        mensajeEstado = "La tarjeta está vacía"
                        Toast.makeText(this@LectorCarnetActivity, "Tarjeta vacía", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                
                // Leer el primer registro
                val record = ndefMessage.records[0]
                val mimeType = String(record.type, Charsets.UTF_8)
                val payload = String(record.payload, Charsets.UTF_8)
                
                android.util.Log.d("NFC_DEBUG", "MIME type: $mimeType")
                android.util.Log.d("NFC_DEBUG", "Payload length: ${payload.length}")
                android.util.Log.d("NFC_DEBUG", "Payload: $payload")
                
                // Intentar decodificar los datos del carnet
                try {
                    android.util.Log.d("NFC_DEBUG", "Iniciando decodificación JSON...")
                    val datosCarnet = Json.decodeFromString<DatosCarnetNFC>(payload)
                    android.util.Log.d("NFC_DEBUG", "Datos decodificados correctamente")
                    android.util.Log.d("NFC_DEBUG", "Clave encriptada: ${datosCarnet.claveEncriptada}")
                    android.util.Log.d("NFC_DEBUG", "Fecha emisión: ${datosCarnet.fechaEmision}")
                    
                    // Buscar el socio por clave encriptada
                    android.util.Log.d("NFC_DEBUG", "Iniciando búsqueda de socio en base de datos...")
                    val tiempoBusqueda = System.currentTimeMillis()
                    val socio = carnetService.buscarSocioPorClaveEncriptada(datosCarnet.claveEncriptada)
                    val tiempoTotalBusqueda = System.currentTimeMillis() - tiempoBusqueda
                    android.util.Log.d("NFC_DEBUG", "Búsqueda completada en ${tiempoTotalBusqueda}ms")
                    android.util.Log.d("NFC_DEBUG", "Socio encontrado: ${socio?.nombre ?: "null"}")
                    
                    if (socio != null) {
                        android.util.Log.d("NFC_DEBUG", "Detalles del socio - ID: ${socio.id}, N° Socio: ${socio.nSocio}, DNI: ${socio.dni}")
                        
                        // Verificar asistencia al evento específico
                        if (eventoId != -1L && socio.id != null) {
                            android.util.Log.d("NFC_DEBUG", "Verificando asistencia al evento ID: $eventoId")
                            val tiempoAsistencia = System.currentTimeMillis()
                            val asistenciaVerificada = asistenciaRepository.verificarAsistencia(eventoId, socio.id)
                            val tiempoTotalAsistencia = System.currentTimeMillis() - tiempoAsistencia
                            android.util.Log.d("NFC_DEBUG", "Verificación de asistencia completada en ${tiempoTotalAsistencia}ms: $asistenciaVerificada")
                            tieneAsistencia = asistenciaVerificada
                        } else {
                            android.util.Log.d("NFC_DEBUG", "No se puede verificar asistencia - eventoId: $eventoId, socioId: ${socio.id}")
                            tieneAsistencia = null
                        }
                    }
                    
                    val tiempoTotal = System.currentTimeMillis() - tiempoInicio
                    android.util.Log.d("NFC_DEBUG", "Proceso completo de lectura NFC completado en ${tiempoTotal}ms")
                    
                    runOnUiThread {
                        ultimoCarnetLeido = datosCarnet
                        socioEncontrado = socio
                        
                        if (socio != null) {
                            if (tieneAsistencia == true) {
                                mensajeEstado = "✅ Acceso autorizado"
                                Toast.makeText(
                                    this@LectorCarnetActivity,
                                    "Acceso autorizado: ${socio.nombre}",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (tieneAsistencia == false) {
                                mensajeEstado = "❌ Sin registro de asistencia"
                                Toast.makeText(
                                    this@LectorCarnetActivity,
                                    "No registrado en este evento: ${socio.nombre}",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                mensajeEstado = "✅ Carnet válido"
                                Toast.makeText(
                                    this@LectorCarnetActivity,
                                    "Carnet válido: ${socio.nombre}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            mensajeEstado = "❌ Carnet no reconocido"
                            tieneAsistencia = null
                            Toast.makeText(
                                this@LectorCarnetActivity,
                                "Carnet no válido o no encontrado",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    
                } catch (e: Exception) {
                    android.util.Log.e("NFC_DEBUG", "Error decodificando JSON: ${e.message}")
                    android.util.Log.e("NFC_DEBUG", "Stack trace: ${e.stackTraceToString()}")
                    runOnUiThread {
                        mensajeEstado = "❌ Datos no válidos"
                        Toast.makeText(
                            this@LectorCarnetActivity,
                            "Error al leer los datos: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                
            } catch (e: Exception) {
                val tiempoTotal = System.currentTimeMillis() - tiempoInicio
                android.util.Log.e("NFC_DEBUG", "Error general en lectura NFC después de ${tiempoTotal}ms: ${e.message}")
                android.util.Log.e("NFC_DEBUG", "Stack trace completo: ${e.stackTraceToString()}")
                runOnUiThread {
                    mensajeEstado = "❌ Error de lectura"
                    Toast.makeText(
                        this@LectorCarnetActivity,
                        "Error al leer la tarjeta: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LectorCarnetScreen() {
        // Determinar el color de fondo basado en el estado de validez
        val backgroundColor = when {
            tieneAsistencia == true -> Color(0xFF4CAF50).copy(alpha = 0.2f) // Verde claro para válido
            socioEncontrado != null && tieneAsistencia == false -> Color(0xFFF44336).copy(alpha = 0.2f) // Rojo claro para no válido
            ultimoCarnetLeido != null && socioEncontrado == null -> Color(0xFFF44336).copy(alpha = 0.2f) // Rojo claro para carnet inválido
            else -> MaterialTheme.colorScheme.background // Color normal
        }
        
        Scaffold(
            containerColor = backgroundColor,
            topBar = {
                TopAppBar(
                    title = { 
                        Column {
                            Text(
                                text = "Lector de Carnets", 
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = eventoNombre,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(Icons.Default.ArrowBack, "Volver")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Icono principal
                Icon(
                    Icons.Default.CreditCard,
                    contentDescription = "NFC",
                    modifier = Modifier.size(80.dp),
                    tint = if (escaneandoNFC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Estado del escaneo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            tieneAsistencia == true -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            socioEncontrado != null && tieneAsistencia == false -> Color(0xFFF44336).copy(alpha = 0.1f)
                            ultimoCarnetLeido != null && socioEncontrado == null -> Color(0xFFF44336).copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = mensajeEstado,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = when {
                                tieneAsistencia == true -> Color(0xFF4CAF50)
                                socioEncontrado != null && tieneAsistencia == false -> Color(0xFFF44336)
                                ultimoCarnetLeido != null && socioEncontrado == null -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        
                        if (escaneandoNFC && socioEncontrado == null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Información del socio encontrado
                if (socioEncontrado != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "📋 Información del Socio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            InfoRow("Nombre:", socioEncontrado!!.nombre)
                            
                            if (socioEncontrado!!.nSocio != null) {
                                InfoRow("N° Socio:", socioEncontrado!!.nSocio.toString())
                            }
                            
                            if (!socioEncontrado!!.dni.isNullOrBlank()) {
                                InfoRow("DNI:", socioEncontrado!!.dni!!)
                            }
                            
                            if (!socioEncontrado!!.tSocio.isNullOrBlank()) {
                                InfoRow("Tipo:", socioEncontrado!!.tSocio!!)
                            }
                            
                            if (!ultimoCarnetLeido!!.fechaEmision.isNullOrBlank()) {
                                InfoRow("Carnet emitido:", ultimoCarnetLeido!!.fechaEmision!!.take(10))
                            }
                            
                            // Estado de acceso simplificado
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estado:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when (tieneAsistencia) {
                                            true -> Color(0xFF4CAF50)
                                            false -> Color(0xFFF44336)
                                            null -> Color(0xFF9E9E9E)
                                        }
                                    )
                                ) {
                                    Text(
                                        text = when (tieneAsistencia) {
                                            true -> "✓ AUTORIZADO"
                                            false -> "✗ NO AUTORIZADO"
                                            null -> "? SIN VERIFICAR"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Información del carnet no válido
                if (ultimoCarnetLeido != null && socioEncontrado == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336).copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Advertencia",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(32.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Carnet no válido o no encontrado",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFF44336),
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "Clave: ${ultimoCarnetLeido!!.claveEncriptada}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Botón para limpiar
                if (ultimoCarnetLeido != null) {
                    TextButton(
                        onClick = {
                            ultimoCarnetLeido = null
                            socioEncontrado = null
                            tieneAsistencia = null
                            mensajeEstado = "Acerca una tarjeta NFC para verificar el carnet"
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Limpiar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Limpiar y escanear otro")
                    }
                }
            }
        }
    }
    
    @Composable
    fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
} 