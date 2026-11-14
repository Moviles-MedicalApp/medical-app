# Contrato de registro y vinculación

Estado verificado para el backend Docker/WSL del 14 de noviembre de 2026.

## Arquitectura e integridad

`auth-server` y `medical-appointment` usan la misma base PostgreSQL y las mismas tablas
`users`, `patients`, `doctors` y `employees`. El alta de cuenta y perfil ejecutada por
`auth-server` es una sola transacción local; no existe una llamada HTTP ni una
transacción distribuida en este flujo.

`patients.dni` es un `String` de ocho dígitos y es único globalmente, incluso si el
perfil tiene `deleted=true`. Un soft delete conserva identidad, citas e historial y no
libera el DNI. `patients.user_id` puede ser nulo y es único cuando existe.

La migración `002_enforce_global_patient_dni_uniqueness.sql` se detiene con un mensaje
accionable si encuentra duplicados. No elimina ni combina pacientes automáticamente.
La conciliación, si fuese necesaria, debe decidir manualmente qué perfil conserva el
DNI y mover antes todas las referencias de citas y cuenta.

## Autorregistro público de paciente

```http
POST /auth-server/auth/register/patient
Content-Type: application/json
```

También se conserva el alias `POST /auth-server/auth/register`.

```json
{
  "username": "juan.perez",
  "email": "juan@example.com",
  "password": "ClaveSegura123!",
  "name": "Juan",
  "lastName": "Pérez",
  "dni": "01234567",
  "phone": "987654321"
}
```

El servidor valida usuario, correo, contraseña de 8 a 72 caracteres, nombres, teléfono
y DNI de exactamente ocho dígitos. El cliente no envía `role`, `userId` ni `profileId`.
El único rol asignado es `PATIENT` y la contraseña se guarda con el
`PasswordEncoder` configurado.

Respuesta `201 Created`:

```json
{
  "id": 15,
  "patientId": 8,
  "username": "juan.perez",
  "email": "juan@example.com",
  "roles": ["PATIENT"]
}
```

Si ya existe usuario, correo o DNI —incluido un perfil eliminado o uno sin cuenta— la
respuesta es `409 Conflict` con un mensaje genérico. El registro público nunca reactiva
ni sobrescribe un paciente y nunca vincula una cuenta solo porque el solicitante conoce
el DNI. La interfaz debe ofrecer recuperación de acceso o vinculación asistida, sin
mostrar nombres, correos ni el estado concreto del perfil encontrado.

No existe todavía infraestructura de verificación de identidad o invitación por correo.
Por seguridad no se agregó un endpoint público de reclamación incompleto.

## Alta y vinculación administrativa

```http
POST /auth-server/auth/admin/users
Authorization: Bearer <accessToken-admin>
Content-Type: application/json
```

Éxito: `201 Created`. `auth-server` valida nuevamente que quien llama sea una cuenta
activa con rol `ADMIN`.

Cuando se envía `profileId`, el backend toma un bloqueo de escritura sobre el perfil y
comprueba dentro de la transacción que existe, corresponde al tipo solicitado, está
activo y no tiene cuenta. Un listado previo de perfiles disponibles no reserva el
perfil; la app debe manejar `409` si otro administrador lo vinculó primero.

Para vincular un paciente existente se envían `role=PATIENT`, `profileId`, usuario,
correo y contraseña inicial. Para médicos y empleados se puede vincular un perfil o
crear uno nuevo conforme al DTO vigente. `ADMIN` se crea sin perfil.

La implementación vigente mantiene cuentas administrativas nuevas en estado `ACTIVE`
con contraseña inicial porque no hay servicio real de invitaciones. La aplicación no
debe almacenar ni registrar esa contraseña y debe entregarla por un canal seguro. El
flujo futuro recomendado es invitación de un solo uso y cambio obligatorio, pero no se
declara implementado.

## Perfiles elegibles

```http
GET /auth-server/auth/admin/profiles/available?type=PATIENT|DOCTOR|EMPLOYEE
Authorization: Bearer <accessToken-admin>
```

Respuesta `200 OK`. Solo incluye perfiles activos con `user_id IS NULL` y no expone
`user_id`:

```json
[
  {
    "type": "PATIENT",
    "profileId": 8,
    "fullName": "Juan Pérez",
    "identifier": "01234567"
  }
]
```

## Códigos HTTP relevantes

- `201`: registro o alta completada.
- `400`: datos o tipo de perfil inválidos.
- `401`: token ausente, inválido o expirado.
- `403`: cuenta inactiva o rol insuficiente.
- `404`: perfil solicitado por administración inexistente.
- `409`: identidad duplicada, perfil eliminado, ya vinculado o carrera concurrente.

Los detalles SQL y nombres de constraints no forman parte de la respuesta pública.

## Seguridad de red e identidad

Las apps consumen exclusivamente el Gateway. Este elimina cualquier `X-User-Id`,
`X-Username` y `X-User-Roles` recibido del cliente y los vuelve a crear después de
validar el access token con `auth-server`. En el Compose base los microservicios solo se
exponen a la red interna; el override de desarrollo publica sus puertos internos
únicamente en `127.0.0.1`. El Gateway sigue disponible para emuladores/dispositivos en
el puerto `4040`.
