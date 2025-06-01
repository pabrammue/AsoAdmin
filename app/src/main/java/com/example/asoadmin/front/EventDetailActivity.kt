package com.example.asoadmin.front

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.asoadmin.DDBB.supabaseClient
import com.example.asoadmin.back.classes.Evento
import com.example.asoadmin.back.classes.Socio
import com.example.asoadmin.back.classes.Asistencia
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import java.util.Date

class EventDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventDetailScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Obtener datos del evento si viene para edición
    val eventoId = activity?.intent?.getLongExtra("evento_id", -1L) ?: -1L
    val isEditMode = eventoId != -1L
    
    var eventName by remember { mutableStateOf(activity?.intent?.getStringExtra("evento_nombre") ?: "") }
    var eventDescription by remember { mutableStateOf(activity?.intent?.getStringExtra("evento_descripcion") ?: "") }
    var eventDate by remember { mutableStateOf(activity?.intent?.getStringExtra("evento_fecha") ?: "2023-08-17") }
    var eventLocation by remember { mutableStateOf(activity?.intent?.getStringExtra("evento_ubicacion") ?: "") }
    var searchQuery by remember { mutableStateOf("") }
    var showScreenPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var socios  by remember { mutableStateOf<List<Socio>>(emptyList()) }
    var asistencias by remember { mutableStateOf<List<Asistencia>>(emptyList()) }
    var sociosSeleccionados by remember { mutableStateOf<Set<Long>>(emptySet()) }

    LaunchedEffect(Unit) {
        // Cargar lista completa sin filtros
        socios = fetchAllSocios(context)
        
        // Si está en modo edición, cargar las asistencias existentes
        if (isEditMode) {
            asistencias = fetchAsistenciasEvento(context, eventoId)
            sociosSeleccionados = asistencias.mapNotNull { it.idSocio }.toSet()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(if (isEditMode) "A. Polillas / Editar Evento" else "A. Polillas / Crear Evento") 
                },
                navigationIcon = {
                    IconButton(onClick = { showScreenPicker = true }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF6F2F9))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isEditMode) {
                        updateEvent(
                            Evento(eventoId, eventName, eventDescription, eventDate, eventLocation),
                            context
                        )
                    } else {
                        saveEvent(
                            Evento(null, eventName, eventDescription, eventDate, eventLocation),
                            context,
                            sociosSeleccionados
                        )
                    }
                },
                containerColor = Color(0xFF6750A4),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Save, contentDescription = if (isEditMode) "Actualizar evento" else "Guardar evento")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = eventName,
                    onValueChange = { eventName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = eventDescription,
                    onValueChange = { eventDescription = it },
                    label = { Text("Descripción") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    label = { Text("Fecha") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Seleccionar fecha")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(8.dp),
                    readOnly = true
                )
            }
            item {
                OutlinedTextField(
                    value = eventLocation,
                    onValueChange = { eventLocation = it },
                    label = { Text("Ubicación") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { /* llama al plugin de maps */ }) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = "Seleccionar ubicación"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar participante") },
                    trailingIcon = {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Search, contentDescription = "Buscar")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(50),
                    singleLine = true
                )
            }

            // Contador de participantes seleccionados
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Participantes seleccionados: ${sociosSeleccionados.size}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Filtrar socios basado en la búsqueda
            val sociosFiltrados = if (searchQuery.isBlank()) {
                socios
            } else {
                socios.filter { 
                    it.nombre.contains(searchQuery, ignoreCase = true) ||
                    it.nSocio?.toString()?.contains(searchQuery) == true ||
                    it.dni?.contains(searchQuery, ignoreCase = true) == true
                }
            }

            items(sociosFiltrados) { socio ->
                // Usar el estado global de asistencias en lugar del estado local
                val isChecked = socio.id?.let { sociosSeleccionados.contains(it) } ?: false

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { 
                            // Alternar selección del socio
                            socio.id?.let { socioId ->
                                if (isChecked) {
                                    sociosSeleccionados = sociosSeleccionados - socioId
                                    // Si está en modo edición, eliminar asistencia de la BD
                                    if (isEditMode) {
                                        eliminarAsistencia(context, eventoId, socioId, eventDate)
                                    }
                                } else {
                                    sociosSeleccionados = sociosSeleccionados + socioId
                                    // Si está en modo edición, crear asistencia en la BD
                                    if (isEditMode) {
                                        crearAsistencia(context, eventoId, socioId, eventDate)
                                    }
                                }
                            }
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconToggleButton(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            // Alternar selección del socio
                            socio.id?.let { socioId ->
                                if (checked) {
                                    sociosSeleccionados = sociosSeleccionados + socioId
                                    // Si está en modo edición, crear asistencia en la BD
                                    if (isEditMode) {
                                        crearAsistencia(context, eventoId, socioId, eventDate)
                                    }
                                } else {
                                    sociosSeleccionados = sociosSeleccionados - socioId
                                    // Si está en modo edición, eliminar asistencia de la BD
                                    if (isEditMode) {
                                        eliminarAsistencia(context, eventoId, socioId, eventDate)
                                    }
                                }
                            }
                        }
                    ) {
                        if (isChecked) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Seleccionado"
                            )
                        } else {
                            Icon(
                                Icons.Filled.CheckBoxOutlineBlank,
                                contentDescription = "No seleccionado"
                            )
                        }
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = socio.nombre,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row {
                            if (socio.nSocio != null) {
                                Text(
                                    text = "N° ${socio.nSocio}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (socio.dni != null) {
                                Text(
                                    text = if (socio.nSocio != null) " • DNI: ${socio.dni}" else "DNI: ${socio.dni}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Divider()
            }
        }

        // Modal DatePicker
        if (showDatePicker) {
            val initialSelectedDateMillis = remember {
                parseDateStringToMillis(eventDate)
            }
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialSelectedDateMillis
            )
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val calendar = Calendar.getInstance()
                                calendar.timeInMillis = millis
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                eventDate = dateFormat.format(calendar.time)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePicker = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showScreenPicker) {
            ScreenPickerModal(
                onDismiss = { showScreenPicker = false },
                screens = listOf("Lista de Eventos", "Crear Evento"),
                onScreenSelected = { screen ->
                    showScreenPicker = false
                    when (screen) {
                        "Lista de Eventos" -> {
                            context.startActivity(Intent(context, EventListActivity::class.java))
                            if (context is Activity) context.finish()
                        }
                        "Crear Evento" -> {
                            context.startActivity(Intent(context, EventDetailActivity::class.java))
                            if (context is Activity) context.finish()
                        }
                    }
                }
            )
        }
    }
}

fun saveEvent(evento: Evento, context: Context, sociosSeleccionados: Set<Long>) {
    val supabase = supabaseClient(context).getClient()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            println("###################### GUARDANDO EVENTO ######################")
            println("Evento: $evento")
            println("Socios seleccionados: $sociosSeleccionados")
            
            // Primero crear el evento sin el campo ID
            val nuevoEvento = Evento(
                id = null, // PostgreSQL auto-generará el ID
                nombre = evento.nombre,
                descripcion = evento.descripcion,
                fecha = formatearFechaParaDB(evento.fecha),
                ubicacion = evento.ubicacion
            )
            
            val response = supabase.postgrest["Evento"]
                .insert(nuevoEvento)
                .decodeList<Evento>()

            println("###################### EVENTO CREADO ######################")
            println("Response del evento: $response")

            // Obtener el ID del evento recién creado
            val nuevoEventoId = response.firstOrNull()?.id
            println("Nuevo evento ID: $nuevoEventoId")

            // Crear las asistencias para los socios seleccionados
            if (nuevoEventoId != null && sociosSeleccionados.isNotEmpty()) {
                println("###################### CREANDO ASISTENCIAS MASIVAS ######################")
                sociosSeleccionados.forEach { socioId ->
                    println("Creando asistencia para socio: $socioId")
                    try {
                        // Crear objeto Asistencia sin ID (se auto-genera en la BD)
                        val nuevaAsistencia = Asistencia(
                            id = null, // PostgreSQL auto-generará el ID
                            idEvento = nuevoEventoId,
                            idSocio = socioId,
                            fechaEvento = formatearFechaParaDB(evento.fecha)
                        )
                        
                        val asistenciaResponse = supabase.postgrest["Asistencia"]
                            .insert(nuevaAsistencia)
                        println("Asistencia creada exitosamente: $asistenciaResponse")
                    } catch (asistenciaError: Exception) {
                        println("Error creando asistencia para socio $socioId: ${asistenciaError.message}")
                        asistenciaError.printStackTrace()
                    }
                }
                println("###################### ASISTENCIAS MASIVAS COMPLETADAS ######################")
            } else {
                println("###################### NO HAY ASISTENCIAS QUE CREAR ######################")
                println("NuevoEventoId: $nuevoEventoId, SociosSeleccionados: ${sociosSeleccionados.size}")
            }
        } catch (e: Exception) {
            println("###################### ERROR GENERAL AL GUARDAR EVENTO ######################")
            println("Error: ${e.message}")
            println("Stack trace: ${e.stackTrace.contentToString()}")
            e.printStackTrace()
        } finally {
            withContext(Dispatchers.Main) {
                context.startActivity(Intent(context, EventListActivity::class.java))
                if (context is Activity) {
                    context.finish()
                }
            }
        }
    }
}

fun updateEvent(evento: Evento, context: Context) {
    val supabase = supabaseClient(context).getClient()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Verificar que el evento tenga un ID válido para el UPDATE
            val idEvento = evento.id
            if (idEvento == null) {
                println("Error: No se puede actualizar un evento sin ID")
                return@launch
            }
            
            // Para UPDATE, usar el objeto evento completo
            val eventoParaActualizar = Evento(
                id = idEvento, // Mantenemos el ID para el UPDATE
                nombre = evento.nombre,
                descripcion = evento.descripcion,
                fecha = formatearFechaParaDB(evento.fecha),
                ubicacion = evento.ubicacion
            )
            
            supabase.postgrest["Evento"]
                .update(eventoParaActualizar) {
                    eq("id", idEvento) // Usar el ID no-null
                }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            withContext(Dispatchers.Main) {
                context.startActivity(Intent(context, EventListActivity::class.java))
                if (context is Activity) {
                    context.finish()
                }
            }
        }
    }
}

suspend fun fetchAllSocios(context: Context): List<Socio> {
    var result: List<Socio>
    result = emptyList()
    try {
        val client = supabaseClient(context).getClient()
        result = client.postgrest["Socio"]
            .select()
            .decodeList<Socio>()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenPickerModal(
    onDismiss: () -> Unit,
    screens: List<String>,
    onScreenSelected: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("Pantallas disponibles", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                LazyColumn(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(screens) { screen ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onScreenSelected(screen) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(screen, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            Icon(Icons.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}

// Función para cargar las asistencias de un evento
suspend fun fetchAsistenciasEvento(context: Context, eventoId: Long): List<Asistencia> {
    return try {
        val client = supabaseClient(context).getClient()
        println("###################### CARGANDO ASISTENCIAS ######################")
        println("Cargando asistencias para eventoId: $eventoId")
        
        val asistencias = client.postgrest["Asistencia"]
            .select {
                eq("idEvento", eventoId)
            }
            .decodeList<Asistencia>()
        
        println("###################### ASISTENCIAS CARGADAS ######################")
        println("Encontradas ${asistencias.size} asistencias: $asistencias")
        
        asistencias
    } catch (e: Exception) {
        println("###################### ERROR AL CARGAR ASISTENCIAS ######################")
        println("Error: ${e.message}")
        println("Stack trace: ${e.stackTrace.contentToString()}")
        e.printStackTrace()
        emptyList()
    }
}

// Función para crear una nueva asistencia
fun crearAsistencia(context: Context, eventoId: Long, socioId: Long, fechaEvento: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = supabaseClient(context).getClient()
            println("###################### CREANDO ASISTENCIA ######################")
            println("EventoId: $eventoId, SocioId: $socioId, Fecha: $fechaEvento")
            
            // Crear objeto Asistencia sin ID (se auto-genera en la BD)
            val nuevaAsistencia = Asistencia(
                id = null, // PostgreSQL auto-generará el ID
                idEvento = eventoId,
                idSocio = socioId,
                fechaEvento = formatearFechaParaDB(fechaEvento)
            )
            
            val response = client.postgrest["Asistencia"]
                .insert(nuevaAsistencia)
            
            println("###################### ASISTENCIA CREADA EXITOSAMENTE ######################")
            println("Response: $response")
            
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(
                    context, 
                    "Participante agregado exitosamente", 
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            println("###################### ERROR AL CREAR ASISTENCIA ######################")
            println("Error: ${e.message}")
            println("Stack trace: ${e.stackTrace.contentToString()}")
            e.printStackTrace()
            
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(
                    context, 
                    "Error al crear asistencia: ${e.message}", 
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

// Función para eliminar una asistencia
fun eliminarAsistencia(context: Context, eventoId: Long, socioId: Long, fechaEvento: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val client = supabaseClient(context).getClient()
            println("###################### ELIMINANDO ASISTENCIA ######################")
            println("EventoId: $eventoId, SocioId: $socioId, Fecha: $fechaEvento")
            
            val response = client.postgrest["Asistencia"]
                .delete {
                    eq("idEvento", eventoId)
                    eq("idSocio", socioId)
                }
            
            println("###################### ASISTENCIA ELIMINADA EXITOSAMENTE ######################")
            println("Response: $response")
            
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(
                    context, 
                    "Participante removido exitosamente", 
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            println("###################### ERROR AL ELIMINAR ASISTENCIA ######################")
            println("Error: ${e.message}")
            println("Stack trace: ${e.stackTrace.contentToString()}")
            e.printStackTrace()
            
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(
                    context, 
                    "Error al eliminar asistencia: ${e.message}", 
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

// Función auxiliar para formatear fecha para la base de datos
fun formatearFechaParaDB(fechaString: String): String {
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

// Función auxiliar para convertir string de fecha a milliseconds
fun parseDateStringToMillis(dateString: String): Long? {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = format.parse(dateString)
        date?.time
    } catch (e: Exception) {
        // Si no se puede parsear, usar la fecha actual
        Calendar.getInstance().timeInMillis
    }
}

/*
 * NOTA IMPORTANTE: Para las operaciones INSERT (crear nuevos registros),
 * usamos mapOf<String, Any> en lugar de objetos completos porque:
 * 
 * 1. Los campos ID son auto-generados (IDENTITY) en PostgreSQL
 * 2. Enviar id=0 puede causar conflictos o errores de duplicidad
 * 3. Es mejor omitir completamente el campo ID y dejar que la BD lo genere
 * 4. mapOf<String, Any> con tipos explícitos evita problemas de serialización
 * 
 * Para las operaciones UPDATE, también omitimos el ID porque se especifica
 * en la cláusula WHERE.
 */
