package com.example.asoadmin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.asoadmin.classes.Evento

class EventListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventListScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen() {
    val context = LocalContext.current
    val searchQuery = remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<Evento?>(null) }

    var showShareModal by remember { mutableStateOf(false) }
    var showScreenPicker by remember { mutableStateOf(false) }

    val eventList = remember {
        mutableStateListOf(
            //Listado de eventos falsos
            Evento(1, "Almuerzo Patrona", "", "06/10/24", ""),
            Evento(2, "Concierto Rock", "", "12/11/24", ""),
            Evento(3, "Feria Artesanal", "", "20/08/24", "")
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("A. Polillas / Lista de Eventos") },
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
                onClick = { navigateToEventDetail(context) },
                containerColor = Color(0xFF6750A4),
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir evento")
            }
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                placeholder = { Text("Nombre del evento") },
                trailingIcon = {
                    IconButton(onClick = { /* filtra los eventos en funcion del nombre introducido */ }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(eventList.filter {
                    it.nombre.contains(searchQuery.value, ignoreCase = true)
                }) { event ->
                    EventItem(
                        event = event,
                        onDeleteClick = {
                            eventToDelete = event
                            showDeleteDialog = true
                        },
                        onShareClick = {
                            showShareModal = true
                        }
                    )
                }
            }
        }

        // Confirmación de borrado
        if (showDeleteDialog && eventToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Eliminar '${eventToDelete?.nombre}'?") },
                confirmButton = {
                    TextButton(onClick = {
                        eventToDelete?.let { eventList.remove(it) }
                        showDeleteDialog = false
                    }) {
                        Text("Eliminar", color = Color(0xFF6750A4))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Modal de compartir
        if (showShareModal) {
            ShareModal(
                onDismiss = { showShareModal = false },
                onExport = { user, pass ->
                    showShareModal = false
                }
            )
        }

        // Screen Picker Modal
        if (showScreenPicker) {
            ScreenPickerModal(
                onDismiss = { showScreenPicker = false },
                screens = listOf("Lista de Eventos", "Crear Evento"),
                onScreenSelected = { screen ->
                    showScreenPicker = false
                    when (screen) {
                        "Lista de Eventos" -> { context.startActivity(Intent(context, EventListActivity::class.java))
                            (context as? Activity)?.finish()
                        }

                        "Crear Evento" -> {
                            navigateToEventDetail(context)
                        }
                    }
                },
            )
        }
    }
}

@Composable
fun EventItem(
    event: Evento,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F2F9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(event.nombre, style = MaterialTheme.typography.bodyLarge)
                Text(event.fecha, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Row {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                }
                IconButton(onClick = onShareClick) {
                    Icon(Icons.Filled.Share, contentDescription = "Compartir")
                }
                IconButton(onClick = { navigateToEventDetail(context) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                }
            }
        }
    }
}

fun navigateToEventDetail(context: Context) {
    val intent = Intent(context, EventDetailActivity::class.java)
    context.startActivity(intent)
    // Comprueba si viene de otra actividad y finaliza la actividad actual
    if (context is Activity) {
        context.finish()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareModal(
    onDismiss: () -> Unit,
    onExport: (username: String, password: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                Text(
                    "Introduce contraseña para exportar",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario") },
                    trailingIcon = {
                        IconButton(onClick = { username = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { password = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { onExport(username, password) },
                    Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) {
                    Text("Exportar")
                }
            }
        }
    }
}