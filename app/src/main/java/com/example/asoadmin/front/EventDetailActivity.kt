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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.asoadmin.DDBB.ConfigManager
import com.example.asoadmin.back.classes.Evento
import com.example.asoadmin.back.classes.Socio
import com.example.asoadmin.back.classes.Asistencia
import com.example.asoadmin.back.services.EventoService
import com.example.asoadmin.back.services.SocioService
import com.example.asoadmin.back.services.ResultadoOperacion
import com.example.asoadmin.ui.theme.AsoAdminTheme
import com.example.asoadmin.ui.theme.TopAppBarWithDrawer
import com.example.asoadmin.ui.theme.NavigationDrawerContent
import com.example.asoadmin.ui.theme.navegarAPantalla
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.annotation.SuppressLint
import android.location.Geocoder
import androidx.compose.material.icons.filled.Clear
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
            AsoAdminTheme(
                darkTheme = false
            ) {
                EventDetailScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = context as? Activity
    
    // Estados del navigation drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    // Servicios
    val eventoService = EventoService(context)
    val socioService = SocioService(context)
    
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
    var showLocationPicker by remember { mutableStateOf(false) }

    var socios  by remember { mutableStateOf<List<Socio>>(emptyList()) }
    var asistencias by remember { mutableStateOf<List<Asistencia>>(emptyList()) }
    var sociosSeleccionados by remember { mutableStateOf<Set<Long>>(emptySet()) }

    LaunchedEffect(Unit) {
        // Cargar lista completa usando servicios
        socios = socioService.obtenerTodosLosSocios()
        
        // Si está en modo edición, cargar las asistencias existentes
        if (isEditMode) {
            val sociosParticipantes = eventoService.obtenerSociosParticipantes(eventoId)
            sociosSeleccionados = sociosParticipantes
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                pantallaActual = if (isEditMode) "Editar Evento" else "Crear Evento",
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
                    title = if (isEditMode) "Editar Evento" else "Crear Evento",
                    subtitle = "A. Polillas",
                    onDrawerOpen = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (isEditMode) {
                            scope.launch {
                                val resultado = eventoService.actualizarEvento(
                                    Evento(eventoId, eventName, eventDescription, eventDate, eventLocation)
                                )
                                when (resultado) {
                                    is ResultadoOperacion.Exito -> {
                                        context.startActivity(Intent(context, EventListActivity::class.java))
                                        if (context is Activity) context.finish()
                                    }
                                    is ResultadoOperacion.Error -> {
                                        println("Error al actualizar evento: ${resultado.mensaje}")
                                    }
                                }
                            }
                        } else {
                            scope.launch {
                                val resultado = eventoService.crearEventoConParticipantes(
                                    Evento(null, eventName, eventDescription, eventDate, eventLocation),
                                    sociosSeleccionados
                                )
                                when (resultado) {
                                    is ResultadoOperacion.Exito -> {
                                        context.startActivity(Intent(context, EventListActivity::class.java))
                                        if (context is Activity) context.finish()
                                    }
                                    is ResultadoOperacion.Error -> {
                                        println("Error al crear evento: ${resultado.mensaje}")
                                    }
                                }
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Guardar")
                }
            }
        ) { padding ->
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
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
                            IconButton(onClick = { showLocationPicker = true }) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = "Seleccionar ubicación"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLocationPicker = true },
                        shape = RoundedCornerShape(8.dp),
                        readOnly = true
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
                                            scope.launch {
                                                eventoService.removerParticipante(eventoId, socioId)
                                            }
                                        }
                                    } else {
                                        sociosSeleccionados = sociosSeleccionados + socioId
                                        // Si está en modo edición, crear asistencia en la BD
                                        if (isEditMode) {
                                            scope.launch {
                                                eventoService.agregarParticipante(eventoId, socioId, eventDate)
                                            }
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
                                            scope.launch {
                                                eventoService.agregarParticipante(eventoId, socioId, eventDate)
                                            }
                                        }
                                    } else {
                                        sociosSeleccionados = sociosSeleccionados - socioId
                                        // Si está en modo edición, eliminar asistencia de la BD
                                        if (isEditMode) {
                                            scope.launch {
                                                eventoService.removerParticipante(eventoId, socioId)
                                            }
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

            // Modal LocationPicker
            if (showLocationPicker) {
                LocationPickerModal(
                    currentLocation = eventLocation,
                    onLocationSelected = { location ->
                        eventLocation = location
                        showLocationPicker = false
                    },
                    onDismiss = { showLocationPicker = false },
                    onOpenMaps = {
                        // Abrir Google Maps para búsqueda más avanzada
                        openGoogleMaps(context)
                    }
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerModal(
    currentLocation: String,
    onLocationSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onOpenMaps: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Estado para el mapa
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf("") }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Pair<String, LatLng>>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    
    // Cliente de ubicación y geocoder
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    
    // Posición inicial del mapa (se actualizará con la ubicación del usuario)
    val defaultPosition = LatLng(37.3891, -5.9845) // Sevilla como fallback
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 12f)
    }
    
    // Función para generar enlace de Google Maps
    fun generateMapsLink(latLng: LatLng): String {
        return "https://maps.google.com/?q=${latLng.latitude},${latLng.longitude}"
    }
    
    // Función para buscar ubicaciones por texto
    suspend fun searchLocations(query: String) {
        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }
        
        isSearching = true
        try {
            withContext(Dispatchers.IO) {
                val addresses = geocoder.getFromLocationName(query, 5)
                val results = addresses?.mapNotNull { address ->
                    val location = LatLng(address.latitude, address.longitude)
                    val name = address.getAddressLine(0) ?: address.featureName ?: "Ubicación encontrada"
                    name to location
                } ?: emptyList()
                
                withContext(Dispatchers.Main) {
                    searchResults = results
                }
            }
        } catch (e: Exception) {
            println("Error en búsqueda: ${e.message}")
            searchResults = emptyList()
        } finally {
            isSearching = false
        }
    }
    
    // Launcher para permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            getUserLocation(fusedLocationClient) { location ->
                userLocation = location
                cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 12f)
            }
        }
    }
    
    // Solicitar ubicación al abrir el modal
    LaunchedEffect(Unit) {
        try {
            val apiKey = ConfigManager.getMapsApiKey(context)
            if (apiKey.isNotEmpty()) {
                println("Maps API Key cargada desde config.properties")
            }
            
            // Verificar permisos de ubicación
            val fineLocationPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            )
            val coarseLocationPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            )
            
            if (fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
                // Ya tenemos permisos, obtener ubicación
                getUserLocation(fusedLocationClient) { location ->
                    userLocation = location
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 12f)
                }
            } else {
                // Solicitar permisos
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } catch (e: Exception) {
            println("Error al cargar ubicación: ${e.message}")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true, 
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            Modifier
                .fillMaxWidth(0.98f)
                .fillMaxSize(0.92f)
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Seleccionar Ubicación", 
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Campo de búsqueda
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar dirección o lugar") },
                    placeholder = { Text("Ej: Calle Sierpes, Sevilla") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        Row {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { 
                                    searchQuery = ""
                                    searchResults = emptyList()
                                }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                                }
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    searchLocations(searchQuery)
                                }
                            }) {
                                Icon(Icons.Filled.Search, contentDescription = "Buscar")
                            }
                        }
                    },
                    singleLine = true
                )
                
                // Resultados de búsqueda
                if (searchResults.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 120.dp)
                            .padding(vertical = 4.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            items(searchResults) { (name, location) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedLocation = location
                                            selectedAddress = generateMapsLink(location)
                                            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
                                            searchResults = emptyList()
                                            searchQuery = ""
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Mapa más grande
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Ocupar todo el espacio disponible
                    shape = RoundedCornerShape(8.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            selectedLocation = latLng
                            selectedAddress = generateMapsLink(latLng)
                        }
                    ) {
                        // Marcador de ubicación del usuario
                        userLocation?.let { location ->
                            Marker(
                                state = MarkerState(position = location),
                                title = "Tu ubicación"
                            )
                        }
                        
                        // Marcador de ubicación seleccionada
                        selectedLocation?.let { location ->
                            Marker(
                                state = MarkerState(position = location),
                                title = "Ubicación seleccionada"
                            )
                        }
                        
                        // Marcadores de resultados de búsqueda
                        searchResults.forEach { (name, location) ->
                            Marker(
                                state = MarkerState(position = location),
                                title = name,
                                onClick = {
                                    selectedLocation = location
                                    selectedAddress = generateMapsLink(location)
                                    searchResults = emptyList()
                                    searchQuery = ""
                                    true
                                }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                // Enlace seleccionado
                if (selectedAddress.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "Enlace de ubicación:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                selectedAddress,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Botones
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Botón para ir a mi ubicación
                    TextButton(
                        onClick = {
                            userLocation?.let { location ->
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = "Mi ubicación",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("Mi ubicación")
                    }
                    
                    // Botón confirmar
                    TextButton(
                        onClick = {
                            if (selectedAddress.isNotEmpty()) {
                                onLocationSelected(selectedAddress)
                            }
                        },
                        enabled = selectedAddress.isNotEmpty()
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}

// Función auxiliar para obtener la ubicación del usuario
@SuppressLint("MissingPermission")
private fun getUserLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (LatLng) -> Unit
) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onLocationReceived(LatLng(location.latitude, location.longitude))
            }
        }
        .addOnFailureListener {
            // Si falla, usar Sevilla como fallback
            onLocationReceived(LatLng(37.3891, -5.9845))
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

// Función para abrir Google Maps
fun openGoogleMaps(context: Context) {
    try {
        // Intent para abrir Google Maps en modo de búsqueda
        val mapIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("geo:0,0?q=")
            setPackage("com.google.android.apps.maps")
        }
        
        // Verificar si Google Maps está instalado
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Si Google Maps no está instalado, usar el navegador web
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://maps.google.com/")
            }
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        // Fallback a mostrar un toast si no se puede abrir Maps
        android.widget.Toast.makeText(
            context,
            "No se pudo abrir Google Maps",
            android.widget.Toast.LENGTH_SHORT
        ).show()
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
