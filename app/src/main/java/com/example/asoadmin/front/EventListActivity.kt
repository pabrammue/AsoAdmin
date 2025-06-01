package com.example.asoadmin.front

import android.app.Activity
import android.content.Intent
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.example.asoadmin.DDBB.supabaseClient
import com.example.asoadmin.back.classes.Evento
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class EventListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { EventListScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventListScreen() {
    val context = LocalContext.current
    val supa    = supabaseClient(context).getClient()
    var events  by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var toDelete by remember { mutableStateOf<Evento?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Cargar lista
        events = supa
            .postgrest["Evento"]
            .select()
            .decodeList<Evento>()
    }

    // Función para recargar eventos
    suspend fun reloadEvents() {
        events = supa
            .postgrest["Evento"]
            .select()
            .decodeList<Evento>()
    }

    // Recargar eventos cuando se regrese a la pantalla
    LaunchedEffect(events.size) {
        // Este efecto se ejecutará cuando cambie el tamaño de la lista
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Lista de Eventos") })
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

        if (showDialog && toDelete != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title            = { Text("Eliminar evento") },
                text             = { Text("¿Borrar “${toDelete!!.nombre}”?") },
                confirmButton    = {
                    TextButton(onClick = {
                        scope.launch {
                            // Verificar que el evento tenga un ID válido
                            val eventoId = toDelete!!.id
                            if (eventoId != null) {
                                // DELETE sin RLS
                                supa
                                    .postgrest["Evento"]
                                    .delete {
                                        eq("id", eventoId)
                                    }
                                events = events.filter { it.id != eventoId }
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
    }
}
