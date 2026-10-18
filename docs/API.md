# API de Tapizados El Miura

Evidencia GA7-220501096-AA5-EV03, Diseño y desarrollo de servicios web.

Este documento describe los servicios web (API REST en JSON) de la plataforma. Los
consume el sitio público con `fetch()` y también se pueden probar con Postman usando la
colección `docs/TapizadosMiura-API.postman_collection.json`.

## Contenido

- [Conceptos generales](#conceptos-generales)
- Servicios públicos
  - [GET /api/categorias](#get-apicategorias)
  - [GET /api/servicios](#get-apiservicios)
  - [GET /api/informacion](#get-apiinformacion)
  - [GET /api/productos](#get-apiproductos)
  - [GET /api/productos/{id}](#get-apiproductosid)
  - [GET /api/trabajos](#get-apitrabajos)
  - [GET /api/trabajos/{id}](#get-apitrabajosid)
  - [POST /api/cotizaciones](#post-apicotizaciones)
- Autenticación
  - [POST /api/auth/login](#post-apiauthlogin)
  - [POST /api/auth/logout](#post-apiauthlogout)
  - [GET /api/auth/sesion](#get-apiauthsesion)
- Administración (requieren sesión)
  - [Productos](#administración-productos)
  - [Trabajos](#administración-trabajos)
  - [Información del negocio](#administración-información-del-negocio)
  - [Cotizaciones](#administración-cotizaciones)
- [Código fuente](#código-fuente)
- [Limitaciones conocidas](#limitaciones-conocidas)

---

## Conceptos generales

### Dirección base

```
http://localhost:8081/tapizados-miura
```

Todas las rutas de este documento se escriben a partir de esa dirección, por ejemplo
`GET /api/productos` corresponde a `http://localhost:8081/tapizados-miura/api/productos`.

Las páginas del sitio deben abrirse desde el mismo Tomcat (`localhost:8081`). Si se abren
con otra herramienta (por ejemplo Live Server en el puerto 5500), el navegador bloquea las
peticiones a la API por venir de otro origen.

### Formato de las respuestas

- Todas las respuestas son JSON en UTF-8, también los errores.
- **Consultas (GET):** devuelven el dato directamente, un objeto o una lista.
- **Operaciones que modifican datos (POST, PUT, DELETE):** devuelven un mensaje para el
  usuario y, si aplica, el recurso resultante:

  ```json
  {
    "mensaje": "Producto \"Silla de comedor\" actualizado.",
    "datos": { "idProducto": 6, "...": "..." }
  }
  ```

- Los campos sin valor se omiten. Por ejemplo, un producto sin color no trae `"color"`.
- Los precios son números con dos decimales (`1999999.00`), en pesos colombianos.
- Las fechas son texto ISO-8601: `"2026-10-18T15:02:13"`.
- Las imágenes se entregan como dirección lista para usar en `<img src>`, por ejemplo
  `/tapizados-miura/uploads/productos/00014-1.webp`. Si un registro no tiene foto se entrega
  `/tapizados-miura/img/sin-imagen.svg`.

### Formato de los errores

Siempre el mismo:

```json
{
  "mensaje": "Hay campos con errores.",
  "errores": {
    "precio": "El precio debe ser mayor que cero.",
    "stock": "El stock debe ser un número entero mayor o igual a cero."
  }
}
```

- `mensaje`: explicación general para mostrar al usuario.
- `errores`: detalle por campo. Solo aparece cuando hay campos concretos que corregir.

### Códigos HTTP

- **200 OK:** consulta o modificación exitosa.
- **201 Created:** se creó un registro (cotización o producto).
- **400 Bad Request:** datos inválidos, JSON mal escrito o parámetro con formato incorrecto.
- **401 Unauthorized:** falta iniciar sesión o las credenciales no son válidas.
- **404 Not Found:** el recurso no existe, no está publicado o la ruta no existe.
- **405 Method Not Allowed:** la ruta existe pero no admite ese método.
- **409 Conflict:** la operación choca con una regla del negocio (referencia repetida o
  producto sin foto que se intenta publicar).
- **415 Unsupported Media Type:** el cuerpo no se envió como JSON.
- **500 Internal Server Error:** error del servidor, por ejemplo la base de datos apagada.

### Cuerpo de las peticiones

- Los POST y PUT que llevan cuerpo deben enviar la cabecera `Content-Type: application/json`;
  si no, la respuesta es 415.
- Este requisito también es una protección CSRF: un formulario HTML de otra página no puede
  enviar JSON, así que no puede disparar acciones del administrador.

### Sesión del administrador

- `POST /api/auth/login` crea la sesión y el servidor responde con la cookie `JSESSIONID`.
- El navegador y Postman la envían solos en las siguientes peticiones.
- La sesión es la misma del panel administrador (`/admin/...`): iniciar sesión en uno da
  acceso al otro, y cerrar sesión en uno cierra ambos.
- La sesión vence a los 30 minutos sin uso. También se pierde cada vez que se vuelve a
  desplegar la aplicación con `desplegar.bat`.
- Las rutas `/api/admin/*` responden **401** si no hay sesión.

### Regla de visibilidad

El sitio público solo muestra registros visibles:

- **Productos:** solo los activos cuya categoría también esté activa, igual que la vista
  `v_catalogo_publico`.
- **Trabajos:** solo los activos.
- Un registro oculto responde 404 en los servicios públicos, pero el administrador sí lo ve
  en `/api/admin/...`.

---

## Servicios públicos

No requieren sesión.

### GET /api/categorias

Categorías visibles del catálogo con la cantidad de productos visibles de cada una.

- **Requisito:** RF01 / CU02.
- **Uso en el sitio:** menú de categorías. Con `totalProductos = 0` el sitio muestra
  "No hay productos disponibles por el momento" (CU02-2a).
- **Parámetros:** ninguno.
- **Respuesta 200:** lista ordenada por nombre.

```json
[
  {
    "idCategoria": 1,
    "nombre": "Sofás",
    "descripcion": "Sofás y sofacamas para sala.",
    "totalProductos": 3
  }
]
```

### GET /api/servicios

Servicios activos del taller, en el orden en que se muestran en la web.

- **Requisito:** RF05.
- **Parámetros:** ninguno.
- **Respuesta 200:**

```json
[
  {
    "idServicio": 1,
    "nombre": "Tapizado",
    "descripcion": "Cambio de tela y relleno de muebles."
  }
]
```

### GET /api/informacion

Datos del negocio editables desde el panel: contacto, horario, cobertura, trayectoria,
políticas y redes sociales.

- **Requisito:** CU09 / RF09 (la vista del cliente se actualiza al instante).
- **Uso en el sitio:** página "Información", pie de página y número de WhatsApp.
- **Parámetros:** ninguno.
- **Respuesta 200:** un objeto indexado por clave, así el sitio lee directamente
  `info.whatsapp.valor`.

```json
{
  "whatsapp": {
    "etiqueta": "WhatsApp de ventas",
    "valor": "573128628147",
    "grupo": "contacto"
  },
  "horario": {
    "etiqueta": "Horario de atención",
    "valor": "Lunes a sábado, 8:00 a. m. - 6:00 p. m.",
    "grupo": "horario"
  }
}
```

- **Claves actuales:**
  - `whatsapp`, `telefono`, `correo` y `direccion` (grupo `contacto`).
  - `horario` (grupo `horario`) y `cobertura` (grupo `cobertura`).
  - `trayectoria` (grupo `nosotros`) y `garantia` (grupo `politicas`).
  - `instagram`, `facebook` y `tiktok` (grupo `redes`). Facebook y TikTok se agregan con el
    script `03_datos_faltantes (Redes sociales).sql`.

### GET /api/productos

Catálogo público de muebles.

- **Requisito:** RF01 / CU02.
- **Parámetros opcionales:**
  - `categoria`: id de la categoría, por ejemplo `?categoria=1`.
  - `q`: texto a buscar en nombre, referencia o descripción (máximo 100 caracteres), por
    ejemplo `?q=roman`.
  - Se pueden combinar: `?categoria=1&q=roman`.
- **Respuesta 200:** lista ordenada por nombre. Cada producto trae su ficha técnica, para
  que el sitio arme los filtros (color, tela, relleno, madera) con los valores reales.

```json
[
  {
    "idProducto": 3,
    "referencia": "00016",
    "nombre": "Sofá Roman 2 Puestos Gris Claro",
    "descripcion": "Versión de dos puestos del sofá Roman.",
    "precio": 1399999.00,
    "stock": 0,
    "agotado": true,
    "idCategoria": 1,
    "categoria": "Sofás",
    "imagen": "/tapizados-miura/uploads/productos/00016-1.webp",
    "ficha": {
      "estilo": "Moderno",
      "color": "Gris claro",
      "materialTapiz": "Lino",
      "materialEstructura": "Pino",
      "tipoRelleno": "Espuma de alta densidad",
      "tipoPata": "Madera",
      "altoCm": 85.0,
      "anchoCm": 160.0,
      "profundidadCm": 92.0,
      "pesoKg": 55.0,
      "resistenciaKgPuesto": 150,
      "requiereArmado": false
    }
  }
]
```

- **Campos principales:**
  - `agotado`: `true` si el stock es 0. El sitio lo indica visualmente (RF02).
  - `imagen`: foto principal.
  - `ficha.materialTapiz`: tela. `ficha.materialEstructura`: madera o estructura.
    `ficha.tipoRelleno`: espuma.
- **Errores:**
  - 400 si `categoria` no es un número positivo o `q` supera 100 caracteres.
  - Una categoría sin productos visibles responde 200 con una lista vacía.

### GET /api/productos/{id}

Ficha completa de un producto con todas sus fotos.

- **Requisito:** RF02 / CU03.
- **Uso en el sitio:** página de detalle del producto.
- **Respuesta 200:** los mismos campos del catálogo más `imagenes`, que forman el carrusel
  (la primera es la principal).

```json
{
  "idProducto": 1,
  "referencia": "00014",
  "nombre": "Sofácama Cajón Soren (140 cm) Azul",
  "precio": 1999999.00,
  "stock": 4,
  "agotado": false,
  "idCategoria": 1,
  "categoria": "Sofás",
  "imagen": "/tapizados-miura/uploads/productos/00014-1.webp",
  "ficha": { "color": "Azul", "materialTapiz": "Tela", "altoCm": 90.0, "requiereArmado": false },
  "imagenes": [
    { "url": "/tapizados-miura/uploads/productos/00014-1.webp", "textoAlternativo": "Sofácama Soren azul, vista frontal" },
    { "url": "/tapizados-miura/uploads/productos/00014-2.webp", "textoAlternativo": "Sofácama Soren azul, abierto" }
  ]
}
```

- **Errores:**
  - 400 si el id no es numérico.
  - 404 si el producto no existe, está oculto o su categoría está oculta.

### GET /api/trabajos

Galería de trabajos realizados.

- **Requisito:** RF05 / CU06.
- **Parámetro opcional `tipo`:**
  - `restauracion`: comparativos de antes y después, para la página "Tapizado y restauración".
  - `colaboracion`: proyectos para otros negocios con varias fotos, para la página "Renovaciones".
  - Sin `tipo` se devuelven ambos.
- **Respuesta 200:** trabajos visibles, los más recientes primero, cada uno con sus fotos.

```json
[
  {
    "idTrabajo": 1,
    "tipo": "restauracion",
    "tipoLegible": "Restauración",
    "titulo": "Sofá capitoné amarillo",
    "descripcion": "Sofá de gran formato renovado con tapicería resistente a rasguños de mascotas.",
    "idServicio": 2,
    "servicio": "Restauración",
    "imagenAntes": "/tapizados-miura/uploads/trabajos/1-antes.webp",
    "imagenDespues": "/tapizados-miura/uploads/trabajos/1-despues.webp",
    "imagenPortada": "/tapizados-miura/uploads/trabajos/1-despues.webp",
    "fotos": []
  },
  {
    "idTrabajo": 4,
    "tipo": "colaboracion",
    "tipoLegible": "Colaboración",
    "titulo": "La Gran Paila: Tradición y Confort",
    "cliente": "Restaurante La Gran Paila",
    "servicio": "Fabricación a medida",
    "imagenPortada": "/tapizados-miura/uploads/trabajos/4-1.webp",
    "fotos": [
      { "url": "/tapizados-miura/uploads/trabajos/4-1.webp", "textoAlternativo": "Comedor de La Gran Paila" }
    ]
  }
]
```

- **Campos principales:**
  - `imagenAntes` e `imagenDespues`: siempre presentes en las restauraciones (RF08).
    En las colaboraciones pueden faltar.
  - `imagenPortada`: la mejor foto para una miniatura, que es el "después" o la primera foto
    adicional.
  - `fotos`: fotos adicionales; hasta 8 por trabajo.
- **Errores:** 400 si `tipo` no es `restauracion` ni `colaboracion`.

### GET /api/trabajos/{id}

Un trabajo visible con todas sus fotos. Tiene los mismos campos de la lista.

- **Errores:**
  - 400 si el id no es numérico.
  - 404 si no existe o está oculto.

### POST /api/cotizaciones

Registra la cotización que el cliente armó en el carrito y devuelve el enlace de WhatsApp
para enviarla al negocio.

- **Requisito:** RF03 / RF04, CU04 / CU05 y RNF08.
- **Cuerpo:**

```json
{
  "items": [
    { "idProducto": 1, "cantidad": 1 },
    { "idProducto": 4, "cantidad": 2 },
    { "idProducto": 3, "cantidad": 1 }
  ]
}
```

- **Reglas:**
  - **Cantidad de productos:** al menos uno (CU05-2a, "Carrito vacío") y máximo 20 distintos.
  - **Cantidad por producto:** entero entre 1 y 10. También se acepta como texto (`"2"`),
    tal como llega desde un input.
  - **Productos repetidos:** si el mismo producto viene dos veces, se suman las cantidades.
  - **Precio:** se toma siempre de la base de datos. Cualquier precio enviado se ignora.
  - **Productos no disponibles:** si alguno no existe o está oculto, se rechaza toda la
    cotización y se indica cuál retirar del carrito.
  - **Stock:** cotizar no lo descuenta, porque es una cotización y no una venta.
  - **Agotados:** los muebles agotados, o pedidos en mayor cantidad que el stock, se aceptan
    y quedan marcados como "sujeto a fabricación".
  - **Registro:** la cotización y su detalle se guardan en una sola transacción, con estado
    `pendiente`.
- **Respuesta 201:**

```json
{
  "mensaje": "Cotización N.º 3 registrada. Abra el enlace para enviarla por WhatsApp.",
  "datos": {
    "idCotizacion": 3,
    "estado": "pendiente",
    "totalItems": 3,
    "totalUnidades": 4,
    "total": 5999996.00,
    "items": [
      { "idProducto": 1, "nombre": "Sofácama Cajón Soren (140 cm) Azul", "cantidad": 1, "precioUnitario": 1999999.00, "subtotal": 1999999.00, "agotado": false, "sujetoAFabricacion": false },
      { "idProducto": 4, "nombre": "Poltrona Reclinable Azul Noche", "cantidad": 2, "precioUnitario": 1299999.00, "subtotal": 2599998.00, "agotado": false, "sujetoAFabricacion": false },
      { "idProducto": 3, "nombre": "Sofá Roman 2 Puestos Gris Claro", "cantidad": 1, "precioUnitario": 1399999.00, "subtotal": 1399999.00, "agotado": true, "sujetoAFabricacion": true }
    ],
    "mensaje": "Hola, Tapizados El Miura. Quiero esta cotización (N.º 3):\n- 1 x Sofácama Cajón Soren (140 cm) Azul: $1.999.999\n- 2 x Poltrona Reclinable Azul Noche ($1.299.999 c/u): $2.599.998\n- 1 x Sofá Roman 2 Puestos Gris Claro: $1.399.999 (agotado, sujeto a fabricación)\nTotal estimado: $5.999.996",
    "enlaceWhatsapp": "https://wa.me/573128628147?text=Hola%2C%20Tapizados%20El%20Miura.%20Quiero%20esta%20cotizaci%C3%B3n%20..."
  }
}
```

- **Uso en el sitio:** abrir `datos.enlaceWhatsapp` (por ejemplo con `window.location.href`)
  y vaciar el carrito.
- **Codificación del enlace (RNF08):** los espacios se codifican como `%20` y no como `+`,
  porque WhatsApp mostraría el `+` literalmente.
- **Nota de disponibilidad:** cuando la cantidad supera el stock pero hay unidades, el
  mensaje dice, por ejemplo, "(2 disponibles; el resto, sujeto a fabricación)".
- **Errores:**
  - 400 con detalle por posición, por ejemplo `"items[0].cantidad": "Debe ser un número entero entre 1 y 10."`.
  - 400 con los productos no disponibles: `"producto 5": "El producto no existe o ya no está disponible."`.
  - 415 si no se envía como JSON.
  - 500 si el negocio no tiene un WhatsApp válido configurado.

---

## Autenticación

### POST /api/auth/login

Inicia la sesión del administrador.

- **Requisito:** RF06 / CU01 y RNF02. La contraseña se verifica con BCrypt; la base de datos
  solo guarda el hash.
- **Cuerpo:**

```json
{ "correo": "admin@tapizadosmiura.com", "contrasena": "Miura2026*Cambiar" }
```

- **Respuesta 200:** además del cuerpo, el servidor envía la cookie `JSESSIONID`.

```json
{
  "mensaje": "Sesión iniciada. Bienvenido, Administrador Miura.",
  "datos": {
    "autenticado": true,
    "administrador": { "idAdministrador": 1, "nombre": "Administrador Miura", "correo": "admin@tapizadosmiura.com" }
  }
}
```

- **Errores:**
  - 400 si falta el correo o la contraseña (CU01, flujo alterno).
  - 401 "Correo o contraseña incorrectos.". El mensaje es el mismo si el correo no existe,
    para no revelar qué correos están registrados.
- **Seguridad:** el identificador de sesión se cambia al iniciar sesión, para evitar la
  fijación de sesión.

### POST /api/auth/logout

Cierra la sesión del administrador. No lleva cuerpo.

- **Respuesta 200:**

```json
{ "mensaje": "Sesión cerrada.", "datos": { "autenticado": false } }
```

### GET /api/auth/sesion

Indica si hay una sesión iniciada. Siempre responde 200.

```json
{ "autenticado": true, "administrador": { "idAdministrador": 1, "nombre": "Administrador Miura", "correo": "admin@tapizadosmiura.com" } }
```

Si no hay sesión, responde `{ "autenticado": false }`.

---

## Administración: productos

Todas exigen sesión (401 si no hay). Aplican las mismas validaciones del panel (CU07 / RF07).

**Regla del proyecto:** las fotos se suben desde el panel. Por eso un producto creado por la
API **nace oculto y sin foto**, y ningún producto sin foto puede quedar visible (409). La
regla también aplica en el panel: allí muestra la etiqueta "Sin foto" y no deja publicarlo
hasta que se le suba una.

### GET /api/admin/productos

Todos los productos, visibles y ocultos (los visibles primero).

- **Parámetro opcional `q`:** busca en nombre o referencia.
- **Respuesta 200:** los mismos campos del catálogo público más:
  - `estado`: `true` si es visible en el catálogo.
  - `tieneFoto`: `false` si no se puede publicar todavía.

### GET /api/admin/productos/{id}

Un producto (visible u oculto) con todas sus fotos. Responde 404 si no existe.

### POST /api/admin/productos

Crea un producto. Siempre queda oculto.

- **Cuerpo:** usa los mismos nombres de campo del formulario del panel.

```json
{
  "referencia": "00512",
  "nombre": "Silla de comedor Nórdica",
  "idCategoria": 3,
  "precio": 350000,
  "stock": 5,
  "descripcion": "Silla tapizada para comedor.",
  "estilo": "Nórdico",
  "color": "Gris",
  "materialTapiz": "Tela antifluido",
  "materialEstructura": "Pino",
  "tipoRelleno": "Espuma de alta densidad",
  "tipoPata": "Madera",
  "altoCm": 95.5,
  "anchoCm": 45,
  "profundidadCm": 50,
  "pesoKg": 6.5,
  "resistenciaKgPuesto": 120,
  "requiereArmado": false
}
```

- **Obligatorios:**
  - `referencia`: hasta 20 letras, números o guiones; única.
  - `nombre`: máximo 150 caracteres.
  - `idCategoria`: categoría existente y activa.
  - `precio`: mayor que cero.
  - `stock`: entero mayor o igual a cero.
- **Opcionales:**
  - Textos de la ficha (`estilo`, `color`, `materialTapiz`, `materialEstructura`,
    `tipoRelleno`, `tipoPata`): máximo 60 u 80 caracteres según el campo.
  - Medidas (`altoCm`, `anchoCm`, `profundidadCm`, `pesoKg`): mayores que cero.
  - `resistenciaKgPuesto`: entero mayor que cero.
  - `requiereArmado`: booleano.
- **Tipos de dato:** los números pueden enviarse como número o como texto (`"62,2"` también
  es válido). Los booleanos deben ser `true` o `false`.
- **Campos desconocidos:** responden 400. Esto evita que un error de escritura pase
  inadvertido.
- **Respuesta 201:** mensaje y el producto creado (`estado: false`, `tieneFoto: false`).
  Si se envió `"estado": true`, el mensaje avisa que se ignoró.
- **Errores:**
  - 400 con el detalle por campo.
  - 409 si la referencia ya existe.

### PUT /api/admin/productos/{id}

Reemplaza **todos** los datos del producto. Lleva el mismo cuerpo del POST y puede incluir
`"estado": true | false`.

- **Campos no enviados:** quedan vacíos, igual que al guardar el formulario del panel.
- **Foto:** la foto actual se conserva.
- **Respuesta 200:** mensaje y el producto actualizado con sus fotos.
- **Errores:**
  - 400 con el detalle por campo.
  - 404 si no existe.
  - 409 si la referencia es de otro producto, o si se pide `estado: true` y el producto no
    tiene foto.

### PUT /api/admin/productos/{id}/estado

Muestra u oculta un producto (borrado lógico: los productos no se eliminan, para conservar
las cotizaciones que los incluyen).

- **Cuerpo:** `{ "estado": true }` para mostrarlo, o `{ "estado": false }` para ocultarlo.
- **Respuesta 200:** mensaje y el producto.
- **Errores:**
  - 400 si `estado` falta o no es booleano.
  - 404 si no existe.
  - 409 si se intenta mostrar un producto sin foto.

---

## Administración: trabajos

Requieren sesión (CU08). Crear y editar trabajos exige subir fotos (RF08), por eso se hace
en el panel; la API permite consultarlos, mostrarlos u ocultarlos, y eliminarlos.

### GET /api/admin/trabajos

Todos los trabajos, visibles y ocultos.

- **Parámetro opcional `tipo`:** `restauracion` o `colaboracion`.
- **Respuesta 200:** los mismos campos de la galería pública más `estado`.

### GET /api/admin/trabajos/{id}

Un trabajo con sus fotos. Responde 404 si no existe.

### PUT /api/admin/trabajos/{id}/estado

Muestra u oculta un trabajo en la galería.

- **Cuerpo:** `{ "estado": true | false }`.
- **Respuesta 200:** mensaje y el trabajo.
- **Errores:** 400 si `estado` no es booleano; 404 si no existe.

### DELETE /api/admin/trabajos/{id}

Elimina el trabajo, sus fotos adicionales (borrado en cascada en la BD) y los archivos de
imagen del disco.

- **Respuesta 200:**

```json
{ "mensaje": "Trabajo \"Sofá clásico en naranja\" eliminado junto con sus fotos." }
```

- **Errores:** 404 si no existe.

---

## Administración: información del negocio

Requieren sesión (CU09 / RF09).

### GET /api/admin/informacion

Los mismos datos y formato de `GET /api/informacion`.

### PUT /api/admin/informacion

Actualiza **solo** los datos enviados, indexados por clave.

```json
{
  "whatsapp": "+57 312 862 8147",
  "horario": "Lunes a sábado, 8:00 a. m. - 6:00 p. m."
}
```

- **Reglas (las mismas del panel):**
  - Ningún dato puede quedar vacío ni superar 2000 caracteres.
  - `whatsapp`: se guarda solo con dígitos e indicativo. `"+57 312 862 8147"` queda como
    `573128628147`, que es el formato que exige `wa.me`.
  - `telefono`: números, espacios, guiones o el signo +.
  - `correo`: dirección de correo válida.
  - Redes sociales: dirección completa que empiece por `https://`.
  - Solo se aceptan claves que ya existen.
  - Si un dato tiene error no se guarda ninguno.
- **Respuesta 200:** mensaje y toda la información actualizada.
- **Errores:** 400 con el detalle por clave.

---

## Administración: cotizaciones

Requieren sesión. Permiten hacer seguimiento a las cotizaciones que llegan por WhatsApp.

### GET /api/admin/cotizaciones

Listado de cotizaciones, las más recientes primero, con sus totales y sin el detalle.

- **Parámetro opcional `estado`:** `pendiente`, `atendida`, `cerrada` o `descartada`.
- **Respuesta 200:**

```json
[
  {
    "idCotizacion": 3,
    "estado": "pendiente",
    "creadoEn": "2026-10-18T15:02:13",
    "actualizadoEn": "2026-10-18T15:02:13",
    "totalItems": 3,
    "totalUnidades": 4,
    "total": 5999996.00
  }
]
```

- **Errores:** 400 si `estado` no es uno de los cuatro valores.

### GET /api/admin/cotizaciones/{id}

Una cotización con sus productos.

- **Precios:** el nombre y el precio de cada producto son los del momento del envío, aunque
  el producto haya cambiado después.
- **Errores:** 404 si no existe.

### PUT /api/admin/cotizaciones/{id}/estado

Cambia el estado de atención.

```json
{ "estado": "atendida", "notaInterna": "Se llamó al cliente; pidió otro color." }
```

- **`estado` (obligatorio):** `pendiente`, `atendida`, `cerrada` o `descartada`. Se puede
  pasar de cualquier estado a cualquier otro.
- **`notaInterna` (opcional, máximo 2000 caracteres):**
  - Si no se envía, se conserva la nota actual.
  - Si se envía vacía (`""`) o `null`, se borra.
- **Respuesta 200:** mensaje y la cotización actualizada.
- **Errores:** 400 si el estado no es válido; 404 si no existe.

---

## Código fuente

```
src/main/java/co/tapizadosmiura/
├── api/                    Base de la API
│   ├── ApiServlet          Respuestas JSON, errores, lectura del cuerpo y de la ruta
│   ├── ApiExcepcion        Errores previstos con su código HTTP
│   ├── ApiRespaldoServlet  404 en JSON para rutas /api desconocidas
│   ├── CamposJson          Convierte el JSON al formato de los validadores del panel
│   ├── Json                Configuración de Gson
│   ├── UrlPublica          Direcciones de las imágenes
│   ├── dto/                Respuestas de cada servicio (records)
│   ├── publico/            Catálogo, galería, información y cotizaciones
│   ├── auth/               Login, logout y sesión
│   └── admin/              Productos, trabajos, información y cotizaciones
├── filtro/ApiAutenticacionFiltro   401 en /api/admin/* sin sesión
├── dao/CotizacionDAO               Registro y seguimiento de cotizaciones
└── util/                           CotizacionValidador, MensajeWhatsApp, FormatoMoneda
```

La API reutiliza los DAO y los validadores del panel (`ProductoValidador`,
`InformacionValidador`). Así un dato es válido o inválido de la misma forma, se ingrese por
donde se ingrese.

---

## Limitaciones conocidas

Para el alcance actual del proyecto se dejan documentadas, sin resolver:

- **Intentos de login:** no hay límite de intentos fallidos.
- **Cotizaciones masivas:** el envío de cotizaciones no tiene un freno contra envíos
  masivos (por ejemplo, un captcha).
- **Paginación:** los listados no se paginan. Con el volumen actual del catálogo responden
  en milisegundos; si el inventario crece a cientos de productos (RNF05), conviene agregar
  los parámetros `pagina` y `tamano`.
