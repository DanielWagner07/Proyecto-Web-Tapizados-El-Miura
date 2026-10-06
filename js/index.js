// Obtener elementos del DOM
var botonMenu = document.getElementById('botonMenu');
var listaMenu = document.getElementById('listaMenu');

// Funcion para mostrar/ocultar menu
botonMenu.addEventListener('click', function() {
    if (listaMenu.classList.contains('activo')) {
        listaMenu.classList.remove('activo');
    } else {
        listaMenu.classList.add('activo');
    }
});


// =====================
// AGREGAR AL CARRITO
// =====================

// Contador de productos en el carrito
var contadorCarrito = 0;

// Funcion para agregar producto al carrito
function agregarCarrito(nombreProducto) {
    // Aumentar contador
    contadorCarrito = contadorCarrito + 1;
    
    // Mostrar mensaje en consola
    console.log('Producto agregado: ' + nombreProducto);
    console.log('Total en carrito: ' + contadorCarrito);
    
    // Mostrar notificacion
    mostrarNotificacion('Se agregó: ' + nombreProducto);
}


// =====================
// NOTIFICACIONES
// =====================

// Funcion para mostrar notificacion
function mostrarNotificacion(mensaje) {
    var notificacion = document.getElementById('notificacion');
    
    // Cambiar el texto
    notificacion.textContent = mensaje;
    
    // Mostrar la notificacion
    notificacion.classList.add('mostrar');
    
    // Ocultar despues de 3 segundos
    setTimeout(function() {
        notificacion.classList.remove('mostrar');
    }, 3000);
}


// =====================
// BUSCADOR
// =====================

// Obtener el input de busqueda
var inputBuscar = document.querySelector('.buscador input');
var botonBuscar = document.querySelector('.buscador button');

// Funcion de busqueda
botonBuscar.addEventListener('click', function() {
    var textoBusqueda = inputBuscar.value;
    
    if (textoBusqueda === '') {
        alert('Por favor escribe algo para buscar');
    } else {
        alert('Buscando: ' + textoBusqueda);
    }
});


// =====================
// EFECTOS DE HOVER EN CATEGORIAS
// =====================

// Obtener todas las tarjetas de categoria
var tarjetasCategorias = document.querySelectorAll('.categoria-card');

// Agregar evento a cada tarjeta
for (var i = 0; i < tarjetasCategorias.length; i++) {
    tarjetasCategorias[i].addEventListener('click', function() {
        var nombreCategoria = this.querySelector('h3').textContent;
        alert('Seleccionaste la categoría: ' + nombreCategoria);
    });
}


// =====================
// SCROLL SUAVE
// =====================

// Cuando la pagina carga
window.addEventListener('load', function() {
    console.log('Página cargada correctamente');
});