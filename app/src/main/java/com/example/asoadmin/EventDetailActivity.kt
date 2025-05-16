package com.example.asoadmin

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
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.asoadmin.classes.Evento
import com.example.asoadmin.classes.Socio
import com.example.asoadmin.supabaseConection.supabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    var socios  by remember { mutableStateOf<List<Socio>>(emptyList()) }

    LaunchedEffect(Unit) {
        // Cargar lista completa sin filtros
        socios = fetchAllSocios(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("A. Polillas / Crear Evento") },
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
                    saveEvent(
                        Evento(0, eventName, eventDescription, eventDate, eventLocation),
                        context
                    )
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
                        IconButton(onClick = { /* filtrar participantes */ }) {
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
            items(socios) { socio ->
                // Estado local de cada fila
                var isChecked by remember { mutableStateOf(false) }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { /* seleccionar participante */ }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(socio.nombre)

                    IconToggleButton(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            isChecked = checked
                            //TODO guardar el socio seleccionado
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
                }
                Divider()
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

fun saveEvent(evento: Evento, context: Context) {
    val supabase = supabaseClient(context).getClient()
    CoroutineScope(Dispatchers.IO).launch {
        try {
            supabase.postgrest["Evento"]
                .insert(
                    mapOf(
                        "nombre" to evento.nombre,
                        "descripcion" to evento.descripcion,
                        "fecha" to evento.fecha,
                        "ubicacion" to evento.ubicacion
                    )
                )
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
