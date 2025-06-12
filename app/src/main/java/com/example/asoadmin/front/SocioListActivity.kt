package com.example.asoadmin.front

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.asoadmin.back.services.CarnetService
import com.example.asoadmin.back.services.SocioConCarnet
import com.example.asoadmin.back.services.ResultadoOperacion
import com.example.asoadmin.ui.theme.AsoAdminTheme
import com.example.asoadmin.ui.theme.TopAppBarWithDrawer
import com.example.asoadmin.ui.theme.NavigationDrawerContent
import com.example.asoadmin.ui.theme.navegarAPantalla
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.compose.ui.platform.LocalContext

class SocioListActivity : ComponentActivity() {
    
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var intentFiltersArray: Array<IntentFilter>? = null
    private var techListsArray: Array<Array<String>>? = null
    
    private lateinit var carnetService: CarnetService
    
    private var socioSeleccionado by mutableStateOf<SocioConCarnet?>(null)
    private var esperandoNFC by mutableStateOf(false)
    private var sociosConCarnet by mutableStateOf<List<SocioConCarnet>>(emptyList())
    private var isLoading by mutableStateOf(true)
    
    // Función para recargar datos desde cualquier parte de la clase
    private fun recargarDatos() {
        lifecycleScope.launch {
            isLoading = true
            sociosConCarnet = carnetService.obtenerTodosSociosConEstadoCarnet()
            isLoading = false
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar servicio
        carnetService = CarnetService(this)
        
        // Configurar NFC
        configurarNFC()
        
        // Cargar datos iniciales
        recargarDatos()
        
        setContent {
            AsoAdminTheme(
                darkTheme = false
            ) {
                SocioListScreen()
            }
        }
    }
    
    private fun configurarNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        if (nfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta NFC", Toast.LENGTH_LONG).show()
            return
        }
        
        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "Por favor, habilita NFC en la configuración", Toast.LENGTH_LONG).show()
        }
        
        // Configurar intent para capturar tags NFC
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        
        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try {
                addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
        }
        
        intentFiltersArray = arrayOf(ndef)
        techListsArray = arrayOf(arrayOf(Ndef::class.java.name))
    }
    
    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
    }
    
    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            
            if (tag != null && socioSeleccionado != null && esperandoNFC) {
                escribirEnTarjeta(tag, socioSeleccionado!!)
            }
        }
    }
    
    private fun escribirEnTarjeta(tag: Tag, socioConCarnet: SocioConCarnet) {
        lifecycleScope.launch {
            var carnetCreado: Boolean = false
            var carnetTemporal: com.example.asoadmin.back.classes.Carnet? = null
            
            try {
                val ndef = Ndef.get(tag)
                
                if (ndef == null) {
                    runOnUiThread {
                        Toast.makeText(this@SocioListActivity, "La tarjeta no soporta NDEF", Toast.LENGTH_SHORT).show()
                        esperandoNFC = false
                        socioSeleccionado = null
                    }
                    return@launch
                }
                
                ndef.connect()
                
                // Verificar si la tarjeta puede ser bloqueada
                if (!ndef.canMakeReadOnly()) {
                    runOnUiThread {
                        Toast.makeText(this@SocioListActivity, "Esta tarjeta no puede ser bloqueada permanentemente", Toast.LENGTH_LONG).show()
                        esperandoNFC = false
                        socioSeleccionado = null
                    }
                    ndef.close()
                    return@launch
                }
                
                // Si no tiene carnet, crearlo en la base de datos PERO guardamos referencia para poder eliminarlo si falla
                val resultado = if (socioConCarnet.tieneCarnet) {
                    ResultadoOperacion.Exito(socioConCarnet.carnet!!, "Reescribiendo carnet existente")
                } else {
                    carnetService.crearCarnetParaSocio(socioConCarnet.socio.id!!).also { result ->
                        if (result is ResultadoOperacion.Exito) {
                            carnetCreado = true
                            carnetTemporal = result.datos
                        }
                    }
                }
                
                when (resultado) {
                    is ResultadoOperacion.Exito -> {
                        val carnet = resultado.datos
                        
                        // Generar datos para la tarjeta NFC usando el servicio
                        val datosNFC = carnetService.generarDatosNFC(socioConCarnet.socio, carnet)
                        
                        val jsonData = Json.encodeToString(datosNFC)
                        val payload = jsonData.toByteArray(Charsets.UTF_8)
                        val record = NdefRecord.createMime("application/json", payload)
                        val message = NdefMessage(arrayOf(record))
                        
                        // Escribir los datos
                        ndef.writeNdefMessage(message)
                        
                        // BLOQUEAR LA TARJETA PERMANENTEMENTE
                        val bloqueoExitoso = ndef.makeReadOnly()
                        
                        ndef.close()
                        
                        runOnUiThread {
                            if (bloqueoExitoso) {
                                Toast.makeText(
                                    this@SocioListActivity,
                                    "Carnet NFC creado y BLOQUEADO exitosamente para ${socioConCarnet.nombreCompleto}.\n¡La información ahora es permanente!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@SocioListActivity,
                                    "Carnet NFC creado para ${socioConCarnet.nombreCompleto}, pero no se pudo bloquear completamente",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            esperandoNFC = false
                            socioSeleccionado = null
                            // Recargar datos para actualizar la pantalla
                            recargarDatos()
                        }
                    }
                    
                    is ResultadoOperacion.Error -> {
                        ndef.close()
                        runOnUiThread {
                            Toast.makeText(
                                this@SocioListActivity,
                                "Error: ${resultado.mensaje}",
                                Toast.LENGTH_LONG
                            ).show()
                            esperandoNFC = false
                            socioSeleccionado = null
                        }
                    }
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                
                // Si se creó un carnet nuevo y falló la escritura NFC, eliminarlo de la base de datos
                if (carnetCreado && carnetTemporal != null) {
                    try {
                        carnetService.eliminarCarnetDeSocio(socioConCarnet.socio.id!!)
                    } catch (deleteError: Exception) {
                        android.util.Log.e("NFC_ERROR", "Error al eliminar carnet temporal: ${deleteError.message}")
                    }
                }
                
                runOnUiThread {
                    Toast.makeText(
                        this@SocioListActivity,
                        "Error al escribir en la tarjeta: ${e.message}\n${if (carnetCreado) "El carnet no se guardó en la base de datos." else ""}",
                        Toast.LENGTH_LONG
                    ).show()
                    esperandoNFC = false
                    socioSeleccionado = null
                }
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SocioListScreen() {
        val context = LocalContext.current
        var searchQuery by remember { mutableStateOf("") }
        var showCrearCarnetDialog by remember { mutableStateOf(false) }
        var socioParaCarnet by remember { mutableStateOf<SocioConCarnet?>(null) }
        val scope = rememberCoroutineScope()

        // Estados del navigation drawer
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        LaunchedEffect(Unit) {
            recargarDatos()
        }

        // Filtrar socios basado en la búsqueda
        val sociosFiltrados = remember(sociosConCarnet, searchQuery) {
            if (searchQuery.isBlank()) {
                sociosConCarnet
            } else {
                sociosConCarnet.filter { socioConCarnet ->
                    socioConCarnet.nombreCompleto.contains(searchQuery, ignoreCase = true) ||
                    socioConCarnet.numeroSocio?.toString()?.contains(searchQuery) == true ||
                    socioConCarnet.dni?.contains(searchQuery, ignoreCase = true) == true
                }
            }
        }
        
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                NavigationDrawerContent(
                    pantallaActual = "Lista de Socios",
                    onPantallaSelected = { pantalla ->
                        navegarAPantalla(context, pantalla)
                    },
                    onDismiss = {
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBarWithDrawer(
                        title = "Lista de Socios",
                        onDrawerOpen = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        actions = {
                            // Botón de refrescar
                            IconButton(onClick = { recargarDatos() }) {
                                Icon(Icons.Default.Refresh, "Recargar")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // Búsqueda
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar socio...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Estado de NFC
                    if (esperandoNFC && socioSeleccionado != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Acerca una tarjeta NFC",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Creando carnet para ${socioSeleccionado!!.nombreCompleto}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Lista de socios
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(sociosFiltrados) { socioConCarnet ->
                                SocioCard(
                                    socioConCarnet = socioConCarnet,
                                    onCrearCarnet = {
                                        if (nfcAdapter?.isEnabled == true) {
                                            socioSeleccionado = socioConCarnet
                                            esperandoNFC = true
                                            Toast.makeText(
                                                this@SocioListActivity,
                                                "Acerca una tarjeta NFC al dispositivo",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@SocioListActivity,
                                                "NFC no está habilitado",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun SocioCard(
        socioConCarnet: SocioConCarnet,
        onCrearCarnet: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Información del socio
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = socioConCarnet.nombreCompleto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (socioConCarnet.numeroSocio != null) {
                        Text(
                            text = "N° Socio: ${socioConCarnet.numeroSocio}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (!socioConCarnet.dni.isNullOrBlank()) {
                        Text(
                            text = "DNI: ${socioConCarnet.dni}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (!socioConCarnet.tipoSocio.isNullOrBlank()) {
                        Text(
                            text = "Tipo: ${socioConCarnet.tipoSocio}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Mostrar fecha de emisión si tiene carnet
                    if (socioConCarnet.tieneCarnet && !socioConCarnet.fechaEmisionCarnet.isNullOrBlank()) {
                        Text(
                            text = "Carnet: ${socioConCarnet.fechaEmisionCarnet?.take(10)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Botón o estado del carnet
                if (socioConCarnet.tieneCarnet) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Tiene carnet",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tiene carnet",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4CAF50),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Botón para reescribir carnet
                        TextButton(
                            onClick = onCrearCarnet,
                            enabled = !esperandoNFC
                        ) {
                            Text(
                                text = "Reescribir",
                                fontSize = 10.sp
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onCrearCarnet,
                        enabled = !esperandoNFC,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            Icons.Default.CreditCard,
                            contentDescription = "Crear carnet",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Crear Carnet",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
} 