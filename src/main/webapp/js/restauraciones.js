// Variables principales para controlar mensajes y busqueda.
var botonContacto = document.getElementById('botonContacto');
var mensajeContacto = document.getElementById('mensajeContacto');
var campoBusqueda = document.getElementById('campoBusqueda');
var botonBuscar = document.getElementById('botonBuscar');
var tarjetasRestauracion = document.querySelectorAll('.tarjeta-restauracion');
var imagenesAmpliables = document.querySelectorAll('.imagen-ampliable');
var visorImagen = document.getElementById('visorImagen');
var imagenAmpliada = document.getElementById('imagenAmpliada');
var cerrarVisor = document.getElementById('cerrarVisor');

// Funcion basica para buscar restauraciones por nombre.
function buscarRestauracion() {
    var textoBuscado = campoBusqueda.value.trim().toLowerCase();
    var coincidencias = 0;

    tarjetasRestauracion.forEach(function(tarjeta) {
        var nombreRestauracion = tarjeta.dataset.nombre;
        var mostrarTarjeta = nombreRestauracion.indexOf(textoBuscado) !== -1;

        tarjeta.style.display = mostrarTarjeta ? 'block' : 'none';

        if (mostrarTarjeta) {
            coincidencias = coincidencias + 1;
        }
    });

    if (textoBuscado === '') {
        mensajeContacto.textContent = '';
    } else if (coincidencias === 0) {
        mensajeContacto.textContent = 'No encontramos una restauracion con ese nombre.';
    } else {
        mensajeContacto.textContent = 'Se encontraron ' + coincidencias + ' restauraciones.';
    }
}

// Evento del boton de busqueda.
if (botonBuscar) {
    botonBuscar.addEventListener('click', buscarRestauracion);
}

// Evento sencillo para mostrar un mensaje de contacto.
if (botonContacto) {
    botonContacto.addEventListener('click', function() {
        mensajeContacto.textContent = 'Gracias. Puedes continuar el proceso por WhatsApp o desde la seccion de contacto.';
    });
}

// Evento para ampliar imagenes al hacer clic.
imagenesAmpliables.forEach(function(imagen) {
    imagen.addEventListener('click', function() {
        imagenAmpliada.src = imagen.src;
        imagenAmpliada.alt = imagen.alt;
        visorImagen.classList.add('activo');
    });
});

// Evento para cerrar el visor de imagenes.
if (cerrarVisor) {
    cerrarVisor.addEventListener('click', function() {
        visorImagen.classList.remove('activo');
    });
}

if (visorImagen) {
    visorImagen.addEventListener('click', function(evento) {
        if (evento.target === visorImagen) {
            visorImagen.classList.remove('activo');
        }
    });
}
