# AsoAdmin

AsoAdmin es una aplicación móvil Android desarrollada en Kotlin para gestionar eventos y socios de asociaciones. Utiliza Supabase como backend y Google Maps para la gestión de ubicaciones.

## 🚀 Funcionalidades

### 🔐 **Autenticación**
- Sistema de login para administradores
- Autenticación segura con base de datos PostgreSQL

### 📅 **Gestión de Eventos**
- Crear, editar, eliminar y listar eventos
- Selección de fechas con DatePicker
- Integración con Google Maps para ubicaciones
- Gestión de participantes por evento

### 👥 **Gestión de Socios**
- Lista completa de socios
- Búsqueda por nombre, DNI o número de socio
- Selección múltiple para eventos

### 🎫 **Sistema de Carnets**
- Lectura y gestión de carnets
- Registro de asistencias

## 🛠️ Tecnologías

- **Kotlin** con Jetpack Compose
- **Material Design 3**
- **Supabase** (PostgreSQL)
- **Google Maps**
- **Kotlinx Serialization**

## 📋 Requisitos

- Android Studio Meerkat 2024.3.1+
- SDK mínimo: Android 24
- SDK objetivo: Android 35

## ⚙️ Configuración

### 1. Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/AsoAdmin.git
cd AsoAdmin
```

### 2. Configurar Supabase
Crear archivo `config.properties` en `app/src/main/assets/`:
```properties
SUPABASE_URL=tu_url_de_supabase
SUPABASE_KEY=tu_anon_key_de_supabase
MAPS_API_KEY=tu_google_maps_api_key
```

### 3. Configurar Base de Datos
Ejecutar el esquema SQL en Supabase con las tablas:
- Evento
- Socio  
- Administrador
- Asistencia
- Carnet
- Registro

### 4. Compilar
```bash
./gradlew sync
```

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/asoadmin/
├── front/              # Interfaces de usuario
│   ├── LogInActivity.kt
│   ├── EventListActivity.kt
│   ├── EventDetailActivity.kt
│   ├── SocioListActivity.kt
│   └── LectorCarnetActivity.kt
│
├── back/               # Lógica de negocio
│   ├── services/       # Servicios
│   ├── repositories/   # Acceso a datos
│   └── classes/        # Modelos de datos
│
├── DDBB/              # Configuración BD
└── ui/theme/          # Temas Material Design
```

## 📱 Pantallas Principales

- **Login**: Autenticación de administradores
- **Lista de Eventos**: CRUD completo de eventos
- **Detalle de Evento**: Formulario con Maps y participantes
- **Lista de Socios**: Gestión de socios
- **Lector de Carnets**: Sistema de asistencias

## 🔒 Estado del Proyecto

✅ **Funcional y listo para producción**

- Arquitectura limpia implementada
- CRUD completo de eventos
- Gestión de participantes
- Integración con Google Maps
- Sistema de carnets operativo
- Material Design 3

## 📄 Licencia

MIT License

---

**Desarrollado para la gestión moderna de asociaciones**
