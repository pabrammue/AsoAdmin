# AsoAdmin

AsoAdmin es una aplicación móvil Android desarrollada en Kotlin que permite gestionar eventos de asociaciones. Utiliza Material Design 3 para una interfaz moderna, Supabase como backend, y Google Maps para gestión de ubicaciones.

## 🚀 Características Principales

### 🔐 **Autenticación**
- Sistema de login para administradores
- Autenticación segura contra base de datos PostgreSQL
- Gestión de sesiones

### 📅 **Gestión Completa de Eventos**
- **Listado de eventos** con diseño moderno
- **Creación y edición** de eventos completos
- **Eliminación** con confirmación de seguridad
- **DatePicker nativo** para selección de fechas
- **Gestión de participantes** con checkbox intuitivo
- **Búsqueda en tiempo real** de participantes

### 👥 **Gestión de Participantes**
- Selección múltiple de socios para eventos
- Agregar/remover participantes en modo edición
- Búsqueda por nombre, número de socio o DNI
- Contador de participantes seleccionados

### 🗺️ **Integración Avanzada con Maps**
- **Geolocalización automática** del usuario
- **Búsqueda por texto** (direcciones, lugares, POIs)
- **Selección visual** de ubicaciones en mapa interactivo
- **Generación automática** de enlaces de Google Maps
- **Botón de ubicación** en lista de eventos
- **Confirmación de seguridad** antes de abrir Maps

### 🎨 **Diseño Moderno**
- **Material Design 3** completo
- **Tema claro personalizado** con colores corporativos
- **Navegación intuitiva** entre pantallas
- **Componentes nativos** de Android
- **Responsive design** adaptable

### 🏗️ **Arquitectura Limpia**
- **Separación de responsabilidades** Frontend/Backend
- **Repositorios** para acceso a datos
- **Servicios** para lógica de negocio
- **Modelos** serializables para PostgreSQL
- **Manejo de errores** robusto

## 🛠️ Tecnologías

### **Frontend**
- **Kotlin** con Jetpack Compose
- **Material Design 3** UI Components
- **Compose Navigation**
- **Coroutines** para programación asíncrona

### **Backend & Base de Datos**
- **Supabase** (PostgreSQL + API REST)
- **Row Level Security** para seguridad
- **Arquitectura de repositorios** y servicios

### **Maps & Ubicación**
- **Google Maps Compose**
- **Google Play Services Location**
- **Geocoding API** para búsqueda de direcciones

### **Otras Librerías**
- **kotlinx.serialization** para JSON
- **Material Icons Extended**

## 📋 Requisitos

- **Android Studio** Meerkat 2024.3.1 o superior
- **SDK mínimo**: Android 21 (Lollipop)
- **Kotlin** 1.9.0 o superior
- **Gradle** 8.0 o superior

## ⚙️ Configuración

### 1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/AsoAdmin.git
cd AsoAdmin
```

### 2. **Configurar Supabase**
Crea un archivo `config.properties` en `app/src/main/assets/`:
```properties
SUPABASE_URL=tu_url_de_supabase_aqui
SUPABASE_KEY=tu_anon_key_de_supabase_aqui
```

### 3. **Configurar Google Maps**
1. Obtén una API Key de Google Cloud Console
2. Habilita las APIs:
   - Maps SDK for Android
   - Geocoding API
   - Places API (opcional)
3. Agrega la clave en `app/src/main/AndroidManifest.xml`:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="TU_GOOGLE_MAPS_API_KEY" />
```

### 4. **Configurar Base de Datos**
Ejecuta el DDL en tu instancia de Supabase:
```sql
-- Ver /DDBB/schema.sql para el esquema completo
-- Incluye tablas: Evento, Socio, Administrador, Asistencia, Carnet
```

### 5. **Sincronizar proyecto**
```bash
./gradlew sync
```

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/asoadmin/
├── 🎨 front/                    # Frontend (UI & Activities)
│   ├── LogInActivity.kt         # Pantalla de autenticación
│   ├── EventListActivity.kt     # Lista de eventos
│   └── EventDetailActivity.kt   # Crear/editar eventos
│
├── 🔧 back/                     # Backend (Lógica de Negocio)
│   ├── 📊 services/             # Servicios de negocio
│   │   ├── EventoService.kt     # Lógica de eventos
│   │   └── SocioService.kt      # Lógica de socios
│   │
│   ├── 🗄️ repositories/        # Acceso a datos
│   │   ├── EventoRepository.kt
│   │   ├── SocioRepository.kt
│   │   ├── AsistenciaRepository.kt
│   │   └── AdministradorRepository.kt
│   │
│   └── 📋 classes/              # Modelos de datos
│       ├── Evento.kt
│       ├── Socio.kt
│       ├── Asistencia.kt
│       ├── Administrador.kt
│       └── Carnet.kt
│
├── 🗃️ DDBB/                    # Configuración de BD
│   ├── supabaseClient.kt        # Cliente de conexión
│   └── ConfigManager.kt         # Gestión de configuración
│
└── 🎨 ui/theme/                 # Temas y estilos
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

## 📱 Funcionalidades por Pantalla

### **🔑 LoginActivity**
- Autenticación de administradores
- Carga automática de eventos disponibles
- Validación de credenciales

### **📋 EventListActivity**
- Lista de eventos con Material Cards
- Botones de acción: Editar, Eliminar, Ver Ubicación
- FAB para crear nuevos eventos
- Confirmaciones de seguridad

### **✏️ EventDetailActivity**
- Formulario completo de eventos
- DatePicker nativo para fechas
- Maps integrado para ubicaciones
- Gestión de participantes en tiempo real
- Búsqueda de socios con filtros

## 🔒 Seguridad

- **Solo lectura** para administradores y socios (no se pueden crear/editar)
- **Validación de permisos** para ubicación
- **Confirmaciones** antes de acciones destructivas
- **Manejo de errores** robusto

## 🚀 Próximas Mejoras

- [ ] Exportación de datos de eventos
- [ ] Notificaciones push
- [ ] Modo offline
- [ ] Dashboard de estadísticas
- [ ] Filtros avanzados en lista de eventos

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 👥 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

**Desarrollado con ❤️ para la gestión moderna de asociaciones**
