package com.example.asoadmin.front

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.asoadmin.DDBB.supabaseClient
import com.example.asoadmin.R
import com.example.asoadmin.back.classes.Administrador
import com.example.asoadmin.back.classes.Evento
import com.example.asoadmin.ui.theme.AsoAdminTheme
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AsoAdminTheme(
                darkTheme = false // Forzar modo claro
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val supabase = supabaseClient(context = LocalContext.current).getClient()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedEvent by remember { mutableStateOf("No hay eventos disponibles") }
    var expanded by remember { mutableStateOf(false) }
    var events: List<Evento> by remember { mutableStateOf(listOf()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            events = supabase.postgrest["Evento"].select().decodeList<Evento>()
            //se muestra en el logcat el listado de eventos recibidos
            println("###################### Eventos recibidos: $events #######################")

            if (events.isNotEmpty()) {
                selectedEvent = events[0].nombre
            }
            else{
                selectedEvent = "Llegó vacio"
            }
            Toast
                .makeText(context, "Se ha conectado con la DDBB correctamente", Toast.LENGTH_LONG)
                .show()
        } catch (e: Exception) {
            selectedEvent = "No se han cargado los eventos correctamente"
            Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(200.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Usuario
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Usuario",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { username = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Limpiar usuario"
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contraseña
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Contraseña",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { password = "" }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Limpiar contraseña"
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Log In
        Button(
            onClick = {
                if (username.isNotBlank() && password.isNotBlank()) {
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            login(username, password, context, supabase)
                        } catch (e: Exception) {
                            Toast
                                .makeText(context, "Error inesperado: ${e.message}", Toast.LENGTH_LONG)
                                .show()
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    Toast.makeText(context, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text("Log In", modifier = Modifier.padding(end = 8.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "Ingresar"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        // Selector de evento
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Evento",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedEvent,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    events.forEach { event ->
                        DropdownMenuItem(
                            text = { Text(text = event.nombre) },
                            onClick = {
                                selectedEvent = event.nombre
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

suspend fun login(
    username: String,
    password: String,
    context: Context,
    supabase: SupabaseClient
): Boolean {
    return try {

        val administradores = supabase.postgrest["Administrador"]
            .select {
                eq("nombre", username)
                eq("contraseña", password)
            }
            .decodeList<Administrador>()

        val loginExitoso = true //administradores.isNotEmpty()

        withContext(Dispatchers.Main) {
            if (loginExitoso) {
                Toast.makeText(context, "Inicio de sesión correcto", Toast.LENGTH_LONG).show()
                navigateToEventList(context)
            } else {
                Toast.makeText(context, "Inicio de sesión incorrecto", Toast.LENGTH_LONG).show()
            }
        }

        loginExitoso
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Error al iniciar sesión: ${e.message}", Toast.LENGTH_LONG).show()
        }
        false
    }
}

fun navigateToEventList(context: Context) {
    val intent = Intent(context, EventListActivity::class.java)
    context.startActivity(intent)
    if (context is Activity) {
        context.finish()
    }
}
