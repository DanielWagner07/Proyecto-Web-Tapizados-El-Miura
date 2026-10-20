# Front-end del sitio público

Evidencia GA7-220501096-AA4-EV03, Componente front-end del proyecto.

El sitio público son páginas HTML que piden sus datos a la API del proyecto
(`docs/API.md`) con `fetch()`. Ningún producto, precio, foto o dato de contacto está
escrito en el HTML: todo se administra desde el panel y aparece en el sitio al instante.

## Contenido

- [Páginas](#páginas)
- [Estructura de archivos](#estructura-de-archivos)
- [Módulos comunes](#módulos-comunes)
- [Carrito de cotización](#carrito-de-cotización)
- [Estándares de codificación](#estándares-de-codificación)
- [Diseño](#diseño)
- [Cómo agregar una página](#cómo-agregar-una-página)
- [Verificación del código](#verificación-del-código)

---

## Páginas

Cada página corresponde a un prototipo de la evidencia GA5 (interfaz gráfica y mapa de
navegación) y a sus requisitos.

- **`index.html`, Inicio** (prototipo 1).
  - Contenido: portada con fotos reales, categorías, "Lo que ofrecemos" (8 productos,
    primero los disponibles), trabajos recientes y los tres pasos para cotizar.
  - Servicios: `GET /api/productos`, `/api/categorias` y `/api/trabajos`.
- **`muebles.html`, Catálogo** (prototipo 2, RF01 / CU02).
  - Contenido: filtros desplegables de categoría, color, tela, estructura, relleno, precio
    y disponibilidad, más orden y búsqueda.
  - Admite `?categoria=N` y `?q=texto`.
  - Servicios: `GET /api/productos?q=` y `/api/categorias`.
- **`producto.html?id=N`, Detalle** (prototipo 3, RF02 / CU03).
  - Contenido: fotos, ficha técnica, cantidad, "Agregar al carrito", "Preguntar por
    WhatsApp" y productos relacionados.
  - Servicios: `GET /api/productos/{id}` y `/api/productos?categoria=`.
- **`cotizacion.html`, Carrito de cotización** (RF03 / RF04, CU04 / CU05).
  - Contenido: revisa precios y disponibilidad, permite cambiar cantidades y envía la
    cotización por WhatsApp.
  - Servicios: `GET /api/productos/{id}` y `POST /api/cotizaciones`.
- **`restauraciones.html`, Tapizado y restauración** (prototipo 4, RF05 / CU06).
  - Contenido: servicios del taller y un comparador deslizante de antes y después.
  - Servicios: `GET /api/servicios` y `/api/trabajos?tipo=restauracion`.
- **`renovaciones.html`, Renovaciones para negocios** (prototipo 5, RF05 / CU06).
  - Contenido: proyectos para negocios con cliente, servicio y galería.
  - Servicio: `GET /api/trabajos?tipo=colaboracion`.
- **`informacion.html`, Información y contacto** (prototipo 6, RF09 / CU09).
  - Contenido: trayectoria, contacto, servicios, cobertura, garantía y redes.
  - Servicios: `GET /api/informacion` y `/api/servicios`.

Todas las páginas comparten el encabezado (logo, buscador, carrito y menú), el pie y el
botón flotante de WhatsApp con el número que está en la BD.

**Menú principal:** Muebles, Tapizado y restauración, Renovaciones e Información.
- Se quitaron del prototipo "Decoración" y "Trayectoria": no tenían contenido propio, y
  la trayectoria se muestra en Información.
- También se quitaron los enlaces de la barra superior sin respaldo real, como "Envío
  gratis" y "Asesoría en diseño".

## Estructura de archivos

```
src/main/webapp/
├── index.html, muebles.html, producto.html, cotizacion.html,
│   restauraciones.html, renovaciones.html, informacion.html
├── css/
│   ├── base.css           Variables de diseño, encabezado, pie, botones, tarjetas, avisos...
│   └── <pagina>.css       Solo lo exclusivo de cada página
├── js/
│   ├── comun/             Módulos que comparten todas las páginas
│   └── <pagina>.js        Lógica de cada página (inicio.js es la de index.html)
├── fonts/                 Barlow y Barlow Condensed (licencia OFL, incluidas en el proyecto)
└── img/
    ├── Logo TapizadosElMiura.png   Logo original del negocio (fuente)
    ├── logo-miura.png              Versión liviana del logo para la web (8 KB)
    ├── favicon.png                 Ícono de la pestaña (el toro del logo)
    └── sin-imagen.svg              Imagen genérica cuando un registro no tiene foto
```

Cada página HTML solo trae su estructura. Al final del `<head>` carga su script como
módulo (`<script type="module" src="js/<pagina>.js">`), y el script llena el contenido.

## Módulos comunes

Están en `js/comun/` y cada uno tiene una sola responsabilidad:

- **`api.js`:** todas las llamadas a la API (`obtener` y `enviar`).
  - Convierte las respuestas de error al formato único `ErrorApi` (código, mensaje y
    detalle por campo).
  - Sin conexión, el mensaje es "No hay conexión con el servidor...".
- **`plantilla.js`:** dibuja el encabezado, el pie y el botón de WhatsApp.
  - Pide `GET /api/informacion` una sola vez por página y lo comparte con
    `informacionNegocio()`.
- **`carrito.js`:** el carrito de cotización (ver la sección siguiente).
- **`tarjeta-producto.js`:** tarjeta de producto usada en el inicio, el catálogo y los
  relacionados, con su botón "+".
- **`formato.js`:**
  - `pesos()`: formato de precios.
  - `crear()`: crea elementos de forma segura.
  - `enlaceWhatsApp()`, `parametroUrl()` y `plural()`.
- **`avisos.js`:** mensajes breves como "Agregado al carrito", anunciados también a
  lectores de pantalla.
- **`visor.js`:** amplía fotos con el elemento nativo `<dialog>`.
- **`iconos.js`:** íconos SVG del sitio.

## Carrito de cotización

- **Dónde se guarda:** mientras el cliente arma la cotización, esta vive en su navegador
  (`localStorage`, clave `miura.carrito`). Es lo que define el esquema de la BD: la
  cotización solo se registra cuando el cliente la envía, y así no se acumulan carritos
  abandonados.
- **Límites:** máximo 10 unidades por mueble y 20 muebles distintos, los mismos de la API.
- **Datos actualizados:** al abrir `cotizacion.html` se consulta cada producto. Si alguno
  ya no está disponible, se retira y se avisa; si cambió de precio o nombre, se actualiza.
- **Agotados:** los muebles agotados, o pedidos en mayor cantidad que el stock, se marcan
  "se fabrica bajo pedido".
- **Envío:** "Enviar cotización por WhatsApp" llama a `POST /api/cotizaciones`, vacía el
  carrito y abre el enlace `wa.me` con el mensaje listo (CU05).
  - Si el cliente vuelve a la pestaña, ve el número de su cotización y un botón para
    abrir WhatsApp de nuevo.
  - Si la API rechaza un producto ("producto N"), ese producto se retira del carrito para
    que el cliente pueda volver a enviar.
- **Precio definitivo:** el total del carrito es una estimación. El servidor calcula el
  precio con la BD y nunca usa el que envía el navegador.

## Estándares de codificación

**JavaScript**
- Módulos ES (`import` / `export`), `const` y `let` (nunca `var`), `async` / `await` y
  comparaciones estrictas (`===`).
- Nombres en español y en `camelCase`; constantes de configuración en `MAYUSCULAS`.
- Cada archivo empieza con un comentario `@file` que dice qué hace y qué servicios usa.
  Cada función tiene su comentario JSDoc.
- **Seguridad (XSS):** los datos que vienen de la API se insertan con `textContent`
  (función `crear()`), nunca como HTML. Solo los íconos, que son textos fijos del código,
  se insertan como SVG.
- Sin `alert()` y sin `onclick` dentro del HTML: los eventos se registran con
  `addEventListener`.
- Si una consulta falla, la página muestra un mensaje claro en lugar de quedar en blanco.

**HTML**
- Etiquetas semánticas: `header`, `nav`, `main`, `section`, `article`, `aside` y `footer`.
- Migas de pan con `nav` y `aria-current`.
- Accesibilidad:
  - Imágenes con `alt`; las decorativas con `alt=""`.
  - Campos con su etiqueta y botones de solo ícono con `aria-label`.
  - Enlace "Ir al contenido", avisos con `aria-live` y foco visible con el teclado.

**CSS**
- Variables de diseño en `:root` (colores del logo, tipografía y medidas) en `base.css`.
- Clases con la convención BEM en español: `bloque__elemento--variante`.
- Diseño "móvil primero": los estilos base son para celular y se amplían con
  `@media (min-width: ...)`.
- Se respeta la preferencia del sistema de reducir el movimiento.

## Diseño

- **Colores:** los del logo. Rojo `#D40012` para acciones, naranja `#FC8000` para acentos
  (sello "Agotado", franja de la tarjeta de contacto) y negro `#131212`, sobre fondo blanco.
- **Letra:** Barlow Condensed para los títulos, parecida a la letra condensada del logo, y
  Barlow para el texto. Las fuentes están dentro del proyecto, así que el sitio no depende
  de servidores externos.
- **Catálogo:** inspirado en catálogos de tiendas de muebles, con filtros desplegables a la
  izquierda, rejilla de productos, foto en fondo blanco, precio y botón "+" para agregar.
  En celular los filtros se abren como panel lateral.
- **Elemento distintivo:** la forma naranja del espaldar del logo detrás de las fotos de la
  portada, y el comparador antes y después de las restauraciones.

## Cómo agregar una página

1. Copiar una página sencilla, por ejemplo `renovaciones.html`, y cambiar el título, la
   descripción, `data-pagina` del `<body>` y las rutas de su CSS y JS.
2. Crear `css/<pagina>.css` (solo los estilos propios) y `js/<pagina>.js`.
3. En el JS, importar `iniciarPlantilla` de `comun/plantilla.js` y llamarla primero. Luego
   pedir los datos con `obtener()` de `comun/api.js`.
4. Si la página va en el menú, agregarla a la constante `MENU` de `comun/plantilla.js`.

## Verificación del código

Se revisó con herramientas automáticas, sin agregarlas como dependencias del proyecto:

- **ESLint** (reglas recomendadas, más `no-var`, `prefer-const`, `eqeqeq`, `no-alert`,
  `curly` y `no-console`): sin hallazgos en `js/`.
- **html-validate** (reglas recomendadas, incluye WCAG): sin hallazgos en las 7 páginas.
- **axe-core** (WCAG 2 A y AA) en celular y escritorio: sin hallazgos.

El registro completo de pruebas está en `docs/pruebas-AA4.md`.
