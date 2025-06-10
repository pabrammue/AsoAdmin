package com.example.asoadmin.ui.theme

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.asoadmin.front.*

data class PantallaNavegacion(
    val nombre: String,
    val icono: ImageVector,
    val descripcion: String,
    val activityClass: Class<*>
)

val pantallas = listOf(
    PantallaNavegacion(
        nombre = "Lista de Eventos",
        icono = Icons.Default.Event,
        descripcion = "Ver y gestionar eventos",
        activityClass = EventListActivity::class.java
    ),
    PantallaNavegacion(
        nombre = "Crear Evento",
        icono = Icons.Default.Add,
        descripcion = "Crear nuevo evento",
        activityClass = EventDetailActivity::class.java
    ),
    PantallaNavegacion(
        nombre = "Lista de Socios",
        icono = Icons.Default.Person,
        descripcion = "Gestionar socios y carnets",
        activityClass = SocioListActivity::class.java
    ),
    PantallaNavegacion(
        nombre = "Cerrar Sesión",
        icono = Icons.Default.Logout,
        descripcion = "Volver al inicio de sesión",
        activityClass = LoginActivity::class.java
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithDrawer(
    title: String,
    subtitle: String? = null,
    onDrawerOpen: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            if (subtitle != null) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(title)
            }
        },
        navigationIcon = {
            IconButton(onClick = onDrawerOpen) {
                Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
            }
        },
        actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(
    pantallaActual: String,
    onPantallaSelected: (PantallaNavegacion) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header del drawer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Usuario",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "AsoAdmin",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Sistema de gestión",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Divider()
        
        // Lista de pantallas
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(pantallas) { pantalla ->
                NavigationDrawerItem(
                    icon = {
                        Icon(
                            pantalla.icono,
                            contentDescription = pantalla.nombre
                        )
                    },
                    label = {
                        Column {
                            Text(
                                text = pantalla.nombre,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = pantalla.descripcion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    selected = pantalla.nombre == pantallaActual,
                    onClick = {
                        onPantallaSelected(pantalla)
                        onDismiss()
                    },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

fun navegarAPantalla(context: Context, pantalla: PantallaNavegacion) {
    val intent = Intent(context, pantalla.activityClass)
    
    // Si es cerrar sesión, limpiar el stack de actividades
    if (pantalla.activityClass == LoginActivity::class.java) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    
    context.startActivity(intent)
} 