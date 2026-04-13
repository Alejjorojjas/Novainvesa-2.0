# Modelo de Datos — Novainvesa v2.0
## MySQL 8.0 — Hostinger

**Versión:** 2.1  
**Fecha:** Abril 2026  
**Base de datos:** `u228070604_novainvesa_db`  
**Host externo:** `srv1070.hstgr.io`  
**Timezone:** `-05:00` (Colombia)  

---

## Diagrama de relaciones

```
users ──────────────── orders ──────────── order_items
  │                      │                     │
  │                      │                     │
user_addresses        products ────────── product_stats
  │                      │
user_payment_prefs    categories
  │
wishlist ──────────── products

admin_users
import_jobs
product_searches
```

---

## Script SQL completo

```sql
-- ================================================================
-- NOVAINVESA v2.0 — Script de creación de base de datos
-- Ejecutar en phpMyAdmin de Hostinger
-- ================================================================

SET NAMES utf8mb4;
SET time_zone = '-05:00';

-- ----------------------------------------------------------------
-- 1. CATEGORÍAS
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
  id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  slug          VARCHAR(100) NOT NULL UNIQUE,
  name          VARCHAR(100) NOT NULL,
  icon          VARCHAR(10)  NOT NULL COMMENT 'Emoji del ícono',
  color         VARCHAR(7)   NOT NULL COMMENT 'Color hex ej: #EF4444',
  description   VARCHAR(300),
  active        BOOLEAN      NOT NULL DEFAULT TRUE,
  sort_order    INT          NOT NULL DEFAULT 0,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_active (active),
  INDEX idx_sort  (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Datos iniciales de categorías
INSERT INTO categories (slug, name, icon, color, sort_order) VALUES
  ('mascotas',   'Mascotas',   '🐾', '#F59E0B', 1),
  ('hogar',      'Hogar',      '🏠', '#10B981', 2),
  ('tecnologia', 'Tecnología', '📱', '#6366F1', 3),
  ('belleza',    'Belleza',    '💄', '#EC4899', 4),
  ('fitness',    'Fitness',    '💪', '#EF4444', 5);

-- ----------------------------------------------------------------
-- 2. PRODUCTOS
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
  id                INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  dropi_product_id  VARCHAR(100) NOT NULL UNIQUE COMMENT 'ID del producto en Dropi',
  name              VARCHAR(255) NOT NULL,
  slug              VARCHAR(255) NOT NULL UNIQUE,
  category_slug     VARCHAR(100) NOT NULL,
  short_description VARCHAR(300),
  description       TEXT,
  price             DECIMAL(12,2) NOT NULL COMMENT 'Precio de venta en COP',
  compare_at_price  DECIMAL(12,2)          COMMENT 'Precio tachado (oferta)',
  currency          CHAR(3)      NOT NULL DEFAULT 'COP',
  images            JSON                   COMMENT 'Array de URLs de imágenes',
  benefits          JSON                   COMMENT 'Array de beneficios del producto',
  status            ENUM('ACTIVE','DRAFT','ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
  missing_fields    JSON                   COMMENT 'Campos faltantes si está en DRAFT',
  in_stock          BOOLEAN      NOT NULL DEFAULT TRUE,
  featured          BOOLEAN      NOT NULL DEFAULT FALSE,
  weight            DECIMAL(6,2)           COMMENT 'Peso en kg',
  active            BOOLEAN      NOT NULL DEFAULT TRUE,
  imported_at       DATETIME               COMMENT 'Cuándo se importó desde Dropi',
  created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (category_slug) REFERENCES categories(slug) ON UPDATE CASCADE,
  INDEX idx_category (category_slug),
  INDEX idx_status   (status),
  INDEX idx_featured (featured),
  INDEX idx_active   (active),
  INDEX idx_slug     (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------
-- 3. USUARIOS (compradores)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
  id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  email           VARCHAR(255) NOT NULL UNIQUE,
  password_hash   VARCHAR(255) NOT NULL COMMENT 'BCrypt 12 rounds',
  full_name       VARCHAR(255) NOT NULL,
  phone           VARCHAR(20),
  is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
  last_login_at   DATETIME,
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email  (email),
  INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------
-- 4. DIRECCIONES DE USUARIOS
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_addresses (
  id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id     INT UNSIGNED NOT NULL,
  full_name   VARCHAR(255) NOT NULL,
  phone       VARCHAR(20),
  department  VARCHAR(100) NOT NULL,
  city        VARCHAR(100) NOT NULL,
  address     VARCHAR(255) NOT NULL,
  neighborhood VARCHAR(100),
  notes       VARCHAR(255)           COMMENT 'Instrucciones adicionales',
  is_default  BOOLEAN      NOT NULL DEFAULT FALSE,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------
-- 5. PREFERENCIAS DE PAGO DE USUARIOS
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_payment_preferences (
  id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id         INT UNSIGNED NOT NULL UNIQUE,
  preferred_method ENUM('COD','WOMPI','MERCADOPAGO'),
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------
-- 6. WISHLIST
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS wishlist (
  id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id     INT UNSIGNED NOT NULL,
  product_id  INT UNSIGNED NOT NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_product (user_id, product_id),
  FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
  INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------
-- 7. PEDIDOS
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
  id                   INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  order_code           VARCHAR(30)  NOT NULL UNIQUE COMMENT 'NOVA-YYYYMMDD-NNNN',
  user_id              INT UNSIGNED           COMMENT 'NULL si es invitado',

  -- Datos del cliente (copiados en el momento del pedido)
  customer_name        VARCHAR(255) NOT NULL,
  customer_email       VARCHAR(255) NOT NULL,
  customer_phone       VARCHAR(20),
  customer_id_number   VARCHAR(20)            COMMENT 'Cédula',

  -- Dirección de envío
  shipping_department  VARCHAR(100) NOT NULL,
  shipping_city        VARCHAR(100) NOT NULL,
  shipping_address     VARCHAR(255) NOT NULL,
  shipping_neighborhood VARCHAR(100),
  shipping_notes       VARCHAR(255),

  -- Totales
  subtotal             DECIMAL(12,2) NOT NULL,
  shipping_cost        DECIMAL(12,2) NOT NULL DEFAULT 0,
  total                DECIMAL(12,2) NOT NULL,
  currency             CHAR(3)       NOT NULL DEFAULT 'COP',

  -- Pago
  payment_method       ENUM('COD','WOMPI','MERCADOPAGO') NOT NULL,
  payment_status       ENUM('PENDING','CONFIRMED','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
  wompi_transaction_id VARCHAR(100)           COMMENT 'ID de transacción Wompi',
  mp_payment_id        VARCHAR(100)           COMMENT 'ID de pago MercadoPago',
  mp_preference_id     VARCHAR(100)           COMMENT 'ID de preferencia MercadoPago',

  -- Estado del pedido
  order_status         ENUM('PENDING','CONFIRMED','PROCESSING','SHIPPED','DELIVERED','RETURNED','CANCELLED')
                       NOT NULL DEFAULT 'PENDING',

  -- Integración Dropi / N8n
  dropi_order_id       VARCHAR(100)           COMMENT 'ID del pedido en Dropi',
  n8n_job_id           VARCHAR(100)           COMMENT 'ID del job en N8n',
  dropi_sync_status    ENUM('PENDING','SUCCESS','FAILED') DEFAULT 'PENDING',
  dropi_sync_attempts  INT          NOT NULL DEFAULT 0,
  dropi_sync_error     TEXT                   COMMENT 'Último error de sincronización con Dropi',

  -- Metadatos
  ip_address           VARCHAR(45)            COMMENT 'IP del cliente al hacer el pedido',
  user_agent           VARCHAR(500),
  created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
  INDEX idx_order_code    (order_code),
  INDEX idx_user          (user_id),
  INDEX idx_order_status  (order_status),
  INDEX idx_payment_status(payment_status),
  INDEX idx_created_at    (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------
-- 8. ITEMS DE PEDIDOS
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
  id               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  order_id         INT UNSIGNED  NOT NULL,
  product_id       INT UNSIGNED              COMMENT 'NULL si el producto fue archivado',
  dropi_product_id VARCHAR(100)  NOT NULL    COMMENT 'Siempre guardado para enviar a Dropi',

  -- Datos del producto copiados en el momento del pedido
  product_name     VARCHAR(255)  NOT NULL,
  product_image    VARCHAR(500)             COMMENT 'URL de la imagen principal',
  product_slug     VARCHAR(255),

  unit_price       DECIMAL(12,2) NOT NULL,
  quantity         INT           NOT NULL DEFAULT 1,
  subtotal         DECIMAL(12,2) NOT NULL,

  FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL,
  INDEX idx_order   (order_id),
  INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------
-- 9. ADMINISTRADORES
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS admin_users (
  id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  email         VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 12 rounds',
  full_name     VARCHAR(255) NOT NULL,
  role          ENUM('SUPER_ADMIN','ADMIN') NOT NULL DEFAULT 'ADMIN',
  is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
  last_login_at DATETIME,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------
-- 10. MÉTRICAS DE PRODUCTOS
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_stats (
  product_id      INT UNSIGNED  NOT NULL PRIMARY KEY,
  views           BIGINT        NOT NULL DEFAULT 0,
  cart_adds       BIGINT        NOT NULL DEFAULT 0,
  wishlist_count  INT           NOT NULL DEFAULT 0,
  orders_count    INT           NOT NULL DEFAULT 0,
  units_sold      INT           NOT NULL DEFAULT 0,
  total_revenue   DECIMAL(15,2) NOT NULL DEFAULT 0,
  last_updated    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------
-- 11. BÚSQUEDAS (analítica)
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_searches (
  id             INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  query          VARCHAR(255) NOT NULL,
  results_count  INT          NOT NULL DEFAULT 0,
  user_id        INT UNSIGNED           COMMENT 'NULL si no está autenticado',
  session_id     VARCHAR(100),
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
  INDEX idx_query      (query),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------
-- 12. JOBS DE IMPORTACIÓN MASIVA
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS import_jobs (
  id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  job_id       VARCHAR(100) NOT NULL UNIQUE COMMENT 'UUID del job',
  admin_id     INT UNSIGNED NOT NULL,
  total        INT          NOT NULL DEFAULT 0,
  processed    INT          NOT NULL DEFAULT 0,
  published    INT          NOT NULL DEFAULT 0,
  drafts       INT          NOT NULL DEFAULT 0,
  errors       INT          NOT NULL DEFAULT 0,
  status       ENUM('PENDING','PROCESSING','COMPLETED','FAILED') NOT NULL DEFAULT 'PENDING',
  results      JSON                   COMMENT 'Detalle de cada producto importado',
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME,
  FOREIGN KEY (admin_id) REFERENCES admin_users(id) ON DELETE CASCADE,
  INDEX idx_job_id (job_id),
  INDEX idx_admin  (admin_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## Datos iniciales del admin

```sql
-- IMPORTANTE: Cambiar el password_hash por uno real generado con BCrypt 12 rounds
-- Puedes generar uno en: https://bcrypt-generator.com/

INSERT INTO admin_users (email, password_hash, full_name, role) VALUES (
  'admin@novainvesa.com',
  '$2a$12$REEMPLAZAR_CON_HASH_REAL',
  'Alejandro',
  'SUPER_ADMIN'
);
```

---

## Índices de rendimiento adicionales

```sql
-- Para búsquedas de texto en productos
ALTER TABLE products ADD FULLTEXT INDEX ft_products (name, short_description, description);

-- Para reportes del admin por fecha
ALTER TABLE orders ADD INDEX idx_created_date (DATE(created_at));

-- Para consultas de pedidos por estado + fecha
ALTER TABLE orders ADD INDEX idx_status_date (order_status, created_at);
```

---

## Notas importantes

### Sobre los booleanos en MySQL
MySQL retorna `1` y `0` en lugar de `true` y `false`. En Spring Boot, mapear explícitamente:
```java
// En la entidad JPA
@Column(name = "in_stock")
private Boolean inStock; // Spring Data JPA convierte automáticamente
```

### Sobre los campos JSON
MySQL 8.0 soporta tipo JSON nativo. En Spring Boot:
```java
@Column(columnDefinition = "JSON")
@Convert(converter = JsonListConverter.class)
private List<String> images;
```

### Sobre el timezone
Siempre usar `-05:00` (Colombia). Configurar en `application.properties`:
```properties
spring.jpa.properties.hibernate.jdbc.time_zone=America/Bogota
```

### Sobre el order_code
El formato `NOVA-YYYYMMDD-NNNN` usa un secuencial diario. Ejemplo:
- Primer pedido del 13 de abril: `NOVA-20260413-0001`
- Segundo pedido del mismo día: `NOVA-20260413-0002`
- Primer pedido del 14 de abril: `NOVA-20260414-0001`

Implementar con una query atómica:
```sql
SELECT LPAD(COUNT(*) + 1, 4, '0') 
FROM orders 
WHERE DATE(created_at) = CURDATE()
FOR UPDATE;
```

---

## Historial de cambios

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | Oct 2025 | Modelo inicial |
| 2.0 | Abr 2026 | Migración a Spring Boot + JPA |
| 2.1 | Abr 2026 | Agregado import_jobs, status en products, dropi_sync en orders |

