# AsoAdmin

AsoAdmin es una aplicación móvil Android desarrollada en Kotlin que permite gestionar eventos y asociaciones. Utiliza Material Design 3 para una interfaz moderna y Supabase como backend.

## Características

- 🔐 Sistema de autenticación de administradores

- 📅 Gestión completa de eventos

  - Listado de eventos con búsqueda

  - Creación y edición de eventos

  - Eliminación de eventos con confirmación

  - Exportación de eventos

- 🎨 Interfaz moderna con Material Design 3

  - Tema claro personalizado

  - Componentes Material 3

  - Navegación intuitiva

- 🗃️ Integración con Supabase

## Tecnologías

- Kotlin

- Jetpack Compose

- Material Design 3

- Supabase

- dotenv-kotlin

## Requisitos

- Android Studio Meerkat | 2024.3.1 o superior

- SDK mínimo: Android 21

- Kotlin 1.9.0 o superior

## Configuración

1. Clona el repositorio

2. Copia .env.example a app/src/main/assets/.env

3. Configura las variables de Supabase en el archivo .env:

```

SUPABASE_URL=tu_url_aqui

SUPABASE_KEY=tu_key_aqui

```

4. Sincroniza el proyecto con Gradle

## Estructura del Proyecto

```

app/

├─ src/

│  ├─ main/

│  │  ├─ java/com/example/asoadmin/

│  │  │  ├─ classes/           # Modelos de datos

│  │  │  ├─ supabaseConection/ # Cliente de Supabase

│  │  │  ├─ ui/               # Temas y componentes UI

│  │  │  └─ activities/       # Actividades principales

│  │  └─ assets/             # Recursos y configuración

```
