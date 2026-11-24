# MedicalApp

Aplicación móvil multiplataforma para la gestión de una clínica o consultorio médico. Centraliza la administración de pacientes, médicos, especialidades, horarios de atención y citas médicas en una interfaz construida con Compose Multiplatform.

El proyecto comparte la mayor parte de la lógica y la interfaz entre Android e iOS, y se conecta a servicios remotos para autenticación y gestión de información clínica.

## Funcionalidades

- Inicio de sesión, registro y gestión de sesión.
- Panel principal adaptado al rol del usuario.
- Administración de pacientes: consulta, búsqueda, registro, edición y detalle.
- Administración de médicos y especialidades.
- Gestión de horarios y agenda médica.
- Consulta y gestión de citas médicas, incluida una vista de calendario.
- Mensajes, estados de carga y manejo de errores en la interfaz.

## Tecnologías

- [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/)
- [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
- Material 3
- Koin para inyección de dependencias
- Ktor Client para comunicación HTTP
- Kotlinx Serialization y Kotlinx DateTime
- Multiplatform Settings para almacenamiento local de la sesión
- Android y iOS como plataformas de destino

## Estructura del proyecto

```text
.
├── androidApp/        # Punto de entrada y configuración de Android
├── iosApp/            # Proyecto iOS y punto de entrada en Swift
├── shared/            # Código compartido entre plataformas
│   └── src/
│       ├── commonMain/    # UI, dominio, datos, navegación y DI compartidos
│       ├── androidMain/   # Implementaciones específicas de Android
│       └── iosMain/       # Implementaciones específicas de iOS
└── gradle/            # Wrapper y catálogo de versiones
```

Dentro de `shared/src/commonMain`, el código se organiza en capas:

- `presentation`: pantallas, componentes, navegación y `ViewModel`.
- `domain`: modelos, contratos de repositorio y casos de uso.
- `data`: repositorios, DTO, mapeadores, almacenamiento local y clientes API.
- `di`: configuración de dependencias con Koin.

## Requisitos

- JDK 17 o una versión compatible con Android Gradle Plugin.
- Android Studio actualizado para compilar y ejecutar Android.
- Un emulador o dispositivo con Android 7.0 (API 24) o superior.
- macOS con Xcode para compilar y ejecutar la aplicación iOS.

No es necesario instalar Gradle de forma global: el proyecto incluye Gradle Wrapper.

## Instalación y ejecución

1. Clona el repositorio:

   ```bash
   git clone https://github.com/Moviles-MedicalApp/medical-app.git
   cd medical-app
   ```

2. Abre el proyecto en Android Studio y espera a que finalice la sincronización de Gradle.

3. Ejecuta la aplicación en Android desde la configuración `androidApp`, o compila un APK de depuración:

   **Windows**

   ```powershell
   .\gradlew.bat :androidApp:assembleDebug
   ```

   **macOS / Linux**

   ```bash
   ./gradlew :androidApp:assembleDebug
   ```

El APK resultante se genera en `androidApp/build/outputs/apk/debug/`.

### iOS

En macOS, abre la carpeta `iosApp` con Xcode, selecciona un simulador o dispositivo y ejecuta el esquema de la aplicación. Xcode compilará el framework compartido necesario para la app iOS.

## Configuración del backend

La aplicación consume los servicios publicados a través de la siguiente URL base:

```text
https://alexguevara.dev
```

Si necesitas apuntar a otro ambiente, actualiza `BASE_URL` en [`ApiConstants.kt`](shared/src/commonMain/kotlin/pe/com/smart/data/remote/api/support/ApiConstants.kt). No subas credenciales, tokens ni configuraciones sensibles al repositorio.

## Pruebas

Para ejecutar las pruebas disponibles del módulo compartido:

**Windows**

```powershell
.\gradlew.bat :shared:testAndroidHostTest
```

**macOS / Linux**

```bash
./gradlew :shared:testAndroidHostTest
```

Las pruebas de iOS se ejecutan desde macOS con un simulador configurado:

```bash
./gradlew :shared:iosSimulatorArm64Test
```

## Estado del proyecto

El desarrollo de esta versión se encuentra cerrado. El repositorio se conserva como referencia del proyecto y de su implementación actual. Las funcionalidades dependen de la disponibilidad y de los contratos de los servicios backend.
