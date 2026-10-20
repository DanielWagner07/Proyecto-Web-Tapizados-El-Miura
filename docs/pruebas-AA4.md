# Pruebas del front-end

Evidencia GA7-220501096-AA4-EV03. Este documento registra las pruebas del sitio público
conectado a la API (`docs/frontend.md`).

- **Entorno:**
  - Tomcat 10.1 y MariaDB con el volcado del 18 de octubre de 2026 (14 productos visibles
    con sus fotos reales, 6 categorías).
  - Navegador Chromium automatizado con Playwright.
  - Dos tamaños de pantalla: escritorio (1366 × 860) y celular (375 × 760 y 390 × 844).
- **Fecha:** 18 de octubre de 2026.

## Revisión automática de cada página

Se revisaron 9 direcciones en los dos tamaños de pantalla:

- `index.html`.
- `muebles.html` y `muebles.html?categoria=6`.
- `producto.html?id=7` y `producto.html?id=999`.
- `cotizacion.html`, `restauraciones.html`, `renovaciones.html` e `informacion.html`.

En cada una se comprobó:

- **Errores de JavaScript:** ninguno.
- **Desborde horizontal** (contenido más ancho que la pantalla): ninguno.
- **Accesibilidad con axe-core** (WCAG 2 A y AA): sin hallazgos.
  - La primera revisión encontró textos con poco contraste: el gris de los conteos de
    filtros, el verde de los botones de WhatsApp y el resumen del carrito vacío.
  - Se oscureció el gris secundario y el verde de los botones con texto, y se volvió a
    revisar sin hallazgos.

Revisión del código:

- **ESLint:** sin hallazgos en `js/`.
- **html-validate:** sin hallazgos en las 7 páginas.
  - Se corrigieron dos avisos: un `<form>` sin botón de envío (los filtros ahora están en
    un `<div>`) y un `aria-label` en una lista.

## Flujo de cotización (RF03 / RF04, CU04 / CU05)

Prueba de punta a punta en el navegador. La apertura de WhatsApp se interceptó para no
salir del sitio.

1. Catálogo filtrado por "Sillas mecedoras": muestra 2 productos.
   - El botón "+" del primero lo agrega.
   - Aparece el aviso "Agregado al carrito: Silla mecedora beige base madera curva" con el
     enlace "Ver carrito", y el contador del encabezado pasa a 1.
2. Detalle del producto 12 (Comedor cuadrado):
   - El selector de cantidad sube a 5.
   - "Agregar al carrito" lleva el contador a 6.
3. Se metió a mano en el carrito el producto 5, que está oculto.
   - Al abrir `cotizacion.html` se retiró solo, con el aviso "Ya no está disponible y se
     quitó del carrito: Sofacama oculto.".
4. El carrito mostró 2 filas y un total estimado de $ 9.100.000 (5 × 1.650.000 + 850.000).
   - El comedor (4 en stock y 5 pedidos) quedó marcado "Hay 4 unidades; el resto se
     fabrica bajo pedido".
5. "Enviar cotización por WhatsApp":
   - Abrió `https://wa.me/573128628147?text=Hola%2C%20Tapizados%20El%20Miura...`, con los
     espacios codificados como `%20`.
   - En la BD quedó la cotización `pendiente` con sus 2 productos, 6 unidades y total
     9100000.00.
6. Al volver a la pestaña:
   - Aparece "Cotización N.º 4 registrada" con el botón "Abrir WhatsApp".
   - El carrito queda vacío y el botón de envío se deshabilita (CU05-2a).

## Pruebas por página

**Inicio**
- La portada muestra 3 fotos reales de categorías distintas.
- Aparecen 5 categorías con su foto y su número de productos; Poltronas no aparece porque
  no tiene productos visibles.
- "Lo que ofrecemos" muestra 8 productos, primero los disponibles.
- Hay 3 trabajos recientes; cada uno lleva a su galería y se ubica en ese trabajo (`#trabajo-N`).

**Catálogo**
- **Filtros:**
  - Las opciones salen de los valores reales de la ficha técnica, con su conteo.
  - Cambiar la categoría actualiza el título, la descripción, las migas de pan y la
    dirección (`?categoria=N`) sin recargar la página.
- **Búsqueda** (`?q=mecedora`): muestra "Resultados para "mecedora"" con 2 productos y la
  opción "Quitar búsqueda". La búsqueda la resuelve el servidor (`GET /api/productos?q=`).
- **Sin resultados:** "No hay productos disponibles por el momento" (CU02-2a).
- **Orden:** disponibles primero, menor precio, mayor precio y nombre.
- **Celular:** el botón "Filtros" abre el panel lateral con fondo oscurecido; se cierra con
  el botón, con "Ver productos" o con Esc.

**Detalle**
- **Ficha técnica:** muestra solo los datos que tiene el producto, con sus unidades
  (cm, kg), y "Requiere armado: Sí/No".
- **Disponibilidad:** un producto disponible muestra "Disponible: N unidades"; uno
  agotado, el sello "Agotado" y "Agotado: lo fabricamos bajo pedido" (CU03-2a).
- **Cantidad:** el selector no baja de 1 ni pasa de 10.
- **Otros datos:** garantía y cobertura salen de la información del negocio, y "Preguntar
  por WhatsApp" lleva el nombre y la referencia del mueble.
- **Producto inexistente u oculto** (`id=999`): "Este mueble ya no está disponible" con el
  enlace al catálogo.

**Tapizado y restauración**
- Muestra los 3 servicios.
- **Comparador antes y después:** se arrastra con el mouse o el dedo y se maneja con las
  flechas del teclado, porque es un control deslizante accesible.
- "Solicitar restauración" abre WhatsApp con un mensaje listo.

**Renovaciones**
- Cada proyecto muestra su foto principal, sus miniaturas (se amplían en el visor), su
  cliente, el servicio y el número de fotos.

**Información**
- Muestra la trayectoria, la tarjeta de contacto (WhatsApp, teléfono con `tel:`, correo con
  `mailto:`, dirección con enlace al mapa y horario), los servicios, la cobertura, la
  garantía y las 3 redes sociales.
- Todo sale de la BD.

## Errores de conexión

Se bloquearon todas las peticiones a `/api/` para simular el servidor caído:

- Inicio, catálogo, detalle y restauraciones muestran "No hay conexión con el servidor.
  Revise su internet e intente de nuevo.". El catálogo agrega el botón "Intentar de nuevo".
- Información muestra "No fue posible cargar la información del negocio...".
- No hubo errores de JavaScript; ninguna página quedó en blanco.

## Panel administrador (regresión)

- El logo PNG aparece en el formulario de ingreso y en la barra del panel, y las páginas
  del panel tienen el ícono del toro.
- **Cambiar la foto de un producto:** se editó el producto 00099 con una foto nueva. La BD
  apunta al archivo nuevo y el anterior se borró de `miura-uploads/productos` (antes
  quedaba en el disco sin usarse).
- **44 pruebas JUnit:** todas exitosas.
- **Colección de Postman de la API:** sigue pasando completa.
