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
3. **Tomcat.** Abre `desplegar.bat` y ajusta `TOMCAT_DIR` a la carpeta de tu Tomcat.
   `desplegar.bat` inicia Tomcat 11 en el puerto 8081 (el 8080 lo usa el Tomcat 8.5 de XAMPP, que no es compatible).
4. **Desplegar.** Ejecuta `desplegar.bat`. El script compila, ejecuta las pruebas y copia
   `tapizados-miura.war` a la carpeta `webapps` de Tomcat.
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
├── modelo/        Clases de dominio: Producto, Categoria, Administrador
├── dao/           Consultas SQL por tabla (ProductoDAO, CategoriaDAO, AdministradorDAO)
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

## Panel administrador: módulo de catálogo

- `GET /admin/login`: muestra el formulario de ingreso.
- `POST /admin/login`: valida correo y contraseña (BCrypt) e inicia la sesión.
- `POST /admin/logout`: cierra la sesión.
- `GET /admin/productos`: lista los productos; `?q=` filtra por nombre o referencia.
- `GET /admin/productos?accion=nuevo`: formulario de producto nuevo.
- `GET /admin/productos?accion=editar&id=N`: formulario con los datos del producto N.
- `POST /admin/productos` con `accion=guardar`: crea o actualiza un producto con su foto.
- `POST /admin/productos` con `accion=estado`: muestra u oculta un producto en el catálogo.
- `GET /uploads/*`: entrega las imágenes subidas desde el panel.

Reglas que aplica:

- Toda ruta `/admin/*`, salvo el login, exige sesión iniciada.
- El precio debe ser mayor que cero y el stock no puede ser negativo.
- La referencia es única.
- La foto es obligatoria al crear un producto: JPG, PNG o WEBP de máximo 5 MB.
- Los productos no se eliminan, se ocultan (borrado lógico). Así se conservan las
  cotizaciones que ya los incluyen.

### Imágenes subidas

Se guardan fuera del WAR para que no se pierdan al volver a desplegar. Por defecto van a
`<carpeta del usuario>\miura-uploads`; se puede cambiar con el parámetro
`miura.uploads.dir` de `WEB-INF/web.xml`. Si una ruta guardada en la BD no tiene archivo
(como las de los datos de prueba), se muestra `img/sin-imagen.svg`.
