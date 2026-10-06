// Obtener elementos del DOM
var botonMenu = document.getElementById('botonMenu');
var listaMenu = document.getElementById('listaMenu');
var botonAgregar = document.getElementById('botonAgregar');
var cantidadProducto = document.getElementById('cantidadProducto');
var mensajeFormulario = document.getElementById('mensajeFormulario');
var notificacion = document.getElementById('notificacion');
var carritoTexto = document.getElementById('carritoTexto');
var inputBuscar = document.getElementById('inputBuscar');
var botonBuscar = document.getElementById('botonBuscar');

// Contador para el carrito
var contadorCarrito = 0;

// Funcion para mostrar u ocultar el menu
botonMenu.addEventListener('click', function() {
    if (listaMenu.classList.contains('activo')) {
        listaMenu.classList.remove('activo');
    } else {
        listaMenu.classList.add('activo');
    }
});

// Funcion para mostrar notificacion
function mostrarNotificacion(mensaje) {
    notificacion.textContent = mensaje;
    notificacion.classList.add('mostrar');

    setTimeout(function() {
        notificacion.classList.remove('mostrar');
    }, 3000);
}

// Agregar producto al carrito
botonAgregar.addEventListener('click', function() {
    var cantidad = Number(cantidadProducto.value);

    if (cantidad < 1 || cantidad > 10) {
        mensajeFormulario.textContent = 'Ingresa una cantidad valida entre 1 y 10.';
        return;
    }

    contadorCarrito = contadorCarrito + cantidad;
    carritoTexto.textContent = 'ðŸ›’ Carrito (' + contadorCarrito + ')';
    mensajeFormulario.textContent = 'Agregaste ' + cantidad + ' unidad(es) al carrito.';
    mostrarNotificacion('Producto agregado al carrito');
});

// Funcion simple para buscar
botonBuscar.addEventListener('click', function() {
    var textoBusqueda = inputBuscar.value;

    if (textoBusqueda === '') {
        alert('Por favor escribe algo para buscar');
    } else {
        alert('Buscando: ' + textoBusqueda);
    }
});
