package com.example.asoadmin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.asoadmin.classes.Evento

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
    var eventName by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("08/17/2023") }
    var eventLocation by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var showScreenPicker by remember { mutableStateOf(false) }

    val participants = remember {
        mutableStateListOf(
            "Alice", "Bob", "Carlos"
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("A. Polillas / Feria") },
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
                    exportEvent(Evento(0, eventName, eventDescription, eventDate, eventLocation), context)
                },
                containerColor = Color(0xFF6750A4),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Save, contentDescription = "Guardar evento")
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
                        IconButton(onClick = { /* llama al desplegable de fechas */ }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Seleccionar fecha")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
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
                            Icon(Icons.Filled.LocationOn, contentDescription = "Seleccionar ubicación")
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
                        IconButton(onClick = { /* funcion que filtra el listado de usuarios en funcion del nombre escrito */ }) {
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
            items(participants) { name ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { /* llama a la funcion para seleccionar el participante */ }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name, style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.Filled.ArrowForward, contentDescription = null)
                }
                Divider()
            }
        }

        // Screen Picker Modal
        if (showScreenPicker) {
            ScreenPickerModal(
                onDismiss = { showScreenPicker = false },
                screens = listOf("Lista de Eventos", "Crear Evento"),
                onScreenSelected = { screen ->
                    showScreenPicker = false
                    when (screen) {
                        "Lista de Eventos" -> {
                            context.startActivity(Intent(context, EventListActivity::class.java))
                            (context as? Activity)?.finish()
                        }
                        "Crear Evento" -> { context.startActivity(Intent(context, EventDetailActivity::class.java))
                            (context as? Activity)?.finish() }
                    }
                }
            )
        }
    }
}

fun exportEvent(evento: Evento, context: Context) {
    // TODO logica para exportar asistencia al evento
    context.startActivity(Intent(context, EventListActivity::class.java))
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
