# Pruebas del módulo de catálogo (servlets y JSP)

Evidencia GA7-220501096-AA2-EV02. Este documento registra las pruebas del módulo de
inicio de sesión (CU01) y del gestor de catálogo (CU07).

- **Entorno:** Tomcat 10.1, MySQL 8 con `01_esquema.sql` y `02_datos_prueba.sql`.
- **Fecha:** 16 de octubre de 2026.

## Pruebas unitarias (JUnit 5)

Se ejecutan con `mvnw.cmd test`. Resultado: 12 de 12 pruebas exitosas.

`ProductoValidadorTest`, validación del formulario de producto:

- Un formulario con solo los campos obligatorios es válido.
- Los campos obligatorios vacíos generan error.
- Se rechaza un precio cero, negativo o no numérico (RF07).
- Se rechaza un stock negativo o decimal.
- La referencia admite solo letras, números y guiones.
- Las medidas aceptan coma decimal y rechazan valores no positivos.
- Una casilla desmarcada queda en `false`.
- Se recortan los espacios sobrantes de los textos.

`SeguridadUtilTest`, verificación de contraseñas:

- Acepta la contraseña correcta con un hash `$2y$` como el de los datos de prueba.
- Rechaza una contraseña incorrecta.
- Rechaza valores nulos o hashes mal formados sin lanzar errores.
- Verifica un hash recién generado.

## Pruebas funcionales

Cada prueba indica la petición, el resultado esperado y el resultado obtenido.
Todas pasaron.

1. **GET `/admin/productos` sin iniciar sesión.**
   Esperado: redirige al login. Obtenido: 302 a `/admin/login`.
2. **POST `/admin/login` con los campos vacíos.**
   Esperado: mensaje de campos obligatorios. Obtenido: "Ingrese su correo y su contraseña."
3. **POST `/admin/login` con una contraseña incorrecta.**
   Esperado: mensaje genérico de error. Obtenido: "Correo o contraseña incorrectos."
4. **POST `/admin/login` con credenciales correctas.**
   Esperado: entra al panel. Obtenido: 302 a `/admin/productos`, con el nombre del administrador visible.
5. **GET `/admin/productos`.**
   Esperado: lista los 4 productos de prueba. Obtenido: 4 filas, precios con formato `$ 1.299.999` y "Agotado" en el producto sin stock.
6. **GET `/admin/productos?q=roman`.**
   Esperado: filtra por nombre. Obtenido: "2 producto(s)".
7. **GET `/admin/productos?accion=nuevo`.**
   Esperado: el formulario carga las categorías activas. Obtenido: Sofás, Poltronas y Comedores.
8. **POST para crear con precio 0 y sin imagen.**
   Esperado: no guarda y muestra los errores por campo. Obtenido: "El precio debe ser mayor que cero." y "Seleccione una imagen."
9. **POST para crear con una referencia existente (00014).**
   Esperado: no guarda. Obtenido: "Ya existe otro producto con esta referencia."
10. **POST para crear con un archivo `.txt` como imagen.**
    Esperado: lo rechaza. Obtenido: "Formato no permitido. Use JPG, PNG o WEBP."
11. **POST para crear un producto válido con tildes y ñ.**
    Esperado: lo guarda con su imagen. Obtenido: el producto y su `producto_imagen` quedan en la BD, la ñ se guarda correctamente y aparece el aviso de éxito.
12. **GET de la imagen recién subida.**
    Esperado: se muestra. Obtenido: 200 `image/png`.
13. **GET de una imagen de los datos de prueba sin archivo.**
    Esperado: muestra la imagen genérica. Obtenido: 302 a `img/sin-imagen.svg`.
14. **GET `/uploads/../../etc/passwd.png`.**
    Esperado: bloquea la salida de la carpeta. Obtenido: 400.
15. **POST para editar el precio sin enviar foto.**
    Esperado: actualiza el precio y conserva la foto. Obtenido: precio 299999.00 y 1 imagen.
16. **POST `accion=estado` con `activo=false`.**
    Esperado: oculta el producto del catálogo público. Obtenido: `estado = 0` y el producto ya no aparece en `v_catalogo_publico`.
17. **GET para editar un id inexistente (999).**
    Esperado: vuelve al listado con un aviso. Obtenido: 302 al listado.
18. **Búsqueda con `<script>alert(1)</script>`.**
    Esperado: el texto se escapa (protección XSS). Obtenido: no aparece ninguna etiqueta `<script>` en la página.
19. **POST `/admin/logout` y luego GET `/admin/productos`.**
    Esperado: cierra la sesión. Obtenido: redirige al login con el aviso de salida, y el panel vuelve a exigir el ingreso.
20. **GET `/admin/css/admin.css` sin sesión.**
    Esperado: la hoja de estilos es pública. Obtenido: 200.
