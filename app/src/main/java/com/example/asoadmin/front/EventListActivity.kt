package com.example.asoadmin.front

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.asoadmin.back.classes.Evento
import com.example.asoadmin.back.services.EventoService
import com.example.asoadmin.back.services.RegistroService
import com.example.asoadmin.back.services.ResultadoOperacion
import com.example.asoadmin.ui.theme.AsoAdminTheme
import com.example.asoadmin.ui.theme.NavigationDrawerContent
import com.example.asoadmin.ui.theme.TopAppBarWithDrawer
import com.example.asoadmin.ui.theme.navegarAPantalla
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { 
            AsoAdminTheme(
                darkTheme = false
            ) {
                EventListScreen() 
            }
        }
    }
}

@Composable
fun EventOptionsBottomSheet(
    event: Evento,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMapsClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Título del evento
        Text(
            text = event.nombre,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = event.fecha,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Opción Editar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEditClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Editar",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Editar evento",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Opción Eliminar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDeleteClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Eliminar evento",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        
        // Opción Maps (solo si hay ubicación)
        if (event.ubicacion.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMapsClick() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = "Ver ubicación",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Ver ubicación",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Opción Exportar Registros
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExportClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.FileDownload,
                    contentDescription = "Exportar registros",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Exportar registros",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
        
        // Espacio adicional para el bottom sheet
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Función para exportar registros de un evento a CSV
suspend fun exportarRegistrosEvento(context: Context, evento: Evento) {
    try {
        val registroService = RegistroService(context)
        
        // Obtener registros del evento
        val resultado = registroService.obtenerHistorialEvento(evento.id ?: return)
        
        when (resultado) {
            is ResultadoOperacion.Exito -> {
                val registros = resultado.datos
                
                if (registros.isEmpty()) {
                    android.widget.Toast.makeText(
                        context,
                        "No hay registros de acceso para este evento",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                
                // Crear archivo CSV
                val fileName = "registros_${evento.nombre.replace(" ", "_")}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
                val file = File(context.cacheDir, fileName)
                
                FileWriter(file).use { writer ->
                    // Escribir encabezados
                    writer.append("Evento,Socio,Numero_Socio,DNI,Fecha_Hora_Acceso\n")
                    
                    // Escribir datos
                    registros.forEach { registro ->
                        writer.append("\"${evento.nombre}\",")
                        writer.append("\"${registro.nombreSocio}\",")
                        writer.append("\"${registro.numeroSocio ?: ""}\",")
                        writer.append("\"${registro.socio?.dni ?: ""}\",")
                        writer.append("\"${registro.fechaHoraRegistro ?: ""}\"\n")
                    }
                }
                
                // Compartir archivo
                compartirArchivo(context, file)
                
            }
            is ResultadoOperacion.Error -> {
                android.widget.Toast.makeText(
                    context,
                    "Error al obtener registros: ${resultado.mensaje}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
        
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Error al exportar: ${e.message}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

// Función para compartir archivo usando el sistema de Android
fun compartirArchivo(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Registros de Acceso - ${file.nameWithoutExtension}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Compartir registros"))
        
    } catch (e: Exception) {
        android.widget.Toast.makeText(
            context,
            "Error al compartir archivo: ${e.message}",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

// Función para abrir la ubicación en Google Maps
fun openLocationInMaps(context: android.content.Context, locationUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(locationUrl)
            setPackage("com.google.android.apps.maps")
        }
        
        // Verificar si Google Maps está instalado
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Si Google Maps no está instalado, usar el navegador web
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(locationUrl)
            }
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        // Fallback a mostrar un toast si no se puede abrir
        android.widget.Toast.makeText(
            context,
            "No se pudo abrir la ubicación",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen() {
    val context = LocalContext.current
    val eventoService = EventoService(context)
    var events by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var toDelete by remember { mutableStateOf<Evento?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showMapsDialog by remember { mutableStateOf(false) }
    var selectedLocationUrl by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Estados del navigation drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Estados del bottom sheet
    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Evento?>(null) }

    LaunchedEffect(Unit) {
        // Cargar lista usando el servicio
        events = eventoService.obtenerTodosLosEventos()
    }

    // Función para recargar eventos
    suspend fun reloadEvents() {
        events = eventoService.obtenerTodosLosEventos()
    }

    // Recargar eventos cuando se regrese a la pantalla
    LaunchedEffect(events.size) {
        // Este efecto se ejecutará cuando cambie el tamaño de la lista
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                pantallaActual = "Lista de Eventos",
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
                    title = "Lista de Eventos",
                    onDrawerOpen = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    actions = {
                        // Botón para ir a la lista de socios
                        IconButton(onClick = {
                            Intent(context, SocioListActivity::class.java).also {
                                context.startActivity(it)
                            }
                        }) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = "Ver Socios",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    Intent(context, EventDetailActivity::class.java).also {
                        context.startActivity(it)
                    }
                    if (context is Activity) context.finish()
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir")
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(events) { ev ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedEvent = ev
                                    showBottomSheet = true
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = ev.nombre,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ev.fecha,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (ev.descripcion.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ev.descripcion,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Sheet Modal con opciones del evento
            if (showBottomSheet && selectedEvent != null) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = bottomSheetState,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                                    EventOptionsBottomSheet(
                    event = selectedEvent!!,
                    onEditClick = {
                        showBottomSheet = false
                        Intent(context, EventDetailActivity::class.java).also { intent ->
                            selectedEvent!!.id?.let { intent.putExtra("evento_id", it) }
                            intent.putExtra("evento_nombre", selectedEvent!!.nombre)
                            intent.putExtra("evento_descripcion", selectedEvent!!.descripcion)
                            intent.putExtra("evento_fecha", selectedEvent!!.fecha)
                            intent.putExtra("evento_ubicacion", selectedEvent!!.ubicacion)
                            context.startActivity(intent)
                        }
                        if (context is Activity) context.finish()
                    },
                    onDeleteClick = {
                        showBottomSheet = false
                        toDelete = selectedEvent
                        showDialog = true
                    },
                    onMapsClick = {
                        showBottomSheet = false
                        selectedLocationUrl = selectedEvent!!.ubicacion
                        showMapsDialog = true
                    },
                    onExportClick = {
                        showBottomSheet = false
                        scope.launch {
                            exportarRegistrosEvento(context, selectedEvent!!)
                        }
                    }
                )
                }
            }

            if (showDialog && toDelete != null) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Eliminar evento") },
                    text = { Text("¿Borrar \"${toDelete!!.nombre}\"?") },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch {
                                // Verificar que el evento tenga un ID válido
                                val eventoId = toDelete!!.id
                                if (eventoId != null) {
                                    // Eliminar usando el servicio
                                    when (val resultado =
                                        eventoService.eliminarEventoCompleto(eventoId)) {
                                        is ResultadoOperacion.Exito -> {
                                            events = events.filter { it.id != eventoId }
                                        }

                                        is ResultadoOperacion.Error -> {
                                            println("Error al eliminar evento: ${resultado.mensaje}")
                                        }
                                    }
                                } else {
                                    println("Error: No se puede eliminar un evento sin ID")
                                }
                            }
                            showDialog = false
                        }) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            // Diálogo de confirmación para abrir Maps
            if (showMapsDialog) {
                AlertDialog(
                    onDismissRequest = { showMapsDialog = false },
                    title = { Text("Abrir ubicación") },
                    text = { Text("¿Deseas abrir la ubicación del evento en Google Maps?") },
                    confirmButton = {
                        TextButton(onClick = {
                            openLocationInMaps(context, selectedLocationUrl)
                            showMapsDialog = false
                        }) {
                            Text("Abrir Maps")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showMapsDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
