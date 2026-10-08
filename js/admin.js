// --- Control de Interfaz ---
const seccionLogin = document.getElementById('seccion-login');
const seccionDashboard = document.getElementById('seccion-dashboard');
const mensajeLogin = document.getElementById('mensaje-login');

function mostrarModulo(idModulo) {
    // Ocultar todos los módulos
    document.getElementById('modulo-catalogo').classList.add('oculto');
    document.getElementById('modulo-galeria').classList.add('oculto');
    document.getElementById('modulo-informacion').classList.add('oculto');
    
    // Mostrar el seleccionado
    document.getElementById(idModulo).classList.remove('oculto');
}

// --- Simulación de Eventos para las API ---

// 1. API: Login (POST /api/login)
document.getElementById('btn-login').addEventListener('click', function() {
    const usuario = document.getElementById('login-usuario').value;
    const pass = document.getElementById('login-password').value;

    if(usuario === '' || pass === '') {
        mensajeLogin.textContent = 'Por favor ingresa usuario y contraseña.';
        return;
    }

    // Aquí irá el fetch() a la API de login. Por ahora simulamos el acceso:
    seccionLogin.classList.add('oculto');
    seccionDashboard.classList.remove('oculto');
});

document.getElementById('btn-cerrar-sesion').addEventListener('click', function() {
    seccionDashboard.classList.add('oculto');
    seccionLogin.classList.remove('oculto');
    document.getElementById('login-usuario').value = '';
    document.getElementById('login-password').value = '';
});

// 2. API: Crear Producto (POST /api/productos)
document.getElementById('btn-guardar-producto').addEventListener('click', function() {
    // Aquí capturaremos los values de prod-nombre, prod-precio, etc.
    // y armaremos el JSON o FormData para enviar por fetch()
    alert('Preparando datos del mueble para enviar a la API...');
});

// 3. API: Subir Trabajo (POST /api/trabajos)
document.getElementById('btn-guardar-trabajo').addEventListener('click', function() {
    // Verificación de imágenes requerida por el caso de uso
    const imgAntes = document.getElementById('trab-img-antes').files.length;
    const imgDespues = document.getElementById('trab-img-despues').files.length;

    if(imgAntes === 0 || imgDespues === 0) {
        alert('Debes subir ambas imágenes (antes y después) para la comparativa.');
        return;
    }
    
    alert('Imágenes validadas. Listas para enviar a la API...');
});

// 4. API: Actualizar Info (POST o PUT /api/informacion)
document.getElementById('btn-actualizar-info').addEventListener('click', function() {
    alert('Datos de contacto listos para actualizar en la BD...');
});