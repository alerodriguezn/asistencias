# Sistema de Notificaciones y Comunicados - Asistencias App

## Descripción General

Este documento describe el sistema completo de notificaciones y comunicados implementado en la aplicación de Asistencias, que permite a los usuarios recibir notificaciones en tiempo real sobre cambios en solicitudes de asistencia y nuevos comunicados publicados.

## Características Principales

### 1. Sistema de Notificaciones
- **Notificaciones de Solicitudes**: Los estudiantes reciben notificaciones cuando cambia el estado de sus solicitudes de asistencia
- **Notificaciones de Comunicados**: Todos los usuarios reciben notificaciones cuando se publican comunicados dirigidos a su rol
- **Notificaciones en Tiempo Real**: Las notificaciones se crean automáticamente al realizar acciones específicas
- **Filtrado por Usuario**: Cada usuario ve solo sus notificaciones relevantes

### 2. Sistema de Comunicados
- **Creación de Comunicados**: Solo los administradores pueden crear comunicados
- **Destinatarios por Rol**: Los comunicados pueden dirigirse a roles específicos (Estudiante, Profesor, Administrador)
- **Categorías y Prioridades**: Los comunicados tienen categorías y niveles de prioridad
- **Seguimiento de Lectura**: Se registra qué usuarios han leído cada comunicado

## Arquitectura del Sistema

### Modelos de Datos

#### 1. AssistanceRequest (Solicitud de Asistencia)
```kotlin
data class AssistanceRequest(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val assistanceName: String = "",
    val description: String = "",
    val status: String = "Pendiente", // Pendiente, Aprobada, Rechazada
    val requestDate: Timestamp = Timestamp.now(),
    val reviewDate: Timestamp? = null,
    val reviewerId: String? = null,
    val reviewerName: String? = null,
    val comments: String = ""
)
```

#### 2. Comunicado
```kotlin
data class Comunicado(
    val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val autorId: String = "",
    val autorNombre: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val fechaModificacion: Timestamp = Timestamp.now(),
    val categoria: String = "", // General, Académico, Administrativo, etc.
    val prioridad: String = "Normal", // Baja, Normal, Alta, Urgente
    val activo: Boolean = true,
    val destinatarios: List<String> = emptyList(), // Lista de roles destinatarios
    val leidoPor: List<String> = emptyList() // Lista de IDs de usuarios que han leído
)
```

#### 3. Notificación
```kotlin
// Estructura en Firestore
{
    "id": "string",
    "title": "string",
    "message": "string",
    "timestamp": "timestamp",
    "read": "boolean",
    "userId": "string",
    "type": "string", // "status_change", "new_request", "new_comunicado"
    "requestId": "string", // opcional
    "comunicadoId": "string", // opcional
    // campos adicionales según el tipo
}
```

### ViewModels

#### 1. AssistanceRequestViewModel
- `createRequest()`: Crear nueva solicitud
- `getUserRequests()`: Obtener solicitudes del usuario
- `getAllRequests()`: Obtener todas las solicitudes (admin)
- `updateRequestStatus()`: Actualizar estado de solicitud
- `deleteRequest()`: Eliminar solicitud

#### 2. ComunicadoViewModel
- `createComunicado()`: Crear nuevo comunicado
- `getComunicadosForUser()`: Obtener comunicados para un rol específico
- `getAllComunicados()`: Obtener todos los comunicados (admin)
- `markAsRead()`: Marcar comunicado como leído
- `updateComunicado()`: Actualizar comunicado
- `deleteComunicado()`: Eliminar comunicado

### Servicios

#### 1. NotificationService
- `createStatusChangeNotification()`: Notificar cambio de estado
- `createNewRequestNotification()`: Notificar nueva solicitud
- `createComunicadoNotificationForUser()`: Notificar comunicado a usuario específico
- `createComunicadoNotificationsForRole()`: Notificar comunicado a todos los usuarios de un rol
- `getUserNotifications()`: Obtener notificaciones del usuario
- `markNotificationAsRead()`: Marcar notificación como leída

## Flujos de Usuario

### 1. Flujo de Solicitudes de Asistencia

#### Para Estudiantes:
1. **Crear Solicitud**: El estudiante navega a "Solicitar Asistencia"
2. **Llenar Formulario**: Completa los campos requeridos
3. **Enviar Solicitud**: Se crea la solicitud y se notifica a administradores
4. **Seguimiento**: Puede ver el estado en "Mis Solicitudes"
5. **Notificación**: Recibe notificación cuando cambia el estado

#### Para Administradores:
1. **Revisar Solicitudes**: Navega a "Revisar Solicitudes"
2. **Ver Detalles**: Revisa la información de cada solicitud
3. **Tomar Decisión**: Aprueba o rechaza la solicitud
4. **Comentarios**: Puede agregar comentarios explicativos
5. **Notificación Automática**: El estudiante recibe notificación del cambio

### 2. Flujo de Comunicados

#### Para Administradores:
1. **Crear Comunicado**: Navega a "Crear Comunicado"
2. **Configurar Destinatarios**: Selecciona roles destinatarios
3. **Definir Prioridad**: Establece nivel de prioridad
4. **Publicar**: Se crea el comunicado y se envían notificaciones automáticamente

#### Para Todos los Usuarios:
1. **Ver Comunicados**: Navega a "Comunicados"
2. **Filtrar por Rol**: Ve solo comunicados dirigidos a su rol
3. **Leer Detalles**: Toca un comunicado para ver contenido completo
4. **Marcar como Leído**: Se registra automáticamente al leer

## Pantallas Implementadas

### 1. Pantallas de Solicitudes
- **StudentAssistanceRequestScreen**: Formulario para crear solicitudes
- **MyRequestsScreen**: Lista de solicitudes del estudiante
- **RequestReviewScreen**: Panel de revisión para administradores

### 2. Pantallas de Comunicados
- **CreateComunicadoScreen**: Formulario para crear comunicados (solo admin)
- **ComunicadosScreen**: Lista de comunicados para todos los usuarios

### 3. Pantallas de Notificaciones
- **NotificationsScreen**: Lista de notificaciones del usuario
- **NotificationsSend**: Envío manual de notificaciones (admin)

## Sistema de Roles y Permisos

### Roles Definidos:
1. **Estudiante**: Puede crear solicitudes y ver comunicados dirigidos a estudiantes
2. **Profesor**: Puede gestionar cursos, jornadas y ver comunicados dirigidos a profesores
3. **Administrador**: Acceso completo, puede crear comunicados y revisar solicitudes

### Permisos por Funcionalidad:
- **Crear Comunicados**: Solo Administradores
- **Revisar Solicitudes**: Solo Administradores
- **Ver Comunicados**: Todos los usuarios (filtrados por rol)
- **Crear Solicitudes**: Solo Estudiantes
- **Gestionar Cursos/Jornadas**: Profesores y Administradores

## Navegación y Sidebar

### Rutas Comunes (Todos los Usuarios):
- Inicio
- Perfil
- Notificaciones
- Comunicados

### Rutas Específicas por Rol:

#### Estudiante:
- Solicitar Asistencia
- Mis Solicitudes

#### Profesor:
- Cursos
- Jornadas
- Tipos de Asistencia
- Nueva Asistencia

#### Administrador:
- Cursos
- Jornadas
- Tipos de Asistencia
- Nueva Asistencia
- Revisar Solicitudes
- Crear Comunicado

## Base de Datos (Firestore)

### Colecciones:

#### 1. `assistance_requests`
```javascript
{
  "id": "string",
  "studentId": "string",
  "studentName": "string",
  "assistanceName": "string",
  "description": "string",
  "status": "string",
  "requestDate": "timestamp",
  "reviewDate": "timestamp",
  "reviewerId": "string",
  "reviewerName": "string",
  "comments": "string"
}
```

#### 2. `comunicados`
```javascript
{
  "id": "string",
  "titulo": "string",
  "contenido": "string",
  "autorId": "string",
  "autorNombre": "string",
  "fechaCreacion": "timestamp",
  "fechaModificacion": "timestamp",
  "categoria": "string",
  "prioridad": "string",
  "activo": "boolean",
  "destinatarios": ["array"],
  "leidoPor": ["array"]
}
```

#### 3. `notifications`
```javascript
{
  "id": "string",
  "title": "string",
  "message": "string",
  "timestamp": "timestamp",
  "read": "boolean",
  "userId": "string",
  "type": "string",
  "requestId": "string",
  "comunicadoId": "string",
  "categoria": "string",
  "prioridad": "string",
  "autorNombre": "string"
}
```

## Uso del Sistema

### Para Administradores:

#### Crear un Comunicado:
1. Inicia sesión como administrador
2. Ve a "Crear Comunicado" desde el sidebar o pantalla de inicio
3. Completa el formulario:
   - Título del comunicado
   - Categoría (General, Académico, Administrativo, etc.)
   - Prioridad (Baja, Normal, Alta, Urgente)
   - Destinatarios (selecciona roles)
   - Contenido del comunicado
4. Presiona "Publicar Comunicado"
5. Se crean automáticamente notificaciones para todos los usuarios con roles destinatarios

#### Revisar Solicitudes:
1. Ve a "Revisar Solicitudes"
2. Revisa la lista de solicitudes pendientes
3. Toca una solicitud para ver detalles
4. Selecciona "Aprobar" o "Rechazar"
5. Agrega comentarios si es necesario
6. Presiona "Actualizar Estado"
7. El estudiante recibe notificación automática

### Para Estudiantes:

#### Crear Solicitud de Asistencia:
1. Ve a "Solicitar Asistencia"
2. Completa el formulario con los detalles
3. Presiona "Enviar Solicitud"
4. Recibe confirmación y puede ver su solicitud en "Mis Solicitudes"

#### Ver Comunicados:
1. Ve a "Comunicados" desde el sidebar
2. Ve la lista de comunicados dirigidos a estudiantes
3. Toca un comunicado para leer el contenido completo
4. Se marca automáticamente como leído

### Para Todos los Usuarios:

#### Ver Notificaciones:
1. Ve a "Notificaciones" desde el sidebar
2. Ve la lista de notificaciones no leídas
3. Toca una notificación para marcarla como leída
4. Las notificaciones se filtran por usuario

## Características Técnicas

### Notificaciones Automáticas:
- **Cambio de Estado**: Cuando un administrador aprueba/rechaza una solicitud
- **Nuevo Comunicado**: Cuando se publica un comunicado dirigido a roles específicos
- **Nueva Solicitud**: Cuando un estudiante crea una solicitud (para administradores)

### Filtrado Inteligente:
- **Por Usuario**: Cada usuario ve solo sus notificaciones
- **Por Rol**: Los comunicados se filtran según el rol del usuario
- **Por Estado**: Las solicitudes se filtran por estado (Pendiente, Aprobada, Rechazada)

### Interfaz Adaptativa:
- **Sidebar Dinámico**: Se adapta según el rol del usuario
- **Pantalla de Inicio**: Muestra opciones relevantes según el rol
- **Navegación Contextual**: Rutas específicas según permisos

## Consideraciones de Seguridad

1. **Validación de Roles**: Todas las operaciones verifican permisos
2. **Filtrado de Datos**: Los usuarios solo ven información relevante
3. **Autenticación**: Todas las operaciones requieren autenticación
4. **Autorización**: Verificación de permisos antes de operaciones críticas

## Mantenimiento y Escalabilidad

### Optimizaciones Implementadas:
- **Consultas Eficientes**: Uso de índices en Firestore
- **Paginación**: Carga progresiva de datos
- **Caché Local**: Almacenamiento de datos frecuentemente accedidos

### Monitoreo:
- **Logs de Errores**: Captura de errores en operaciones críticas
- **Métricas de Uso**: Seguimiento de funcionalidades más utilizadas
- **Performance**: Monitoreo de tiempos de respuesta

## Conclusión

El sistema de notificaciones y comunicados proporciona una experiencia completa y fluida para todos los usuarios de la aplicación de Asistencias. Con notificaciones automáticas, comunicados dirigidos por rol y una interfaz adaptativa, el sistema mejora significativamente la comunicación y el seguimiento de solicitudes en la institución educativa.

El sistema está diseñado para ser escalable, seguro y fácil de mantener, proporcionando una base sólida para futuras mejoras y funcionalidades adicionales.

## Instalación y Configuración

1. Asegúrate de tener Firebase configurado en el proyecto
2. Configura las reglas de Firestore para las nuevas colecciones
3. Crea los índices necesarios en Firestore
4. Compila y ejecuta la aplicación

## Soporte

Para problemas o preguntas sobre el sistema de notificaciones, revisa:
- Los logs de Firebase Console
- La documentación de Firestore
- Los logs de la aplicación Android 