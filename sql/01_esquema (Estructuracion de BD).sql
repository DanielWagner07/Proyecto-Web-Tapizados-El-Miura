-- =====================================================================
--  Tapizados El Miura - Plataforma web comercial
--  Archivo : 01_esquema (Estructuracion de BD).sql
--  Motor   : MySQL 8.0.16 o superior, o MariaDB 10.4 o superior (XAMPP).
--            Requiere CHECK y columnas generadas.
--  Propósito: crea la base de datos completa (tablas, restricciones,
--             índices y vistas). Es idempotente: borra y recrea la BD,
--             por lo que NO debe ejecutarse sobre una base en producción.
--
--  Orden de ejecución:
--    1. 01_esquema (Estructuracion de BD).sql              -> estructura
--    2. 02_datos_prueba (Insercion de Datos Ejemplo).sql   -> datos de ejemplo (solo desarrollo)
--    3. 03_datos_faltantes.sql                             -> datos agregados después
--
--  Convenciones:
--    - Nombres en snake_case, singular y sin tildes.
--    - Toda tabla tiene PK id_<tabla> INT UNSIGNED AUTO_INCREMENT.
--    - "estado" BOOLEAN = visible/activo en la web pública. Los registros
--      no se borran físicamente si tienen historial (borrado lógico).
--    - creado_en / actualizado_en los mantiene MySQL automáticamente.
--    - Dinero en DECIMAL (nunca FLOAT) para evitar errores de redondeo.
-- =====================================================================

DROP DATABASE IF EXISTS tapizados_miura;
CREATE DATABASE tapizados_miura
  CHARACTER SET utf8mb4          -- soporta tildes, ñ y emojis
  COLLATE utf8mb4_general_ci;    -- búsquedas sin distinguir mayúsculas; existe en MySQL y en MariaDB
USE tapizados_miura;


-- ---------------------------------------------------------------------
-- 1. ADMINISTRADOR
--    Usuarios autorizados para entrar al panel (RF06 / CU01).
--    Los clientes NO se registran; por eso no existe tabla "cliente".
-- ---------------------------------------------------------------------
CREATE TABLE administrador (
  id_administrador INT UNSIGNED  NOT NULL AUTO_INCREMENT,
  nombre           VARCHAR(100)  NOT NULL,
  correo           VARCHAR(150)  NOT NULL,
  -- Hash bcrypt (60 caracteres). Nunca se guarda la contraseña en texto plano.
  -- Se deja en 255 por si en el futuro se migra a Argon2.
  contrasena_hash  VARCHAR(255)  NOT NULL,
  estado           BOOLEAN       NOT NULL DEFAULT TRUE,
  ultimo_acceso    DATETIME      NULL,
  creado_en        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT pk_administrador PRIMARY KEY (id_administrador),
  CONSTRAINT uq_administrador_correo UNIQUE (correo),
  CONSTRAINT ck_administrador_correo CHECK (correo LIKE '%_@_%._%')
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 2. CATEGORIA
--    Agrupa los productos del catálogo (Salas, Sofás, Comedores...).
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
  id_categoria   INT UNSIGNED  NOT NULL AUTO_INCREMENT,
  nombre         VARCHAR(100)  NOT NULL,
  descripcion    TEXT          NULL,
  estado         BOOLEAN       NOT NULL DEFAULT TRUE,
  creado_en      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT pk_categoria PRIMARY KEY (id_categoria),
  CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 3. PRODUCTO
--    Mueble del catálogo. Las columnas de ficha técnica salen del
--    prototipo "Detalle de producto"; las de material/color/pata/relleno
--    alimentan los filtros del prototipo "Catálogo".
--    Las fotos van en producto_imagen (el detalle tiene carrusel).
-- ---------------------------------------------------------------------
CREATE TABLE producto (
  id_producto           INT UNSIGNED  NOT NULL AUTO_INCREMENT,
  id_categoria          INT UNSIGNED  NOT NULL,
  referencia            VARCHAR(20)   NOT NULL,   -- "REF: 00014" del prototipo
  nombre                VARCHAR(150)  NOT NULL,
  descripcion           TEXT          NULL,
  precio                DECIMAL(12,2) NOT NULL,   -- pesos colombianos
  stock                 INT UNSIGNED  NOT NULL DEFAULT 0,
  -- Ficha técnica (todas opcionales: no todo mueble aplica)
  estilo                VARCHAR(60)   NULL,
  color                 VARCHAR(60)   NULL,
  material_tapiz        VARCHAR(80)   NULL,       -- filtro "Tela"
  material_estructura   VARCHAR(80)   NULL,       -- filtro "Madera"
  tipo_relleno          VARCHAR(80)   NULL,       -- filtro "Espuma"
  tipo_pata             VARCHAR(80)   NULL,       -- filtro "Pata"
  alto_cm               DECIMAL(6,1)  NULL,       -- filtro "Tamaño" (derivado)
  ancho_cm              DECIMAL(6,1)  NULL,
  profundidad_cm        DECIMAL(6,1)  NULL,
  peso_kg               DECIMAL(6,1)  NULL,
  resistencia_kg_puesto SMALLINT UNSIGNED NULL,   -- "150 kg por puesto"
  requiere_armado       BOOLEAN       NOT NULL DEFAULT FALSE,
  estado                BOOLEAN       NOT NULL DEFAULT TRUE,
  creado_en             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                                      ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT pk_producto PRIMARY KEY (id_producto),
  CONSTRAINT uq_producto_referencia UNIQUE (referencia),
  -- RESTRICT: no se puede borrar una categoría que aún tiene productos
  -- (política de integridad del Modelo Conceptual).
  CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria)
    REFERENCES categoria (id_categoria)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT ck_producto_precio      CHECK (precio > 0),          -- regla RF07
  CONSTRAINT ck_producto_dimensiones CHECK (
        (alto_cm        IS NULL OR alto_cm        > 0)
    AND (ancho_cm       IS NULL OR ancho_cm       > 0)
    AND (profundidad_cm IS NULL OR profundidad_cm > 0)
    AND (peso_kg        IS NULL OR peso_kg        > 0))
) ENGINE = InnoDB;

-- El catálogo público siempre filtra por categoría y estado (RF01).
CREATE INDEX ix_producto_categoria_estado ON producto (id_categoria, estado);


-- ---------------------------------------------------------------------
-- 4. PRODUCTO_IMAGEN
--    Fotos de cada producto. La de menor "orden" es la imagen principal
--    que se muestra en el catálogo; el resto forman el carrusel.
--    Se guarda la RUTA del archivo, no el archivo (las imágenes viven en
--    el servidor web, ver Diagrama de Despliegue).
-- ---------------------------------------------------------------------
CREATE TABLE producto_imagen (
  id_producto_imagen INT UNSIGNED     NOT NULL AUTO_INCREMENT,
  id_producto        INT UNSIGNED     NOT NULL,
  ruta               VARCHAR(255)     NOT NULL,
  texto_alternativo  VARCHAR(150)     NULL,      -- accesibilidad (atributo alt)
  orden              TINYINT UNSIGNED NOT NULL DEFAULT 0,
  creado_en          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_producto_imagen PRIMARY KEY (id_producto_imagen),
  CONSTRAINT uq_producto_imagen_orden UNIQUE (id_producto, orden),
  -- CASCADE: si se elimina el producto, sus fotos no tienen sentido.
  CONSTRAINT fk_producto_imagen_producto FOREIGN KEY (id_producto)
    REFERENCES producto (id_producto)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT ck_producto_imagen_ruta CHECK (
    LOWER(ruta) REGEXP '\\.(jpg|jpeg|png|webp)$')  -- formatos permitidos (CU07)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 5. SERVICIO
--    Servicios ofrecidos (tapizado, restauración, fabricación a medida).
-- ---------------------------------------------------------------------
CREATE TABLE servicio (
  id_servicio    INT UNSIGNED     NOT NULL AUTO_INCREMENT,
  nombre         VARCHAR(150)     NOT NULL,
  descripcion    TEXT             NULL,
  orden          TINYINT UNSIGNED NOT NULL DEFAULT 0,   -- orden en la web
  estado         BOOLEAN          NOT NULL DEFAULT TRUE,
  creado_en      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT pk_servicio PRIMARY KEY (id_servicio),
  CONSTRAINT uq_servicio_nombre UNIQUE (nombre)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 6. TRABAJO
--    Portafolio. Una sola tabla para las dos galerías del panel:
--      - 'restauracion': comparativo antes/después (ambas fotos obligatorias,
--        regla de RF08 / CU08, garantizada aquí con un CHECK).
--      - 'colaboracion': proyecto con otro negocio (ej. Discoteca Zona 0),
--        con varias fotos en trabajo_imagen; antes/después opcionales.
-- ---------------------------------------------------------------------
CREATE TABLE trabajo (
  id_trabajo     INT UNSIGNED  NOT NULL AUTO_INCREMENT,
  id_servicio    INT UNSIGNED  NULL,    -- servicio que evidencia (opcional)
  tipo           ENUM('restauracion','colaboracion') NOT NULL,
  titulo         VARCHAR(150)  NOT NULL,
  descripcion    TEXT          NULL,
  cliente        VARCHAR(150)  NULL,    -- negocio aliado en colaboraciones
  imagen_antes   VARCHAR(255)  NULL,
  imagen_despues VARCHAR(255)  NULL,
  estado         BOOLEAN       NOT NULL DEFAULT TRUE,
  creado_en      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT pk_trabajo PRIMARY KEY (id_trabajo),
  -- SET NULL: si se elimina un servicio, el trabajo sigue en el portafolio.
  CONSTRAINT fk_trabajo_servicio FOREIGN KEY (id_servicio)
    REFERENCES servicio (id_servicio)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT ck_trabajo_restauracion_imagenes CHECK (
    tipo <> 'restauracion'
    OR (imagen_antes IS NOT NULL AND imagen_despues IS NOT NULL))
) ENGINE = InnoDB;

CREATE INDEX ix_trabajo_tipo_estado ON trabajo (tipo, estado);


-- ---------------------------------------------------------------------
-- 7. TRABAJO_IMAGEN
--    Fotos adicionales de un trabajo (el prototipo de Colaboraciones
--    muestra 4 fotos por proyecto).
-- ---------------------------------------------------------------------
CREATE TABLE trabajo_imagen (
  id_trabajo_imagen INT UNSIGNED     NOT NULL AUTO_INCREMENT,
  id_trabajo        INT UNSIGNED     NOT NULL,
  ruta              VARCHAR(255)     NOT NULL,
  texto_alternativo VARCHAR(150)     NULL,
  orden             TINYINT UNSIGNED NOT NULL DEFAULT 0,
  creado_en         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_trabajo_imagen PRIMARY KEY (id_trabajo_imagen),
  CONSTRAINT uq_trabajo_imagen_orden UNIQUE (id_trabajo, orden),
  CONSTRAINT fk_trabajo_imagen_trabajo FOREIGN KEY (id_trabajo)
    REFERENCES trabajo (id_trabajo)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT ck_trabajo_imagen_ruta CHECK (
    LOWER(ruta) REGEXP '\\.(jpg|jpeg|png|webp)$')
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 8. COTIZACION
--    Se registra cuando el cliente pulsa "Enviar por WhatsApp" (RF04).
--    Mientras el cliente arma la cotización, esta vive en su navegador;
--    así la BD no se llena de carritos abandonados.
--    El TOTAL no se guarda: se calcula en la vista v_cotizacion_resumen
--    a partir del detalle, de modo que nunca puede quedar desfasado.
-- ---------------------------------------------------------------------
CREATE TABLE cotizacion (
  id_cotizacion  INT UNSIGNED  NOT NULL AUTO_INCREMENT,
  estado         ENUM('pendiente','atendida','cerrada','descartada')
                               NOT NULL DEFAULT 'pendiente',
  nota_interna   TEXT          NULL,   -- uso exclusivo del administrador
  creado_en      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT pk_cotizacion PRIMARY KEY (id_cotizacion)
) ENGINE = InnoDB;

CREATE INDEX ix_cotizacion_estado_fecha ON cotizacion (estado, creado_en);


-- ---------------------------------------------------------------------
-- 9. DETALLE_COTIZACION
--    Cada producto incluido en una cotización.
--    - precio_unitario y nombre_producto son una COPIA del momento del
--      envío: si el producto cambia de precio después, la cotización
--      histórica sigue mostrando lo que el cliente vio.
--    - subtotal es una columna GENERADA: MySQL la calcula sola
--      (cantidad x precio_unitario) y no se puede escribir a mano.
-- ---------------------------------------------------------------------
CREATE TABLE detalle_cotizacion (
  id_detalle_cotizacion INT UNSIGNED      NOT NULL AUTO_INCREMENT,
  id_cotizacion         INT UNSIGNED      NOT NULL,
  id_producto           INT UNSIGNED      NOT NULL,
  nombre_producto       VARCHAR(150)      NOT NULL,
  cantidad              SMALLINT UNSIGNED NOT NULL,
  precio_unitario       DECIMAL(12,2)     NOT NULL,
  subtotal              DECIMAL(14,2)
                        AS (cantidad * precio_unitario) STORED,
  CONSTRAINT pk_detalle_cotizacion PRIMARY KEY (id_detalle_cotizacion),
  -- Un producto aparece una sola vez por cotización; se suma la cantidad.
  CONSTRAINT uq_detalle_cotizacion_producto UNIQUE (id_cotizacion, id_producto),
  CONSTRAINT fk_detalle_cotizacion_cotizacion FOREIGN KEY (id_cotizacion)
    REFERENCES cotizacion (id_cotizacion)
    ON UPDATE CASCADE ON DELETE CASCADE,
  -- RESTRICT: un producto ya cotizado no se borra; se desactiva (estado).
  CONSTRAINT fk_detalle_cotizacion_producto FOREIGN KEY (id_producto)
    REFERENCES producto (id_producto)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT ck_detalle_cotizacion_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_detalle_cotizacion_precio   CHECK (precio_unitario > 0)
) ENGINE = InnoDB;


-- ---------------------------------------------------------------------
-- 10. INFORMACION_NEGOCIO
--    Datos editables desde el "Gestor de Información" (RF09 / CU09):
--    teléfonos, WhatsApp, horarios, cobertura, trayectoria, garantía...
--    Se usa clave/valor para poder agregar datos nuevos sin alterar la
--    estructura de la tabla.
-- ---------------------------------------------------------------------
CREATE TABLE informacion_negocio (
  id_informacion INT UNSIGNED NOT NULL AUTO_INCREMENT,
  clave          VARCHAR(60)  NOT NULL,   -- identificador usado por el código
  etiqueta       VARCHAR(100) NOT NULL,   -- nombre legible en el panel
  valor          TEXT         NOT NULL,
  grupo          ENUM('contacto','horario','cobertura','nosotros','politicas','redes')
                              NOT NULL,
  actualizado_en DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT pk_informacion_negocio PRIMARY KEY (id_informacion),
  CONSTRAINT uq_informacion_negocio_clave UNIQUE (clave),
  CONSTRAINT ck_informacion_negocio_clave CHECK (clave REGEXP '^[a-z][a-z0-9_]*$'),
  CONSTRAINT ck_informacion_negocio_valor CHECK (CHAR_LENGTH(valor) <= 2000) -- CU09 2a
) ENGINE = InnoDB;


-- =====================================================================
--  VISTAS
--  Encapsulan las consultas más usadas para que el backend no repita
--  la lógica de "qué es visible" en varios lugares.
-- =====================================================================

-- Catálogo público (RF01): solo productos activos de categorías activas,
-- con su imagen principal y bandera de agotado (RF02 / CU03 2a).
CREATE VIEW v_catalogo_publico AS
SELECT p.id_producto,
       p.referencia,
       p.nombre,
       p.precio,
       p.stock,
       (p.stock = 0)  AS agotado,
       c.id_categoria,
       c.nombre       AS categoria,
       (SELECT pi.ruta
          FROM producto_imagen pi
         WHERE pi.id_producto = p.id_producto
         ORDER BY pi.orden
         LIMIT 1)     AS imagen_principal
  FROM producto  p
  JOIN categoria c ON c.id_categoria = p.id_categoria
 WHERE p.estado = TRUE
   AND c.estado = TRUE;

-- Resumen de cotizaciones para el panel: total calculado del detalle.
CREATE VIEW v_cotizacion_resumen AS
SELECT co.id_cotizacion,
       co.estado,
       co.creado_en,
       COUNT(d.id_detalle_cotizacion)  AS total_items,
       COALESCE(SUM(d.cantidad), 0)    AS total_unidades,
       COALESCE(SUM(d.subtotal), 0)    AS total
  FROM cotizacion co
  LEFT JOIN detalle_cotizacion d ON d.id_cotizacion = co.id_cotizacion
 GROUP BY co.id_cotizacion, co.estado, co.creado_en;
