-- =====================================================================
--  Tapizados El Miura - Datos de prueba
--  Archivo : 02_datos_prueba (Insercion de Datos Ejemplo).sql
--  Requiere: haber ejecutado 01_esquema (Estructuracion de BD).sql
--  Propósito: poblar la BD con datos coherentes para desarrollo y pruebas.
--             Los productos y trabajos salen de los prototipos de interfaz.
--  ⚠ NO usar en producción: crea un administrador con contraseña conocida.
-- =====================================================================

USE tapizados_miura;

START TRANSACTION;

-- ---------------------------------------------------------------------
-- Administrador inicial
--   correo     : admin@tapizadosmiura.com
--   contraseña : Miura2026*Cambiar   (hash bcrypt, costo 10)
--   Cambiar la contraseña en el primer inicio de sesión.
-- ---------------------------------------------------------------------
INSERT INTO administrador (nombre, correo, contrasena_hash) VALUES
('Administrador Miura', 'admin@tapizadosmiura.com',
 '$2y$10$URZ6HrdCF5wVxkjxUpTn9u4wis3IxUtT6iJbA55Xc7kO.NIMQkmPe');

-- ---------------------------------------------------------------------
-- Categorías
-- ---------------------------------------------------------------------
INSERT INTO categoria (id_categoria, nombre, descripcion) VALUES
(1, 'Sofás',     'Sofás y sofacamas para sala.'),
(2, 'Poltronas', 'Sillones individuales y reclinables.'),
(3, 'Comedores', 'Sillas y juegos de comedor.');

-- ---------------------------------------------------------------------
-- Productos (tomados del prototipo de Catálogo y Detalle)
-- ---------------------------------------------------------------------
INSERT INTO producto
  (id_producto, id_categoria, referencia, nombre, descripcion, precio, stock,
   estilo, color, material_tapiz, material_estructura, tipo_relleno, tipo_pata,
   alto_cm, ancho_cm, profundidad_cm, peso_kg, resistencia_kg_puesto, requiere_armado)
VALUES
(1, 1, '00014', 'Sofácama Cajón Soren (140 cm) Azul',
 'Sofácama de dos puestos con cajón inferior, ideal para espacios reducidos.',
 1999999.00, 4, 'Contemporáneo', 'Azul', 'Tela', 'Madera de roble',
 'Espuma industrial', 'Madera', 90, 177, 96, 62.2, 150, FALSE),
(2, 1, '00015', 'Sofá Roman 3 Puestos Beige',
 'Sofá de tres puestos con cojines removibles.',
 2399999.00, 2, 'Moderno', 'Beige', 'Lino', 'Pino',
 'Espuma de alta densidad', 'Madera', 85, 210, 92, 70.0, 150, FALSE),
(3, 1, '00016', 'Sofá Roman 2 Puestos Gris Claro',
 'Versión de dos puestos del sofá Roman.',
 1399999.00, 0, 'Moderno', 'Gris claro', 'Lino', 'Pino',
 'Espuma de alta densidad', 'Madera', 85, 160, 92, 55.0, 150, FALSE),  -- agotado
(4, 2, '00021', 'Poltrona Reclinable Azul Noche',
 'Poltrona reclinable manual.',
 1299999.00, 3, 'Clásico', 'Azul', 'Tela antifluidos', 'Madera de roble',
 'Espuma industrial', 'Oculta', 100, 85, 95, 38.0, 120, FALSE);

INSERT INTO producto_imagen (id_producto, ruta, texto_alternativo, orden) VALUES
(1, 'uploads/productos/00014-1.webp', 'Sofácama Soren azul, vista frontal', 0),
(1, 'uploads/productos/00014-2.webp', 'Sofácama Soren azul, abierto',       1),
(2, 'uploads/productos/00015-1.webp', 'Sofá Roman beige en sala',           0),
(3, 'uploads/productos/00016-1.webp', 'Sofá Roman gris claro',              0),
(4, 'uploads/productos/00021-1.webp', 'Poltrona reclinable azul',           0);

-- ---------------------------------------------------------------------
-- Servicios
-- ---------------------------------------------------------------------
INSERT INTO servicio (id_servicio, nombre, descripcion, orden) VALUES
(1, 'Tapizado',              'Cambio de tela y relleno de muebles.',               1),
(2, 'Restauración',          'Reparación de estructura, pintura y acabados.',      2),
(3, 'Fabricación a medida',  'Diseño y construcción de muebles desde cero.',       3);

-- ---------------------------------------------------------------------
-- Trabajos (prototipos de Restauraciones y Colaboraciones)
-- ---------------------------------------------------------------------
INSERT INTO trabajo
  (id_trabajo, id_servicio, tipo, titulo, descripcion, cliente, imagen_antes, imagen_despues)
VALUES
(1, 2, 'restauracion', 'Sofá capitoné amarillo',
 'Sofá de gran formato renovado con tapicería resistente a rasguños de mascotas.',
 NULL, 'uploads/trabajos/1-antes.webp', 'uploads/trabajos/1-despues.webp'),
(2, 1, 'restauracion', 'Sofá clásico en naranja',
 'Estructura recuperada y retapizada en un tono naranja moderno.',
 NULL, 'uploads/trabajos/2-antes.webp', 'uploads/trabajos/2-despues.webp'),
(3, 1, 'colaboracion', 'Zona 0: Renovación Exclusiva',
 'Renovación completa del mobiliario de la discoteca.',
 'Discoteca Zona 0', NULL, NULL),
(4, 3, 'colaboracion', 'La Gran Paila: Tradición y Confort',
 'Fabricación a medida del mobiliario del restaurante.',
 'Restaurante La Gran Paila', NULL, NULL);

INSERT INTO trabajo_imagen (id_trabajo, ruta, texto_alternativo, orden) VALUES
(3, 'uploads/trabajos/3-1.webp', 'Sala VIP de Zona 0',            0),
(3, 'uploads/trabajos/3-2.webp', 'Sofás de la barra de Zona 0',   1),
(4, 'uploads/trabajos/4-1.webp', 'Comedor de La Gran Paila',      0),
(4, 'uploads/trabajos/4-2.webp', 'Sillas tapizadas La Gran Paila', 1);

-- ---------------------------------------------------------------------
-- Información del negocio (valores de ejemplo, reemplazar por los reales)
-- ---------------------------------------------------------------------
INSERT INTO informacion_negocio (clave, etiqueta, valor, grupo) VALUES
('whatsapp',          'WhatsApp de ventas',      '573120000000',                  'contacto'),
('telefono',          'Línea de atención',       '3330000000',                    'contacto'),
('correo',            'Correo de contacto',      'contacto@tapizadosmiura.com',   'contacto'),
('direccion',         'Dirección del taller',    'Caucasia, Antioquia',           'contacto'),
('horario',           'Horario de atención',     'Lunes a sábado, 8:00 a. m. - 6:00 p. m.', 'horario'),
('cobertura',         'Cobertura geográfica',    'Bajo Cauca antioqueño y municipios cercanos.', 'cobertura'),
('trayectoria',       'Trayectoria',             'Texto de trayectoria del negocio.', 'nosotros'),
('garantia',          'Garantía',                'Texto de la política de garantía.', 'politicas'),
('instagram',         'Instagram',               'https://instagram.com/tapizadoselmiura', 'redes');

-- ---------------------------------------------------------------------
-- Cotizaciones de ejemplo (el total lo calcula v_cotizacion_resumen)
--   #1: 1 Soren + 2 Poltronas = 1.999.999 + 2.599.998 = 4.599.997
--   #2: 1 Roman 3P            = 2.399.999
-- ---------------------------------------------------------------------
INSERT INTO cotizacion (id_cotizacion, estado) VALUES
(1, 'pendiente'),
(2, 'atendida');

INSERT INTO detalle_cotizacion
  (id_cotizacion, id_producto, nombre_producto, cantidad, precio_unitario)
VALUES
(1, 1, 'Sofácama Cajón Soren (140 cm) Azul', 1, 1999999.00),
(1, 4, 'Poltrona Reclinable Azul Noche',     2, 1299999.00),
(2, 2, 'Sofá Roman 3 Puestos Beige',         1, 2399999.00);

COMMIT;
