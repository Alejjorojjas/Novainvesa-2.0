# Reglas de Negocio — Novainvesa v2.0

**Versión:** 2.1  
**Fecha:** Abril 2026  

---

## FASE 1 — Web (MVP actual)
## FASE 2 — App móvil iOS + Android (después del MVP)

---

## 1. Productos y catálogo

### RN-001 — Gestión del catálogo
- El catálogo se gestiona manualmente desde el panel de administración
- Los productos se importan desde Dropi usando el importador (ID o URL)
- No hay sincronización automática de catálogo con Dropi (limitación de su API)
- El admin puede editar nombre, precio, descripción y categoría antes de publicar

### RN-002 — Estados de producto
- `ACTIVE` → visible en la tienda, se puede comprar
- `DRAFT` → importado pero incompleto, solo visible en el admin
- `ARCHIVED` → retirado de la tienda, no se puede comprar ni aparece en búsquedas
- Un producto pasa a DRAFT automáticamente si le faltan campos requeridos al importar
- Un producto ARCHIVED mantiene sus pedidos históricos intactos

### RN-003 — Requisitos para publicar un producto
- Nombre: obligatorio
- Precio mayor a $0 COP: obligatorio
- Mínimo 1 imagen: obligatorio
- Categoría asignada: obligatorio
- dropi_product_id: obligatorio (se asigna automáticamente al importar)
- Descripción: opcional

### RN-004 — Precios
- Todos los precios en COP (pesos colombianos) sin decimales
- El precio de venta lo define el admin (puede diferir del precio sugerido de Dropi)
- El precio tachado (compare_at_price) es opcional — muestra una "oferta"
- El margen mínimo sugerido es del 30% sobre el precio del proveedor en Dropi

### RN-005 — Importación masiva
- Máximo 50 productos por importación masiva
- Si un producto ya existe (mismo dropi_product_id) → se muestra advertencia pero se permite actualizar
- Timeout de 10 segundos por producto — si Dropi no responde, se marca como ERROR
- Los errores no detienen el proceso — se continúa con el siguiente producto

---

## 2. Carrito de compras

### RN-010 — Límites del carrito
- Máximo 10 unidades del mismo producto por pedido
- Máximo 20 productos diferentes en el carrito
- Si el usuario intenta agregar más, se muestra mensaje de límite alcanzado

### RN-011 — Persistencia del carrito
- El carrito se guarda en localStorage con TTL de 7 días
- Al expirar, el carrito se limpia automáticamente
- El carrito se limpia al confirmar un pedido exitosamente
- Los usuarios registrados mantienen el carrito entre dispositivos (Fase 2)

### RN-012 — Validación antes del checkout
- No se puede ir al checkout con el carrito vacío
- Si un producto se agota entre que se agrega al carrito y se hace checkout, se notifica al usuario
- Los precios se validan en el backend al crear el pedido (no se confía en el precio del frontend)

---

## 3. Checkout y pedidos

### RN-014 — Datos requeridos en el checkout
| Campo | Validación |
|-------|-----------|
| Nombre completo | Mínimo 3 caracteres |
| Email | Formato válido |
| Teléfono | 10 dígitos, solo Colombia por ahora |
| Departamento | Obligatorio |
| Ciudad | Obligatorio |
| Dirección | Mínimo 10 caracteres |
| Barrio | Opcional |
| Notas | Opcional |
| Método de pago | Obligatorio |

### RN-015 — Código de pedido
- Formato: `NOVA-YYYYMMDD-NNNN`
- El número NNNN es un secuencial diario (se reinicia cada día)
- El primer pedido del día es NOVA-YYYYMMDD-0001
- Se genera de forma atómica en el backend para evitar duplicados
- Es único e inmutable — nunca se reutiliza

### RN-016 — Datos del cliente en el pedido
- Los datos del cliente se copian en el pedido al momento de crearlo
- Si el usuario actualiza su perfil después, el pedido mantiene los datos originales
- Esto garantiza que el historial del pedido es inmutable

### RN-017 — Registro opcional en checkout
- El usuario puede comprar sin registrarse (como invitado)
- Si compra como invitado, se le muestra un banner con beneficios de registrarse:
  - Rastrear pedidos fácilmente
  - Dirección guardada para la próxima compra
  - Lista de favoritos
- Si el usuario se registra durante el checkout, el pedido se vincula automáticamente a su cuenta

### RN-018 — Elegibilidad para COD (Pago Contra Entrega)
- COD disponible solo si:
  1. La ciudad del envío tiene cobertura confirmada de Dropi
  2. El total del pedido es menor a $500.000 COP
- Si no cumple alguna condición, la opción COD se oculta del selector de pago
- La verificación de cobertura se hace en tiempo real al ingresar la ciudad

---

## 4. Pagos

### RN-019 — Métodos de pago disponibles (Fase 1 — Colombia)
| Método | Disponibilidad | Límite |
|--------|---------------|--------|
| COD | Solo ciudades con cobertura Dropi + total < $500k | Sin límite mínimo |
| Wompi | Siempre disponible | Sin límite |
| MercadoPago | Siempre disponible | Sin límite |

### RN-020 — Flujo de pago digital
- Al seleccionar Wompi o MercadoPago, se crea una preferencia de pago en el backend
- La preferencia expira en 30 minutos
- El usuario es redirigido a la pasarela de pago
- Al volver, el sistema verifica el estado del pago via webhook
- Si el pago es aprobado → el pedido se confirma y se envía a Dropi via N8n
- Si el pago falla → el pedido queda en estado PENDING, no se envía a Dropi

### RN-021 — Verificación de webhooks
- Todos los webhooks de pasarelas de pago se verifican con HMAC-SHA256
- Si la firma no coincide → retornar 401 y NO procesar el webhook
- Los webhooks se procesan de forma idempotente (mismo evento dos veces = mismo resultado)

### RN-022 — Creación de pedido en Dropi
- El pedido en Dropi se crea SOLO después de confirmación de pago
- Para COD: se crea inmediatamente al confirmar el pedido
- Para Wompi/MercadoPago: se crea al recibir el webhook de pago aprobado
- NUNCA se crea el pedido en Dropi si el pago no fue confirmado

### RN-023 — Tiempos de entrega estimados
| Ciudad | Tiempo estimado |
|--------|----------------|
| Bogotá | 1-2 días hábiles |
| Medellín, Cali, Barranquilla | 2-3 días hábiles |
| Otras ciudades principales | 3-5 días hábiles |
| Ciudades intermedias | 5-7 días hábiles |

---

## 5. Automatización con N8n

### RN-025 — Flujo N8n → Dropi
- El backend envía un webhook a N8n al confirmar cada pedido
- N8n hace login en Dropi con las credenciales del admin
- N8n crea el pedido en Dropi con el dropi_product_id de cada item
- N8n retorna el dropiOrderId al backend para actualizar la BD
- Si N8n falla después de 3 intentos → se envía email al admin para gestión manual
- El admin puede reintentar la sincronización desde el panel

### RN-026 — Fallback manual
- Si la automatización falla, el pedido queda en estado `DROPI_ERROR`
- El admin recibe email inmediato con los datos del pedido para crearlo manualmente en Dropi
- El admin puede marcar el pedido como sincronizado manualmente desde el panel

---

## 6. Notificaciones

### RN-030 — Email de confirmación
- Se envía siempre que el cliente tenga email (campo obligatorio)
- Contenido: código de pedido, resumen de items, total, dirección, tiempo de entrega estimado
- Se reintenta 3 veces si falla (con delay de 5 minutos entre intentos)
- Si los 3 intentos fallan, se registra el error pero NO se cancela el pedido

### RN-031 — WhatsApp de confirmación
- Se envía solo si el cliente proporcionó número de teléfono
- Se envía via Chatea Pro API
- Si falla, no se reintenta (el email ya fue enviado como respaldo)
- Mensaje incluye: código de pedido, total y tiempo de entrega

### RN-032 — Notificaciones de estado
- El cliente recibe notificación cuando el pedido pasa a SHIPPED (en camino)
- Canal: WhatsApp si tiene número, si no email
- Incluye número de guía de la transportadora cuando esté disponible

---

## 7. Rastreo de pedidos

### RN-035 — Rastreo sin cuenta
- Cualquier persona con el código NOVA-... puede rastrear un pedido
- No requiere login
- Se muestra información limitada (estado, items, fecha estimada de entrega)
- El email del cliente se muestra parcialmente ocultado: `j***@gmail.com`

### RN-036 — Rastreo con cuenta
- Los usuarios registrados pueden ver el historial completo de sus pedidos
- Pueden ver todos los detalles sin restricciones
- Los pedidos hechos como invitado con el mismo email se vinculan automáticamente al registrarse

---

## 8. Administración

### RN-040 — Acceso al panel admin
- El panel de admin es accesible en `/admin`
- Requiere credenciales de admin_users (separadas de usuarios normales)
- El JWT admin tiene expiración de 24 horas (más corto que el de usuarios)
- JWT_ADMIN_SECRET es diferente a JWT_SECRET
- Rate limit: 3 intentos fallidos de login / 15 minutos / IP

### RN-041 — Roles de admin
| Rol | Permisos |
|-----|---------|
| `SUPER_ADMIN` | Todo: productos, pedidos, usuarios, admins, configuración |
| `ADMIN` | Productos, pedidos, usuarios (no puede gestionar otros admins) |

### RN-042 — Estados de pedido que el admin puede cambiar
```
PENDING     → CONFIRMED, CANCELLED
CONFIRMED   → PROCESSING, CANCELLED
PROCESSING  → SHIPPED, CANCELLED
SHIPPED     → DELIVERED, RETURNED
DELIVERED   → (estado final, no se puede cambiar)
RETURNED    → (estado final, no se puede cambiar)
CANCELLED   → (estado final, no se puede cambiar)
```

### RN-043 — Importador de productos
- Solo admins con rol SUPER_ADMIN o ADMIN pueden importar productos
- El importador detecta automáticamente si el input es un ID o una URL de Dropi
- Los productos importados quedan en borrador si les falta información obligatoria
- El admin debe revisar y publicar manualmente los borradores

---

## 9. Seguridad

### RN-050 — Contraseñas
- BCrypt con 12 rounds para usuarios y admins
- Mínimo 8 caracteres, al menos 1 letra y 1 número
- No se almacena la contraseña en texto plano en ningún log o respuesta

### RN-051 — Tokens JWT
- Usuarios: expiran en 7 días
- Admins: expiran en 24 horas
- Los tokens no se pueden revocar individualmente (sin blacklist en MVP)
- Al cambiar la contraseña, todos los tokens anteriores quedan inválidos

### RN-052 — Rate limiting
| Endpoint | Límite |
|---------|--------|
| POST /auth/login | 5 intentos / 15min / IP |
| POST /auth/register | 10 intentos / 15min / IP |
| POST /admin/auth/login | 3 intentos / 15min / IP |
| API general | 200 requests / 15min / IP |
| POST /orders | 10 pedidos / hora / IP |

---

## 10. FASE 2 — App móvil iOS + Android

> **Condición de inicio:** La Fase 2 comienza cuando el MVP web esté 100% funcional con al menos 10 ventas reales confirmadas.

### Arquitectura de la app móvil
```
App móvil (React Native + Expo)
         │
         │ REST API (mismo backend Spring Boot)
         ▼
Backend Spring Boot ── MySQL ── N8n ── Dropi
```

**El backend NO necesita modificaciones** para la app móvil. La misma API que usa el web sirve para la app.

### Tecnologías sugeridas para la app
```
Framework:    React Native con Expo (SDK 52+)
Navegación:   Expo Router (file-based, similar a Next.js)
UI:           NativeWind (Tailwind para React Native)
Estado:       Zustand (mismo que el web)
HTTP:         Axios (mismo que el web)
Auth:         JWT (mismo backend)
Push notifs:  Expo Notifications
Pagos:        MercadoPago SDK móvil + Wompi WebView
```

### Funcionalidades adicionales de la app vs web
| Feature | Web | App |
|---------|-----|-----|
| Catálogo y compras | ✅ | ✅ |
| Checkout completo | ✅ | ✅ |
| Rastreo de pedidos | ✅ | ✅ |
| Push notifications | ❌ | ✅ |
| Biometría (Face ID/huella) | ❌ | ✅ |
| Offline básico (ver favoritos) | ❌ | ✅ |
| Compartir productos | Parcial | ✅ |
| Modo cámara para buscar | ❌ | Futuro |

### Distribución de la app
- **Android:** Google Play Store
- **iOS:** Apple App Store
- **Nombre en tiendas:** "Novainvesa — Tienda Online"
- **Bundle ID:** `com.novainvesa.app`

### Ramas en GitHub para la app
```
main
└── dev
    └── feat/mobile/nombre-feature
        fix/mobile/nombre-bug
```
Carpeta en monorepo: `Mobile/` (se agrega en Fase 2)

---

## 11. Expansión internacional (Fase 3)

> Después de la app móvil, cuando Colombia esté consolidado.

```
Países objetivo (en orden):
1. Colombia    ← Fase 1 (MVP actual)
2. México      ← Fase 3
3. Chile       ← Fase 3
4. Perú        ← Fase 3
5. Argentina   ← Fase 4

Cambios necesarios para expansión:
- Múltiples monedas (COP, MXN, CLP, PEN, ARS)
- Proveedores locales por país (Dropi México, etc.)
- Pasarelas de pago por país
- Traducción completa (ya tenemos ES base)
- COD según cobertura de cada proveedor local
```

---

## Historial de cambios

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | Oct 2025 | Reglas iniciales |
| 2.0 | Abr 2026 | Actualización completa para v2 |
| 2.1 | Abr 2026 | Agregado Fase 2 app móvil + Fase 3 expansión |

