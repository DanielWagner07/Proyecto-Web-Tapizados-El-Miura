# Pruebas de los servicios web (API)

Evidencia GA7-220501096-AA5-EV03. Este documento registra las pruebas de la API descrita
en `docs/API.md`: servicios públicos, cotización con WhatsApp, autenticación y servicios de
administración.

- **Entorno:** Tomcat 10.1 (Servlet 6) y MariaDB, cargados con el volcado actual de la
  base de datos (`SQL Actual con Datos Reales`). Es el mismo motor de XAMPP y son los
  mismos datos reales del proyecto.
- **Fecha:** 18 de octubre de 2026.
- **Cómo repetirlas:**
  - Pruebas unitarias: `mvnw.cmd test`. También se ejecutan en cada `desplegar.bat`.
  - Pruebas funcionales: importar en Postman `docs/TapizadosMiura-API.postman_collection.json`
    y ejecutar la colección completa con el Runner (Tomcat 11 y MySQL encendidos).

## Pruebas unitarias (JUnit 5)

Resultado: 44 de 44 pruebas exitosas. Son las 25 de la evidencia AA2 más 19 nuevas.

Las pruebas unitarias no usan la base de datos: así `desplegar.bat` no falla cuando MySQL
está apagado. Las reglas que dependen de la BD se prueban en la sección funcional.

`CotizacionValidadorTest`, cuerpo de `POST /api/cotizaciones` (RF03 / CU04):

- Una cotización con productos y cantidades válidas se acepta.
- Sin productos se rechaza (CU05-2a, carrito vacío), también si falta `items` o no es una lista.
- La cantidad debe ser un entero entre 1 y 10: se rechazan 0, 11, 2.5 y la cantidad ausente.
- El id del producto debe ser numérico y positivo.
- Un número enviado como texto (`"3"`) se acepta, porque así llega el valor de un input.
- Un producto repetido suma sus cantidades, y se rechaza si la suma pasa de 10.
- El precio enviado por el navegador se ignora.
- Se rechazan más de 20 productos distintos.

`MensajeWhatsAppTest`, mensaje y enlace de WhatsApp (RF04 / RNF08):

- Los precios se escriben en pesos con punto de miles y sin centavos (`$1.999.999`).
- El mensaje lista cada producto con su subtotal, el precio unitario cuando la cantidad es
  mayor que 1, y el total.
- Los muebles agotados salen como "(agotado, sujeto a fabricación)". Si se piden más
  unidades que el stock salen como "(2 disponibles; el resto, sujeto a fabricación)". Si hay
  stock suficiente no se agrega ninguna nota.
- El enlace codifica los espacios como `%20` y no como `+`, conserva el signo `+` escrito
  (`%2B`) y los saltos de línea (`%0A`), y al decodificarlo se recupera el texto original.
- El número del negocio debe tener solo dígitos con indicativo.

`CamposJsonTest`, conversión del JSON al formato de los validadores del panel:

- Textos y números se convierten a texto; `true` queda como casilla marcada.
- `false` y `null` se omiten, igual que una casilla sin marcar o un campo vacío.
- Un campo desconocido o con tipo de dato incorrecto responde 400 con el detalle de cada campo.
- El booleano obligatorio exige `true` o `false`; el texto `"true"` se rechaza.
- El texto opcional se recorta y admite que no se envíe.
- Las rutas de imagen se convierten en direcciones dentro de la aplicación, o en la imagen
  genérica si no hay foto.

## Pruebas funcionales con Postman

La colección tiene 59 peticiones en 8 carpetas, y cada una trae pruebas automáticas
(código HTTP, formato JSON y contenido de la respuesta).

- **Resultado:** 59 peticiones, 156 aserciones y 0 fallas.
- **Repetición:** la colección se ejecutó tres veces seguidas sobre la misma base de datos,
  con el mismo resultado.
- **Datos que deja:** quedan como estaban, salvo dos cosas:
  - El producto oculto de prueba `API-PRUEBA`, que se crea en la primera ejecución y se
    reutiliza en las siguientes.
  - Una cotización de prueba por ejecución, que la misma colección marca como `descartada`.

### 1. Servicios públicos

- Categorías: lista con `totalProductos` (Comedores 0, Poltronas 1, Sofás 3). Resultado: 200.
- Servicios: los 3 servicios activos en su orden. Resultado: 200.
- Información del negocio: objeto por clave con un WhatsApp de 10 a 15 dígitos. Resultado: 200.
- Catálogo completo:
  - Trae 4 productos; el producto 5 (oculto) no aparece.
  - Cada producto trae imagen y ficha técnica.
  - Resultado: 200.
- Catálogo filtrado por categoría 1: todos los productos son de Sofás. Resultado: 200.
- Búsqueda `q=roman`: todos los resultados contienen "roman". Resultado: 200.
- Categoría no numérica: detalle en el campo `categoria`. Resultado: 400.
- Detalle del producto 1: trae ficha y carrusel, y no expone datos de administración.
  Resultado: 200.
- Producto 3 (stock 0): marcado `agotado: true` (RF02). Resultado: 200.
- Producto 5 (oculto): Resultado: 404.
- Id no numérico (`/api/productos/abc`): Resultado: 400.
- Galería completa: el trabajo 5 (oculto) no aparece. Resultado: 200.
- Restauraciones: todas traen foto del antes y del después (RF08). Resultado: 200.
- Colaboraciones: todas traen su lista de fotos. Resultado: 200.
- Tipo de trabajo inválido: Resultado: 400.
- Detalle del trabajo 1: Resultado: 200. Trabajo inexistente: Resultado: 404.
- Ruta `/api/noexiste`: 404 en JSON (no la página HTML de Tomcat).

### 2. Cotización y WhatsApp

- Cotización con los productos 1, 4 y 3 (el 3 agotado), enviando un precio falso de 1 para
  el producto 3:
  - Se registró y respondió 201.
  - Se usó el precio de la BD ($1.399.999), no el enviado.
  - El total es la suma de los subtotales.
  - El producto 3 quedó marcado como agotado.
  - El enlace es `https://wa.me/573128628147?text=...` y no contiene `+`.
  - En la BD quedaron la cotización (estado `pendiente`) y sus 3 líneas de detalle; la vista
    `v_cotizacion_resumen` muestra el mismo total.
- Carrito vacío: Resultado: 400 con error en `items`.
- Cantidades 0 y 2.5: Resultado: 400 con error en `items[0].cantidad` e `items[1].cantidad`.
- Producto oculto (5) e inexistente (99999): la cotización no se guarda e indica los dos
  productos a retirar. Resultado: 400.
- Cuerpo sin `Content-Type: application/json`: Resultado: 415.
- JSON mal escrito: Resultado: 400.
- `GET /api/cotizaciones`: Resultado: 405.

### 3. Autenticación

- `/api/admin/productos` sin sesión: Resultado: 401 en JSON.
- Consultar sesión sin iniciar: `autenticado: false`. Resultado: 200.
- Login con campos vacíos: Resultado: 400 con error en `correo` y `contrasena`.
- Login con contraseña incorrecta: "Correo o contraseña incorrectos.". Resultado: 401.
- Login correcto: `autenticado: true` y la respuesta no incluye la contraseña ni su hash.
  Resultado: 200.
- Consultar sesión iniciada: `autenticado: true` con el nombre del administrador. Resultado: 200.

### 4. Administración de productos

- Listar todos: incluye el producto oculto 5. Resultado: 200.
- Detalle del producto oculto 5: `estado: false`. Resultado: 200.
- Crear `API-PRUEBA` enviando `estado: true`: quedó oculto y sin foto, y el mensaje avisa
  que se ignoró el estado. Resultado: 201 (409 en las ejecuciones siguientes, que la colección
  acepta).
- Referencia repetida: Resultado: 409.
- Datos inválidos (referencia vacía, precio −1, stock −2, categoría 999): detalle por campo.
  Resultado: 400.
- Publicar el producto sin foto: Resultado: 409.
- Actualizar el producto (precio 360000): Resultado: 200 con el precio nuevo.
- Ocultar el producto 2 y consultarlo en el sitio público: 200 y luego 404. Al volver a
  mostrarlo: 200 con `estado: true`.
- Producto inexistente: Resultado: 404.

### 5. Administración de trabajos

- Listar todos: incluye el trabajo oculto 5. Resultado: 200.
- Detalle del trabajo 3: Resultado: 200.
- Ocultar y volver a mostrar el trabajo 1: Resultado: 200 y 200.
- Estado `"visible"` (texto en lugar de booleano): Resultado: 400.
- Eliminar un trabajo inexistente: Resultado: 404.
- Eliminar un trabajo real: se probó aparte con un trabajo creado solo para la prueba, para
  no borrar datos reales. Se borraron el registro, su foto en `trabajo_imagen` y el archivo
  en `miura-uploads`; al repetir la petición respondió 404.

### 6. Administración de la información del negocio

- Consultar: Resultado: 200.
- Actualizar con WhatsApp con letras, correo sin arroba, Instagram sin `https://` y una
  clave inexistente: detalle de los cuatro errores y ningún dato guardado. Resultado: 400.
- Actualizar el horario con su mismo valor: Resultado: 200.
- Script `03_datos_faltantes (Redes sociales).sql`:
  - Agregó `facebook` y `tiktok`, que aparecen en `GET /api/informacion` y en el formulario
    del panel.
  - Ejecutarlo dos veces no duplica ni cambia los datos.

### 7. Administración de cotizaciones

- Listar: Resultado: 200. Filtrar `estado=pendiente`: todas pendientes. Resultado: 200.
- Detalle de la cotización creada en la carpeta 2: trae sus 3 productos. Resultado: 200.
- Cambiar a `descartada` con nota interna: Resultado: 200.
- Cambiar sin enviar la nota: se conserva la nota anterior.
- Estado `"vendida"`: Resultado: 400.

### 8. Cierre de sesión

- Logout: Resultado: 200.
- `/api/admin/cotizaciones` después del logout: Resultado: 401.

## Prueba de regresión del panel

Se revisó que el panel de la evidencia AA2 siga funcionando con los cambios:

- Login del panel y listado de productos con los datos reales: correctos.
- El producto creado por la API aparece en el panel con las etiquetas "Oculto" y "Sin foto".
- Pulsar "Mostrar" en ese producto lo deja oculto, con el aviso "no tiene foto. Edítelo y
  súbale una antes de mostrarlo".
- Editarlo con "Mostrar en el catálogo" marcado y sin subir foto muestra el error "Suba una
  foto para mostrar el producto en el catálogo." El campo de imagen se rotula "Imagen
  (necesaria para mostrarlo en el catálogo)".
- La sesión es compartida: iniciar sesión en la API da acceso al panel, y cerrarla en la
  API cierra el panel.

## Actualización: colección independiente de los datos

Después de cargar los productos reales (volcado del 18 de octubre), los productos de
prueba 1 a 4 quedaron ocultos y varias pruebas de la colección fallaban, porque usaban ids
fijos (el producto 1 visible, el 3 agotado, el 5 oculto).

La colección se ajustó para tomar los ids de las primeras respuestas de la API:

- El primer y el segundo producto del catálogo, su categoría y el primer trabajo visible.
- El caso del producto oculto se prueba ocultando un producto visible y consultándolo en el
  sitio público (404). Al final se vuelve a mostrar.
- La marca de agotado se valida contra el stock (`agotado` es verdadero solo con stock 0),
  en lugar de depender de un producto agotado fijo.

**Resultado:** 59 peticiones, 161 aserciones y 0 fallas, tanto con el volcado del 17 como
con el del 18 de octubre. Solo requiere que haya al menos 2 productos y 1 trabajo visibles.
