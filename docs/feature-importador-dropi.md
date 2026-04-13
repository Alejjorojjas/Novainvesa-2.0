# Feature: Importador de Productos desde Dropi
## Novainvesa v2.1

**Tipo:** Nueva funcionalidad — Panel de Administración  
**Prioridad:** Alta  
**Estimado:** 3-4 días de desarrollo  

---

## Descripción

El importador permite al admin traer productos de Dropi a Novainvesa pegando el ID del producto o la URL completa. Soporta importación individual y masiva. Antes de publicar, el admin puede editar todos los campos del producto.

---

## Flujo de usuario

### Importación individual
```
1. Admin entra al panel → sección "Importar productos"
2. Pega ID (ej: 1865251) o URL de Dropi en el campo
3. Clic en "Buscar producto"
4. Sistema detecta formato y llama a Dropi API
5. Se muestra formulario pre-llenado con los datos de Dropi
6. Admin edita lo que necesite (nombre, precio, categoría, descripción)
7. Admin decide: "Publicar ahora" o "Guardar borrador"
8. Si toda la info requerida está → se publica
9. Si falta algo → se guarda como borrador con indicador de qué falta
```

### Importación masiva
```
1. Admin pega múltiples IDs o URLs (uno por línea o separados por coma)
2. Clic en "Importar todos"
3. Sistema muestra barra de progreso (3/10 importados...)
4. Cada producto se procesa:
   - Si tiene toda la info → PUBLICADO automáticamente
   - Si falta categoría u otro campo → BORRADOR
   - Si Dropi no lo encuentra → ERROR (se muestra en el resumen)
5. Resumen final: "8 publicados, 1 borrador, 1 error"
6. Los borradores quedan en la lista para editar después
```

---

## Detección de formato (ID vs URL)

```java
// Si es solo números → es un ID
// Si contiene "dropi.co" o "/" → es una URL, extraer el ID
public String extractDropiId(String input) {
    input = input.trim();
    if (input.matches("\\d+")) {
        return input; // Ya es un ID
    }
    // Extraer ID de URL: https://app.dropi.co/dashboard/products/1865251
    Pattern pattern = Pattern.compile("/(\\d+)(?:[/?]|$)");
    Matcher matcher = pattern.matcher(input);
    if (matcher.find()) {
        return matcher.group(1);
    }
    throw new InvalidDropiInputException("Formato no reconocido: " + input);
}
```

---

## Reglas de publicación automática

| Campo | Requerido para publicar |
|-------|------------------------|
| `name` | ✅ Sí |
| `price` > 0 | ✅ Sí |
| `images` (mínimo 1) | ✅ Sí |
| `category_slug` | ✅ Sí |
| `dropi_product_id` | ✅ Sí (automático) |
| `description` | ❌ No |
| `benefits` | ❌ No |

Si falta algún campo requerido → `status = DRAFT`  
Si tiene todos los campos requeridos → `status = ACTIVE`

---

## Endpoints nuevos en Spring Boot

### Buscar producto en Dropi
```
POST /api/v1/admin/products/preview
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "input": "1865251"  // ID o URL de Dropi
}

Response 200:
{
  "success": true,
  "data": {
    "dropiId": "1865251",
    "name": "Ejercitador Multi",
    "price": 36500,
    "suggestedPrice": 36500,
    "images": ["https://cdn.dropi.co/..."],
    "description": "...",
    "benefits": ["..."],
    "category": "fitness",
    "inStock": true,
    "alreadyImported": false  // true si ya existe en la BD
  }
}
```

### Importar y guardar producto
```
POST /api/v1/admin/products/import
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "dropiProductId": "1865251",
  "name": "Ejercitador Multi — Kit 7 en 1",
  "price": 39900,
  "description": "...",
  "categorySlug": "fitness",
  "images": ["https://cdn.dropi.co/..."],
  "benefits": ["..."],
  "featured": false,
  "publishImmediately": true
}

Response 201:
{
  "success": true,
  "data": {
    "id": 5,
    "slug": "ejercitador-multi-kit-7-en-1",
    "status": "ACTIVE",  // o "DRAFT"
    "missingFields": []  // campos que faltan si es DRAFT
  }
}
```

### Importación masiva
```
POST /api/v1/admin/products/import-bulk
Authorization: Bearer {admin_token}
Content-Type: application/json

Body:
{
  "inputs": ["1865251", "1923456", "https://app.dropi.co/.../1734521"]
}

Response 202: (aceptado, procesamiento async)
{
  "success": true,
  "data": {
    "jobId": "bulk-import-abc123",
    "total": 3,
    "status": "PROCESSING"
  }
}

// Polling para ver progreso:
GET /api/v1/admin/products/import-bulk/{jobId}
Response:
{
  "success": true,
  "data": {
    "jobId": "bulk-import-abc123",
    "total": 3,
    "processed": 2,
    "published": 1,
    "drafts": 1,
    "errors": 0,
    "status": "PROCESSING"  // o "COMPLETED"
  }
}
```

### Editar producto (borrador o publicado)
```
PUT /api/v1/admin/products/{id}
Authorization: Bearer {admin_token}

Body: (campos a actualizar)
{
  "name": "Nuevo nombre",
  "price": 42000,
  "categorySlug": "fitness",
  "description": "..."
}
```

### Publicar borrador
```
POST /api/v1/admin/products/{id}/publish
Authorization: Bearer {admin_token}

Response 200:
{
  "success": true,
  "data": {
    "id": 5,
    "status": "ACTIVE"
  }
}
```

### Archivar producto
```
POST /api/v1/admin/products/{id}/archive
Authorization: Bearer {admin_token}
```

---

## Cambios en el modelo de datos

### Tabla `products` — campos adicionales

```sql
ALTER TABLE products 
ADD COLUMN status ENUM('ACTIVE', 'DRAFT', 'ARCHIVED') 
  NOT NULL DEFAULT 'ACTIVE' AFTER active,
ADD COLUMN missing_fields JSON 
  COMMENT 'Campos faltantes para publicar (solo en DRAFT)' 
  AFTER status,
ADD COLUMN imported_at DATETIME 
  COMMENT 'Cuándo se importó desde Dropi'
  AFTER updated_at,
ADD INDEX idx_status (status);
```

---

## Componentes de frontend (Next.js)

### Página del importador
```
app/[locale]/admin/productos/importar/page.tsx

Componentes:
- <ImportadorInput />     → campo de texto + botón "Buscar"
- <ImportadorMasivo />    → textarea para múltiples IDs + progreso
- <ProductoPreview />     → formulario editable pre-llenado
- <ProgressoBulk />       → barra de progreso importación masiva
- <ResumenImportacion />  → tabla de resultados al terminar
```

---

## Integración con Dropi API

```java
// DropiService.java — método para obtener producto por ID
public DropiProductDTO getProductById(String dropiId) {
    try {
        String token = getIntegrationToken(); // token de Mis Integraciones
        ResponseEntity<Map> response = restTemplate.exchange(
            dropiApiUrl + "/api/v1/products/" + dropiId,
            HttpMethod.GET,
            new HttpEntity<>(buildHeaders(token)),
            Map.class
        );
        return mapToDto(response.getBody());
    } catch (HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new ProductNotFoundException("Producto " + dropiId + " no encontrado en Dropi");
        }
        throw new DropiApiException("Error al consultar Dropi: " + e.getMessage());
    }
}
```

---

## Pantalla del importador (UI)

```
┌─────────────────────────────────────────────────────────┐
│  📦 Importar productos desde Dropi                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  [ Importación individual ]  [ Importación masiva ]     │
│                                                         │
│  Pega el ID o URL del producto:                         │
│  ┌─────────────────────────────────────┐ [Buscar]      │
│  │ 1865251 o https://app.dropi.co/...  │               │
│  └─────────────────────────────────────┘               │
│                                                         │
│  ─────────── Vista previa del producto ───────────      │
│                                                         │
│  [🖼️ Imagen]  Nombre: [Ejercitador Multi ________]     │
│               Precio: [$________ COP]                   │
│               Categoría: [Fitness ▼]                   │
│               Descripción: [____________]               │
│                                                         │
│  Imágenes disponibles: ☑️ img1  ☑️ img2  ☐ img3        │
│                                                         │
│  Estado: ✅ Listo para publicar                         │
│                                                         │
│  [💾 Guardar borrador]      [🚀 Publicar ahora]        │
└─────────────────────────────────────────────────────────┘
```

---

## Consideraciones

1. Si el producto ya existe en la BD (`dropi_product_id` duplicado) → mostrar advertencia pero permitir actualizar
2. Las imágenes se guardan como URLs de Dropi CloudFront, no se descargan
3. El slug se genera automáticamente desde el nombre (slugify)
4. Si el slug ya existe, agregar sufijo: `-2`, `-3`, etc.
5. La importación masiva procesa máximo 50 productos por vez
6. Timeout de 10 segundos por producto — si Dropi no responde, marcar como ERROR

