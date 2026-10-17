# Tapizados El Miura – Plataforma web

Sitio web comercial de Tapizados El Miura (Caucasia, Antioquia): catálogo de muebles,
galería de restauraciones y panel administrador para gestionar el contenido.

Proyecto web Java (Jakarta EE 10: Servlets + JSP) que se despliega en Apache Tomcat
y usa una base de datos MySQL.

## Requisitos

- JDK 17 o superior
- Apache Tomcat 10.1 u 11
- MySQL 8.0.16 o superior
- Git

Maven no necesita instalarse: `mvnw.cmd` lo descarga la primera vez.

## Puesta en marcha

1. **Base de datos.** Ejecuta en MySQL, en este orden, los scripts del repositorio de la BD:
   - `01_esquema.sql`: crea la base `tapizados_miura` con tablas, restricciones y vistas.
   - `02_datos_prueba.sql`: carga datos de ejemplo y el administrador inicial.
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

Usuario de prueba (creado por `02_datos_prueba.sql`, solo para desarrollo):

- Correo: `admin@tapizadosmiura.com`
- Contraseña: `Miura2026*Cambiar`

## Comandos útiles

- `mvnw.cmd test`: ejecuta las pruebas unitarias.
- `mvnw.cmd clean package`: genera `target/tapizados-miura.war`.

## Estructura del código

```
src/main/java/co/tapizadosmiura/
├── config/        Conexion: abre conexiones JDBC con los datos de db.properties
├── modelo/        Clases de dominio: Producto, Trabajo, InformacionNegocio...
├── dao/           Consultas SQL por tabla (ProductoDAO, TrabajoDAO, InformacionNegocioDAO...)
├── controlador/   Servlets: reciben las peticiones GET/POST y eligen la vista
├── filtro/        AutenticacionFiltro: protege las rutas /admin/*
└── util/          Validación de formularios, contraseñas e imágenes
src/main/webapp/
├── *.html, css/, js/, img/   Sitio público
├── admin/css/                Estilos del panel
└── WEB-INF/
    ├── web.xml               Configuración general (UTF-8, sesión, carpeta de imágenes)
    └── vistas/admin/         Páginas JSP del panel (no accesibles directamente)
src/test/java/                Pruebas unitarias (JUnit 5)
```

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
- Los productos no se eliminan, se ocultan (borrado lógico). Así se conservan las
  cotizaciones que ya los incluyen.

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
