package com.example.asoadmin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.asoadmin.dto.Administrador
import com.example.asoadmin.ui.theme.AsoAdminTheme
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val supabase = createSupabaseClient(
    supabaseUrl = "https://bpqcdxhrzwtzfnppmxla.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJwcWNkeGhyend0emZucHBteGxhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQzMjAyMjQsImV4cCI6MjA1OTg5NjIyNH0.meASDNvCYsL4Rmxi6wagjKQLzBzgMe24PUL3guW6Gv0"
) {
    install(Postgrest)
}

class LogIn : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            AsoAdminTheme(
                darkTheme = false // Forzar el modo claro
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
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedEvent by remember { mutableStateOf("Feria") }
    var expanded by remember { mutableStateOf(false) }
    var events by remember { mutableStateOf<List<String>>(listOf()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            events = supabase.postgrest["Evento"].select().decodeList<String>()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar eventos: ${e.message}", Toast.LENGTH_LONG).show()
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
            modifier = Modifier
                .size(200.dp),
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
                            imageVector = Icons.Default.Clear,
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
                            imageVector = Icons.Default.Clear,
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
                            login(username, password, context)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Error inesperado: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
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
                    imageVector = Icons.Default.ArrowForward,
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                            text = { Text(text = event) },
                            onClick = {
                                selectedEvent = event
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Evento
        Button(
            onClick = { /* Implementar acción de evento */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Evento", modifier = Modifier.padding(end = 8.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Ir a evento"
            )
        }
    }
}

suspend fun login(
    username: String,
    password: String,
    context: android.content.Context
): Boolean {
    return try {
        // Consulta corregida que filtra por nombre de usuario y contraseña
        // Falla porque no hay Serializers
        val administradores = supabase.postgrest["Administrador"]
            .select {
                eq("nombre", username)
                eq("contraseña", password)
            }
            .decodeList<Administrador>()

        // Verifica si se encontró algún administrador con esas credenciales
        val loginExitoso = administradores.isNotEmpty()

        withContext(Dispatchers.Main) {
            if (loginExitoso) {
                Toast.makeText(context, "Inicio de sesión correcto", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Inicio de sesión incorrecto", Toast.LENGTH_LONG).show()
            }
        }

        loginExitoso
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                context,
                "Error al iniciar sesión: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
        false
    }
}