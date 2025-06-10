package com.example.asoadmin.front

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
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
import com.example.asoadmin.back.classes.Evento
import com.example.asoadmin.back.services.EventoService
import com.example.asoadmin.back.services.ResultadoOperacion
import com.example.asoadmin.ui.theme.AsoAdminTheme
import com.example.asoadmin.ui.theme.TopAppBarWithDrawer
import com.example.asoadmin.ui.theme.NavigationDrawerContent
import com.example.asoadmin.ui.theme.navegarAPantalla
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen() {
    val context = LocalContext.current
    val eventoService = EventoService(context)
    var events  by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var toDelete by remember { mutableStateOf<Evento?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showMapsDialog by remember { mutableStateOf(false) }
    var selectedLocationUrl by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Estados del navigation drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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
            Column(Modifier
                .padding(padding)
                .padding(16.dp)
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(events) { ev ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(ev.nombre)
                                    Text(ev.fecha, style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    // Botón de ubicación (solo si hay ubicación)
                                    if (ev.ubicacion.isNotBlank()) {
                                        IconButton(onClick = {
                                            selectedLocationUrl = ev.ubicacion
                                            showMapsDialog = true
                                        }) {
                                            Icon(Icons.Filled.LocationOn, contentDescription = "Ver ubicación")
                                        }
                                    }
                                    
                                    IconButton(onClick = {
                                        toDelete = ev
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                                    }
                                    IconButton(onClick = {
                                        Intent(context, EventDetailActivity::class.java).also { intent ->
                                            // Manejar ID nullable
                                            ev.id?.let { intent.putExtra("evento_id", it) }
                                            intent.putExtra("evento_nombre", ev.nombre)
                                            intent.putExtra("evento_descripcion", ev.descripcion)
                                            intent.putExtra("evento_fecha", ev.fecha)
                                            intent.putExtra("evento_ubicacion", ev.ubicacion)
                                            context.startActivity(intent)
                                        }
                                        if (context is Activity) context.finish()
                                    }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog && toDelete != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title            = { Text("Eliminar evento") },
                text             = { Text("¿Borrar \"${toDelete!!.nombre}\"?") },
                confirmButton    = {
                    TextButton(onClick = {
                        scope.launch {
                            // Verificar que el evento tenga un ID válido
                            val eventoId = toDelete!!.id
                            if (eventoId != null) {
                                // Eliminar usando el servicio
                                when (val resultado = eventoService.eliminarEventoCompleto(eventoId)) {
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
                dismissButton   = {
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
