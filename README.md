# Tapizados El Miura – Plataforma web

Sitio web comercial de Tapizados El Miura (Caucasia, Antioquia): catálogo de muebles,
galería de restauraciones, cotizaciones por WhatsApp y panel administrador para gestionar
el contenido.

Proyecto web Java (Jakarta EE 10: Servlets + JSP) que se despliega en Apache Tomcat
y usa una base de datos MySQL. Tiene tres partes:

- **Sitio público** (HTML, CSS y JavaScript): toma todos sus datos de la API con `fetch()`.
  Ver [`docs/frontend.md`](docs/frontend.md).
- **Servicios web** en JSON (`/api/...`). Ver [`docs/API.md`](docs/API.md).
- **Panel administrador** (servlets y JSP) en `/admin/...`.

## Requisitos

- JDK 17 o superior
- Apache Tomcat 10.1 u 11
- MySQL 8.0.16 o superior (o el MariaDB de XAMPP)
- Git
- Postman (opcional, para probar la API)

Maven no necesita instalarse: `mvnw.cmd` lo descarga la primera vez.

## Puesta en marcha

1. **Base de datos.** Los scripts están en la carpeta [`sql/`](sql) del proyecto. Ejecútalos
   en MySQL en este orden (en phpMyAdmin: pestaña **Importar**, un archivo a la vez):
   - `01_esquema (Estructuracion de BD).sql`: crea la base `tapizados_miura` con tablas,
     restricciones y vistas. Borra la base si ya existe, así que no se ejecuta sobre una BD
     con datos reales.
   - `02_datos_prueba (Insercion de Datos Ejemplo).sql`: carga datos de ejemplo y el
     administrador de prueba.
   - `03_datos_faltantes.sql`: agrega los datos que se fueron necesitando después de crear la
     BD. Hoy agrega Facebook y TikTok a la información del negocio y las categorías Sillas
     mecedoras, Juegos de sala y Mesas de centro. Los datos nuevos se agregan al final de este
     mismo archivo. Se puede ejecutar sobre una BD con datos reales: no borra nada y, si los
     datos ya existen, no hace nada.

   Los scripts funcionan en MySQL 8 y en el MariaDB de XAMPP.

   Con los datos de ejemplo, los productos y trabajos se ven con la imagen genérica, porque
   sus fotos no vienen en el repositorio (ver [Imágenes subidas](#imágenes-subidas)).
2. **Conexión.** Revisa usuario y contraseña de MySQL en `src/main/resources/db.properties`.
3. **Tomcat.** Se usa Tomcat 11 en el puerto 8081. El puerto 8080 lo ocupa el Tomcat 8.5
   de XAMPP, que no es compatible con este proyecto. Si tu Tomcat está en otra carpeta,
   ajusta `TOMCAT_DIR` en `desplegar.bat`.
4. **Desplegar.** Ejecuta `desplegar.bat` cada vez que cambies el código. El script:
   - compila y ejecuta las pruebas;
   - copia `tapizados-miura.war` a la carpeta `webapps` de Tomcat;
   - inicia Tomcat si no está corriendo y abre el panel.

   Si no cambiaste el código y solo necesitas encender el servidor (por ejemplo, después
   de reiniciar el computador), usa `iniciar-servidor.bat`: enciende Tomcat 11 y abre el panel.
5. **Abrir:**
   - Sitio público: http://localhost:8081/tapizados-miura/
   - Panel administrador: http://localhost:8081/tapizados-miura/admin/login

Usuario de prueba (creado por el script 02, solo para desarrollo):

- Correo: `admin@tapizadosmiura.com`
- Contraseña: `Miura2026*Cambiar`

## Comandos útiles

- `mvnw.cmd test`: ejecuta las pruebas unitarias (44 pruebas).
- `mvnw.cmd clean package`: genera `target/tapizados-miura.war`.

## Estructura del código

```
src/main/java/co/tapizadosmiura/
├── config/        Conexion: abre conexiones JDBC con los datos de db.properties
├── modelo/        Clases de dominio: Producto, Trabajo, Cotizacion, InformacionNegocio...
├── dao/           Consultas SQL por tabla (ProductoDAO, TrabajoDAO, CotizacionDAO...)
├── controlador/   Servlets del panel: reciben las peticiones GET/POST y eligen la vista JSP
├── api/           Servicios web en JSON (ver docs/API.md)
│   ├── publico/   Catálogo, galería, información y cotizaciones
│   ├── auth/      Inicio y cierre de sesión
│   ├── admin/     Gestión de productos, trabajos, información y cotizaciones
│   └── dto/       Formato de las respuestas JSON
├── filtro/        AutenticacionFiltro (/admin/*) y ApiAutenticacionFiltro (/api/admin/*)
└── util/          Validaciones, contraseñas, imágenes y mensaje de WhatsApp
src/main/webapp/
├── *.html                    Sitio público: index, muebles, producto, cotizacion,
│                             restauraciones, renovaciones, informacion
├── css/                      base.css (común) y una hoja por página
├── js/                       Un script por página y js/comun/ (API, carrito, plantilla...)
├── fonts/, img/              Fuentes Barlow, logo, ícono y la imagen "sin imagen"
├── admin/css/                Estilos del panel
└── WEB-INF/
    ├── web.xml               Configuración general (UTF-8, sesión, carpeta de imágenes)
    └── vistas/admin/         Páginas JSP del panel (no accesibles directamente)
src/test/java/                Pruebas unitarias (JUnit 5)
docs/
├── API.md                                 Documentación de cada servicio web
├── frontend.md                            Estructura y estándares del sitio público
├── TapizadosMiura-API.postman_collection.json   Colección de Postman con pruebas automáticas
├── pruebas-AA2.md                         Registro de pruebas del panel (servlets y JSP)
├── pruebas-AA5.md                         Registro de pruebas de la API
└── pruebas-AA4.md                         Registro de pruebas del sitio público
sql/
├── 01_esquema (Estructuracion de BD).sql            Estructura de la BD
├── 02_datos_prueba (Insercion de Datos Ejemplo).sql Datos de ejemplo
└── 03_datos_faltantes.sql                           Datos agregados después (redes, categorías...)
```

## Sitio público

- **Páginas:**
  - Inicio, catálogo con filtros, ficha de producto y carrito de cotización.
  - Tapizado y restauración, con un comparador de antes y después.
  - Renovaciones para negocios e Información.
- **Datos:** salen de la BD a través de la API; lo que se cambia en el panel se ve al instante.
- **Carrito:** se guarda en el navegador. Al enviarlo, la cotización se registra y se abre
  WhatsApp con el mensaje listo.
- **Abrir el sitio:** siempre desde Tomcat (`http://localhost:8081/tapizados-miura/`), no
  con Live Server, porque las páginas y la API deben estar en el mismo servidor.

Detalles, estructura del código y estándares: [`docs/frontend.md`](docs/frontend.md).

## Servicios web (API)

La API responde en JSON bajo `http://localhost:8081/tapizados-miura/api/`. Resumen:

- **Públicos** (sin sesión):
  - `GET /api/categorias`, `GET /api/servicios` y `GET /api/informacion`.
  - `GET /api/productos?categoria=&q=` y `GET /api/productos/{id}`.
  - `GET /api/trabajos?tipo=` y `GET /api/trabajos/{id}`.
  - `POST /api/cotizaciones`: registra el carrito y devuelve el enlace de WhatsApp.
- **Autenticación:** `POST /api/auth/login`, `POST /api/auth/logout` y `GET /api/auth/sesion`.
- **Administración** (responden 401 sin sesión):
  - `/api/admin/productos`: consultar, crear, editar, y mostrar u ocultar.
  - `/api/admin/trabajos`: consultar, mostrar u ocultar, y eliminar.
  - `/api/admin/informacion`: consultar y actualizar.
  - `/api/admin/cotizaciones`: consultar y cambiar el estado.

Para probarla:

1. Enciende MySQL y Tomcat 11 (`iniciar-servidor.bat`).
2. En Postman, importa `docs/TapizadosMiura-API.postman_collection.json`.
3. Ejecuta la colección completa con el **Runner**. Las peticiones van en orden: la de login
   guarda la sesión para las de administración.

Formato de errores, códigos HTTP, reglas y ejemplos de cada servicio: [`docs/API.md`](docs/API.md).

## Panel administrador

### Acceso


- `GET /admin/login`: muestra el formulario de ingreso.
- `POST /admin/login`: valida correo y contraseña (BCrypt) e inicia la sesión.
- `POST /admin/logout`: cierra la sesión.
- Toda ruta `/admin/*`, salvo el login, exige sesión iniciada.

### Gestor de catálogo (CU07)

- `GET /admin/productos`: lista los productos; `?q=` filtra por nombre o referencia.
- `GET /admin/productos?accion=nuevo`: formulario de producto nuevo.
- `GET /admin/productos?accion=editar&id=N`: formulario con los datos del producto N.
- `POST /admin/productos` con `accion=guardar`: crea o actualiza un producto con su foto.
- `POST /admin/productos` con `accion=estado`: muestra u oculta un producto en el catálogo.

Reglas:

- El precio debe ser mayor que cero y el stock no puede ser negativo.
- La referencia es única.
- La foto es obligatoria al crear un producto: JPG, PNG o WEBP de máximo 5 MB.
- Al cambiar la foto de un producto, el archivo anterior se borra del disco.
- Los productos no se eliminan, se ocultan (borrado lógico). Así se conservan las
  cotizaciones que ya los incluyen.
- Un producto sin foto no puede quedar visible. Solo pasa con los productos creados por la
  API, que nacen ocultos: en el listado aparecen con la etiqueta "Sin foto" y se publican
  después de subirles la foto al editarlos.

### Galería de trabajos (CU08)

- `GET /admin/trabajos`: tarjetas con los trabajos; `?tipo=restauracion` o `?tipo=colaboracion` filtra.
- `GET /admin/trabajos?accion=nuevo`: formulario de trabajo nuevo.
- `GET /admin/trabajos?accion=editar&id=N`: formulario con los datos y fotos del trabajo N.
- `POST /admin/trabajos` con `accion=guardar`: crea o actualiza; recibe fotos de antes, después y adicionales.
- `POST /admin/trabajos` con `accion=estado`: muestra u oculta el trabajo en la galería.
- `POST /admin/trabajos` con `accion=eliminar`: borra el trabajo, sus fotos y los archivos.
- `POST /admin/trabajos` con `accion=eliminarFoto`: quita una foto adicional.

Reglas:

- Una restauración exige foto del antes y del después (RF08).
- Una colaboración puede no tenerlas; muestra sus fotos adicionales.
- Máximo 8 fotos adicionales por trabajo.
- Al reemplazar o quitar una foto, el archivo viejo se borra del disco.

### Información del negocio (CU09)

- `GET /admin/informacion`: formulario con todos los datos, agrupados (Contacto, Horario...).
- `POST /admin/informacion`: valida y guarda todos los datos en una sola transacción.

Reglas:

- Ningún dato puede quedar vacío ni superar 2000 caracteres.
- El WhatsApp se guarda solo con números e indicativo (ej. `573120000000`), como lo exige el enlace `wa.me`.
- El correo debe ser válido y las redes sociales deben ser direcciones que empiecen por `https://`.
- Si un dato tiene error no se guarda ninguno, para no dejar la información a medias.

### Imágenes subidas

`GET /uploads/*` entrega las imágenes subidas desde el panel.

Se guardan fuera del WAR para que no se pierdan al volver a desplegar. Por defecto van a
`<carpeta del usuario>\miura-uploads`; se puede cambiar con el parámetro
`miura.uploads.dir` de `WEB-INF/web.xml`. Si una ruta guardada en la BD no tiene archivo
(como las de los datos de prueba), se muestra `img/sin-imagen.svg`.
