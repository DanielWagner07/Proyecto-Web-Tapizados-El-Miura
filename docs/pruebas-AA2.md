# Pruebas del módulo de catálogo (servlets y JSP)

Evidencia GA7-220501096-AA2-EV02. Este documento registra las pruebas del panel
administrador: inicio de sesión (CU01), gestor de catálogo (CU07), galería de trabajos
(CU08) e información del negocio (CU09).

- **Entorno:** Tomcat 10.1, MySQL 8 con `01_esquema.sql` y `02_datos_prueba.sql`.
- **Fechas:** 16 y 17 de octubre de 2026.

## Pruebas unitarias (JUnit 5)

Se ejecutan con `mvnw.cmd test`. Resultado: 25 de 25 pruebas exitosas.

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

`TrabajoValidadorTest`, galería de trabajos:

- Un formulario con tipo y título es válido.
- El tipo debe ser restauración o colaboración.
- El título es obligatorio y admite máximo 150 caracteres.
- El servicio es opcional, pero si se envía debe ser un id válido.
- Una restauración sin foto del antes o del después no es válida (RF08).
- Una colaboración no exige fotos de antes y después.
- No se pueden superar las 8 fotos adicionales.

`InformacionValidadorTest`, información del negocio:

- El WhatsApp se guarda solo con números.
- Ningún dato puede quedar vacío ni superar 2000 caracteres (CU09 2a).
- El WhatsApp debe tener entre 10 y 15 dígitos.
- El teléfono acepta números, espacios, guiones y el signo +.
- El correo debe tener un formato válido.
- Las redes sociales deben ser direcciones web completas.

## Pruebas funcionales: acceso y catálogo

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

## Pruebas funcionales: galería de trabajos

Todas pasaron.

1. **GET `/admin/trabajos` sin iniciar sesión.**
   Esperado: redirige al login. Obtenido: 302 a `/admin/login`.
2. **GET `/admin/trabajos`.**
   Esperado: muestra los 4 trabajos de prueba. Obtenido: 4 tarjetas.
3. **GET `/admin/trabajos?tipo=colaboracion`.**
   Esperado: filtra por tipo. Obtenido: 2 tarjetas.
4. **GET `/admin/trabajos?accion=nuevo&tipo=colaboracion`.**
   Esperado: el formulario abre con "Colaboración" marcada. Obtenido: opción marcada.
5. **POST de una restauración sin fotos.**
   Esperado: no guarda. Obtenido: "Una restauración necesita la foto del antes." y "...del después."
6. **POST de una restauración solo con la foto del antes.**
   Esperado: no guarda. Obtenido: "Una restauración necesita la foto del después."
7. **POST con tipo no válido y título vacío.**
   Esperado: errores por campo. Obtenido: mensajes de tipo y de título obligatorio.
8. **POST de una restauración válida con servicio.**
   Esperado: se publica. Obtenido: el registro queda en `trabajo` con ambas fotos, los archivos en `miura-uploads/trabajos` y aparece el aviso de éxito.
9. **POST de una colaboración con 3 fotos adicionales.**
   Esperado: se guardan en orden. Obtenido: `trabajo_imagen` con orden 0, 1 y 2.
10. **POST para agregar 6 fotos a un trabajo que ya tiene 3.**
    Esperado: lo rechaza por superar el máximo. Obtenido: "Máximo 8 fotos adicionales por trabajo. Puede agregar 5 más."
11. **Editar agregando 2 fotos.**
    Esperado: se agregan al final. Obtenido: 5 fotos, la última con orden 4.
12. **Cambiar a restauración una colaboración sin antes ni después.**
    Esperado: no lo permite. Obtenido: errores de foto del antes y del después.
13. **GET para editar una colaboración.**
    Esperado: muestra sus fotos guardadas con la opción de quitarlas. Obtenido: 5 botones "Quitar".
14. **POST `accion=eliminarFoto`.**
    Esperado: quita la foto de la BD y el archivo. Obtenido: quedan 4 fotos, el archivo se borró y vuelve al formulario de edición.
15. **POST `accion=eliminarFoto` con una foto que pertenece a otro trabajo.**
    Esperado: no la borra. Obtenido: la foto sigue en la BD.
16. **Reemplazar la foto del después de una restauración.**
    Esperado: guarda la nueva y borra la anterior. Obtenido: la ruta cambió y el archivo viejo se borró.
17. **POST `accion=estado` con `activo=false`.**
    Esperado: oculta el trabajo. Obtenido: `estado = 0`.
18. **POST `accion=eliminar`.**
    Esperado: borra el trabajo, sus fotos y los archivos. Obtenido: 0 registros en `trabajo` y `trabajo_imagen`; los archivos pasaron de 6 a 2.
19. **Eliminar un trabajo inexistente (999).**
    Esperado: aviso de error. Obtenido: "El trabajo solicitado no existe."
20. **POST con un archivo `.txt` como foto.**
    Esperado: lo rechaza. Obtenido: "Formato no permitido. Use JPG, PNG o WEBP."

## Pruebas funcionales: información del negocio

Todas pasaron.

1. **GET `/admin/informacion`.**
   Esperado: muestra los 9 datos agrupados. Obtenido: secciones Contacto, Horario, Cobertura, Nosotros, Políticas y Redes sociales, con campos de tipo `tel`, `email` y `url` donde corresponde.
2. **POST con WhatsApp de 3 dígitos, correo sin @, Instagram sin https:// y horario vacío.**
   Esperado: no guarda nada y marca cada campo. Obtenido: 4 mensajes de error y los valores de la BD sin cambios.
3. **POST válido con WhatsApp "+57 310 555-1234", tildes y ñ.**
   Esperado: guarda y normaliza. Obtenido: WhatsApp guardado como `573105551234`, textos con tildes correctos y aviso de éxito.
4. **POST con un campo inventado (`valor_hackeo`).**
   Esperado: se ignora. Obtenido: la tabla sigue con 9 registros.
5. **POST con un texto de 2001 caracteres.**
   Esperado: lo rechaza. Obtenido: "Máximo 2000 caracteres."
6. **POST con `<script>` en un valor.**
   Esperado: se muestra como texto, no se ejecuta. Obtenido: no aparece ninguna etiqueta `<script>` en la página.

## Prueba de regresión

Después de los cambios internos se volvió a probar el catálogo:

- Crear un producto válido sigue funcionando.
- Las validaciones (referencia duplicada, nombre vacío, precio negativo, imagen faltante) siguen respondiendo igual.
