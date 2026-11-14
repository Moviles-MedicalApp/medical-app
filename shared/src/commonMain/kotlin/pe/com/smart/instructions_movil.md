# Instrucciones para Codex — Aplicación móvil MedicalApp (Kotlin Multiplatform)

## 0. Objetivo y forma de trabajar

Actúa como ingeniero **Kotlin Multiplatform / Compose Multiplatform / Material 3 / Ktor / MVVM**. Tu objetivo es adecuar el registro público y la administración de usuarios a la política de cuentas/perfiles acordada con el backend, **sin duplicar pacientes ni asumir endpoints no implementados**.

**Primero inspecciona el código real** y conserva firmas públicas de composables, callbacks, rutas de navegación, APIs y DTO salvo cambios contractuales verificados. Trabaja directamente en el repositorio. Mantén código completo compilable, no pseudocódigo. No rehagas módulos no relacionados. No afirmes que algo compila o está probado si no lo ejecutaste.

**Dependencia entre equipos:** consulta el contrato técnico que publique el Codex backend (`docs/registration-contract.md` o ubicación equivalente) y verifica rutas, estados HTTP y DTO antes de añadir pantallas o nuevas llamadas. Si ese contrato aún no existe, limita esta tarea a mejoras compatibles con los endpoints compartidos y documenta lo que queda bloqueado. **Nunca inventes un endpoint de comprobación de DNI, reclamación de cuenta o activación de correo.**

## 1. Contexto comprobado en la aplicación

- `shared/src/commonMain/kotlin/pe/com/smart/presentation/login/RegistrationScreen.kt` contiene los campos `username`, `email`, `name`, `lastName`, `dni`, `phone`, `password`, `confirmation`; la firma de navegación actual es `RegistrationScreen(onRegistered: () -> Unit, onBack: () -> Unit)` (en posible refactor posterior también se pasa un ViewModel por defecto).
- El flujo de registro público es `RegistrationScreen -> RegistrationViewModel (si ya existe, reutilizar) -> AuthRepository.registerPatient(PatientRegistrationRequest) -> AuthApiService.registerPatient -> POST /auth/register/patient -> 201 Created`.
- `AuthRepository` envuelve operaciones en `Result`, `TokenStorage` maneja access/refresh token, y el registro no inicia automáticamente una sesión por sí mismo.
- `presentation/users/UsersScreen.kt`, `UsersUiState.kt`, `UsersViewModel.kt` se usan para alta administrativa, listado, roles, estados y gestión de cuentas.
- `UserProvisioningSelection` es un handoff de navegación temporal: `profileId`, `role`, `profileName`, `specialityId`, `email`, `version`. Mantenerlo compatible y no persistir IDs de perfiles como credenciales.
- `AuthApiService.createAdminUser` usa `POST /auth/admin/users`; `getAvailableProfiles(type)` usa `GET /auth/admin/profiles/available?type=...`.
- Los roles existentes incluyen `ADMIN`, `DOCTOR`, `PATIENT`, `EMPLOYEE` según el backend. La autorización real siempre corresponde al servidor.
- Hay componentes UI globales como `AppLoading`, `AppError`, `AppEmptyState`, `AppConfirmDialog` y colores basados en `MaterialTheme.colorScheme`. Reutilizarlos cuando aplique.
- Se han reportado anteriormente errores de sintaxis al refactorizar `UsersViewModel` por cierres `try/catch` mal colocados. **Compilar tras cada cambio**, sin entregar archivos con errores de sintaxis.

## 2. Reglas de negocio que la UI debe respetar

**RM-01**. El registro desde Login es **solo para pacientes**; el formulario no puede asignar `ADMIN`, `DOCTOR` ni `EMPLOYEE`.

**RM-02**. DNI: cadena de **exactamente ocho dígitos** para el caso actual; filtrar caracteres no numéricos y limitar a ocho; no convertir a `Int` para conservar ceros a la izquierda. Esto mejora UX, pero **no sustituye la restricción única en backend**.

**RM-03**. Un paciente puede existir sin usuario. La UI administrativa debe distinguir **crear perfil clínico** de **crear cuenta de acceso** y **vincular perfil existente**. No crear un segundo paciente para otorgar acceso al primero.

**RM-04**. No ofrecer vinculación automática basada solo en DNI desde el registro público. Si el backend informa que se necesita reclamación/verificación, presentar pasos neutrales sin revelar datos personales ni afirmar que la cuenta ya existe para un DNI concreto.

**RM-05**. Cuando un ADMIN selecciona perfil existente, validar disponibilidad consultando backend y volver a verificarla al guardar en el servidor. Un `GET availableProfiles` vacío NO significa fallo de conectividad.

**RM-06**. La cuenta administrativa no debe compartir contraseñas definitivas. Cuando backend implemente activación por invitación, adaptar el UI a ese contrato. Hasta entonces, si el backend exige contraseña temporal, conservar el flujo actual, advertir al operador sobre entrega segura y no registrar contraseña en logs, analytics ni persistencia.

**RM-07**. Errores 400, 401, 403, 409 y 429 deben convertirse en mensajes comprensibles, sin mostrar trazas ni mensajes internos. Un `409` puede significar conflicto de datos/vinculación; mostrar alternativa de recuperación/asistencia **solo si esa ruta está realmente implementada**.

**RM-08**. `onRegistered()` se invoca **únicamente tras éxito 201 real**. No auto-login salvo que el backend y navegación lo soporten explícitamente. Evitar doble envío pulsando repetidamente el botón mientras `isLoading = true`.

**RM-09**. Retener datos no sensibles al fallar el registro, pero borrar `password` y `confirmation` después de éxito; no persistir contraseñas en `SavedStateHandle`, disco, logs o preferencias. No incluir credenciales en eventos de analytics.

**RM-10**. Respeta estados en el cliente: `PENDING`, `ACTIVE`, `BLOCKED`, `DISABLED`; no tratar `PENDING` como una cuenta activa sin que el backend lo permita.

## 3. Orden de ejecución

### Fase A — Auditoría

1. Leer implementaciones actuales de `RegistrationScreen`, `RegistrationViewModel`/`RegistrationUiState` si existen, `LoginScreen`, navegación de `onRegistered`/`onBack`, `AuthRepository`, `AuthApiService`, DTOs y `HttpClientFactory` (revisar `expectSuccess` y `HttpResponseValidator`).
2. Leer `UsersScreen`, `UsersViewModel`, `UsersUiState`, `UserProvisioningSelection`, formularios de paciente/médico y ruta administrativa de vinculación.
3. Verificar en el contrato backend si `registerPatient` y `createAdminUser` siguen devolviendo `201`; inspeccionar exactamente los códigos y cuerpos de error, las verificaciones de identidad y si existe invitación.
4. Detectar rutas duplicadas de creación de paciente, errores de UI que confundan perfil con cuenta, llamadas a repositorios directamente desde composables y duplicados de componentes. Documentar los hallazgos.

### Fase B — Registro público, MVVM

1. Mantener `RegistrationScreen(onRegistered, onBack)` compatible con la navegación existente. Si ViewModel se pasa como parámetro opcional, mantener los dos callbacks originales y sus nombres.
2. `RegistrationUiState`: datos, flags `isLoading`, error general y errores por campo. `RegistrationViewModel`: entradas, validaciones, llamada a `AuthRepository` y estado.
3. El formulario debe agrupar datos de acceso, datos personales y contraseña; respetar `safeDrawingPadding`/insets **sin duplicarlos** si el padre ya los aplica; usar teclado apropiado, `ImeAction`, desplazamiento con IME, indicadores de carga y visibilidad de contraseña.
4. Validar usuario (3..64), correo según contrato (hasta 64 si el backend así lo mantiene), contraseña (8..72 si coincide con servidor), nombres/apellidos (2..64), DNI de ocho dígitos y teléfono opcional. **No endurecer reglas arbitrariamente** (por ejemplo, exigir formato de contraseña distinto al backend).
5. Ejecutar validación antes de solicitar. Deshabilitar botón mientras carga. Al fallar, conservar campos y exhibir mensaje sin datos sensibles; al éxito limpiar contraseñas y llamar `onRegistered()` una sola vez.
6. Para DNI vinculado o perfil existente, mostrar mensaje seguro de asistencia/reclamación según contrato real; jamás recuperar automáticamente el perfil a partir de DNI.

### Fase C — Administración de usuarios

1. Mantener estructura `UsersScreen` solo UI, `UsersViewModel` lógica, `UsersUiState` estado. No trasladar `AuthRepository` ni `rememberCoroutineScope` a la UI.
2. Separar explícitamente en la interfaz: **perfil existente vinculado** vs **alta de nuevo perfil y cuenta**. Mostrar nombre y rol del perfil elegido, estados de elegibilidad y errores de verificación.
3. Para ADMIN verificar UI y permisos visuales, pero confiar en las validaciones backend. Nunca permitir que el autorregistro público escoja roles distintos de `PATIENT`.
4. Conservar búsqueda, filtros, paginación, edición, roles, bloqueo/desactivación, restablecimiento de contraseña, controles sobre cuenta propia. No perder funciones al refactorizar.
5. Si el backend publica invitación/activación, agregar estado `PENDING`, mensaje de invitación pendiente, reenvío **solo si existe endpoint real** y quitar contraseña temporal según contrato. De lo contrario mantener operación existente con advertencias de seguridad.
6. Ajustar manejo de errores HTTP en `AuthApiService` y/o repositorio al patrón del cliente Ktor existente; `ApiRequestException(statusCode, message)` se usa actualmente para disponibilidad de perfiles. Preservar códigos para que el ViewModel distinga 400/401/403/409/429.
7. Comprobar que `HttpResponseValidator` no lanza antes de que `AuthApiService` inspecte `response.status`; adaptar a un único manejo consistente, sin duplicar excepciones.

### Fase D — Integración, navegación y sesión

1. Inspeccionar qué hace `onRegistered()` en la navegación actual: confirmar si vuelve al login, muestra mensaje de éxito o navega al home. Cambiar solo si hay razón concreta y probar el comportamiento.
2. Si el resultado indica `PENDING`, mostrar mensaje coherente de activación; no guardar tokens inexistentes ni acceder a rutas privadas.
3. Vinculación administrativa: mantener selección de perfil por `profileId` y su rol; al éxito, limpiar handoff `UserProvisioningSelection`, actualizar listas y permitir regresar al módulo original sin cuenta duplicada.
4. Verificar que los errores de sesión expirada no se confundan con perfil no disponible; si corresponde usar el patrón de cierre/renovación de sesión existente.
5. No incorporar controles de consulta pública por DNI que revelen la existencia de pacientes, excepto si la API diseñada por backend cumple política de privacidad/verificación.

### Fase E — Pruebas y comprobación

Agregar al menos pruebas (unitarias o instrumentadas según infraestructura) para:

- DNI limitado a 8 y preservación de ceros a la izquierda.
- Validación de campos vacíos, contraseñas distintas, email inválido.
- Estado `isLoading` y bloqueo de doble envío.
- `201` exitoso -> exactamente un `onRegistered`, sin inicio de sesión implícito.
- `400`/`409`/`429`, errores de red y conservación de datos del formulario.
- Perfil vinculado/ausente en alta administrativa; respuesta vacía vs error al consultar disponibilidad.
- Acciones de cuenta y filtros existentes no se rompen.
- Render móvil con IME, scroll y safe areas en Android y iOS cuando ambos targets estén disponibles.

Ejecutar `./gradlew ...` para las tareas presentes en el repositorio; inspeccionar los módulos reales antes de decidir comandos. Anotar tests y build que pasaron, y los no ejecutables por falta de entorno.

## 4. Contrato actual y coordinación con backend

| Llamada existente | Comportamiento observado |
|---|---|
| `AuthRepository.registerPatient(PatientRegistrationRequest)` | `Result<RegisteredUser>`; solicitud a `POST /auth/register/patient`; éxito `201` |
| `AuthRepository.createAdministrativeUser(AdminUserRequest)` | `Result<RegisteredUser>`; solicitud a `POST /auth/admin/users`; éxito `201` |
| `AuthRepository.availableProfiles(type)` | `Result<List<AvailableProfileDto>>`; `GET /auth/admin/profiles/available` |
| `AuthRepository.users(...)`, `updateUser`, `changeUserStatus`, `resetUserPassword`, `disableUser` | Operaciones administrativas existentes que deben conservarse |
| `RegistrationScreen(onRegistered, onBack)` | Contrato de navegación que debe preservarse |

**Atención:** el backend Java denomina al DTO de alta administrativa `StaffRegistrationRequest` y al resultado `StaffUserResponse`, mientras Kotlin usa `AdminUserRequest`/`RegisteredUser`. Confirmar que sus campos y JSON coinciden; no asumir que los nombres de clases implican incompatibilidad. Si el backend cambia la respuesta, adaptar DTO Kotlin con evidencia y pruebas de serialización.

## 5. Entregables

1. Código final aplicado a archivos reales, siguiendo MVVM, sin usar lógica HTTP en composables.
2. Cualquier componente nuevo colocado en el paquete real de `presentation/login` o `presentation/users/components` según convenciones.
3. Errores HTTP traducidos a textos seguros y accionables.
4. Documentación breve de lo que requiere backend antes de habilitar vinculación/activación pública.
5. Build/tests y reporte de resultados sin afirmaciones no verificadas.
6. Resumen por archivo cambiado y checklist de flujo: nuevo paciente, paciente existente, perfil sin cuenta, alta administrativa, sesión vencida y fallo de servidor.

## 6. Restricciones de implementación

- **No** crear endpoints en el cliente que no existan en backend.
- **No** modificar esquemas ni lógica Spring Boot desde el repositorio móvil.
- **No** permitir acceso al perfil clínico solo por DNI.
- **No** persistir ni registrar contraseñas o tokens en mensajes de depuración.
- **No** modificar nombres de callbacks `onRegistered`, `onBack` ni rutas actuales sin actualizar/validar todos los consumidores.
- **No** duplicar el formulario de creación de pacientes para crear usuarios: son operaciones distintas.
- Usar `MaterialTheme.colorScheme`, estado inmutable, `StateFlow`, `collectAsStateWithLifecycle`, repositorios existentes y componentes globales cuando estén disponibles.
- El usuario requiere código completo y listo para copiar cuando se presente cualquier archivo Kotlin en explicaciones; el trabajo de Codex debe consistir en editar los archivos reales.
