-- =====================================================================
--  Tapizados El Miura - Plataforma web comercial
--  Archivo : 03_datos_faltantes.sql
--  Requiere: haber ejecutado 01_esquema (Estructuracion de BD).sql
--  Propósito: agrega los datos que se fueron necesitando después de crear
--             la BD. Cada bloque nuevo se agrega al final de este archivo,
--             con su fecha y su propósito.
--
--  Bloques actuales:
--    1. Redes sociales Facebook y TikTok.
--    2. Categorías Sillas mecedoras, Juegos de sala y Mesas de centro
--       (18/10/2026).
--
--  Se puede ejecutar sobre la BD actual (con datos reales): NO borra ni
--  modifica nada. Si los datos ya existen, no hace nada (es idempotente).
-- =====================================================================


-- =====================================================================
--  1. Redes sociales
--  Propósito: agrega a "informacion_negocio" las redes sociales que muestra
--             la página informacion.html y que la BD no tenía (Facebook y
--             TikTok). Después de ejecutarlo se editan desde el panel:
--             Información del negocio > Redes sociales.
--
--  Los valores son de ejemplo: reemplace "(cambiar)" por la dirección real
--  desde el panel. Deben empezar por https:// (regla de CU09).
-- =====================================================================

USE tapizados_miura;

INSERT INTO informacion_negocio (clave, etiqueta, valor, grupo)
VALUES
  ('facebook', 'Facebook', 'https://facebook.com/tapizadoselmiura(cambiar)', 'redes'),
  ('tiktok',   'TikTok',   'https://tiktok.com/@tapizadoselmiura(cambiar)',  'redes')
ON DUPLICATE KEY UPDATE clave = clave;  -- si ya existen, se conservan sus valores

-- Verificación
SELECT clave, etiqueta, valor FROM informacion_negocio WHERE grupo = 'redes';


-- =====================================================================
--  2. Categorías nuevas del catálogo (agregado el 18/10/2026)
--  Propósito: crea las categorías que necesitan los productos reales
--             cargados desde el panel: sillas mecedoras, juegos de sala
--             y mesas de centro.
--
--  También se puede ejecutar sobre la BD con datos reales: no borra ni
--  modifica nada. Si una categoría ya existe (mismo nombre), se conserva
--  tal como está.
--
--  Después de ejecutarlo:
--    1. En el panel, Gestor de catálogo > Editar, asigne a cada producto
--       su categoría nueva (la lista desplegable ya las muestra).
--    2. Las categorías aparecen en el sitio público solo si tienen al
--       menos un producto visible (GET /api/categorias trae el total).
-- =====================================================================

INSERT INTO categoria (nombre, descripcion)
VALUES
  ('Sillas mecedoras', 'Mecedoras tapizadas para sala, terraza o habitación.'),
  ('Juegos de sala',   'Conjuntos de sofá y poltronas para la sala.'),
  ('Mesas de centro',  'Mesas de centro y auxiliares para la sala.')
ON DUPLICATE KEY UPDATE nombre = nombre;  -- si ya existen, se conservan

-- Verificación
SELECT id_categoria, nombre, descripcion, estado FROM categoria ORDER BY id_categoria;
