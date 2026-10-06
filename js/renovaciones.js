// Variables principales del visor de imagenes.
var imagenesZoom = document.querySelectorAll('.imagen-zoom, .columna-imagen img');
var visorImagen = document.getElementById('visorImagen');
var imagenAmpliada = document.getElementById('imagenAmpliada');
var cerrarVisor = document.getElementById('cerrarVisor');

// Funcion para abrir la imagen seleccionada en grande.
function abrirVisor(evento) {
    var imagenSeleccionada = evento.currentTarget;

    imagenAmpliada.src = imagenSeleccionada.src;
    imagenAmpliada.alt = imagenSeleccionada.alt;
    visorImagen.classList.add('activo');
}

// Funcion para cerrar el visor.
function cerrarImagen() {
    visorImagen.classList.remove('activo');
    imagenAmpliada.src = '';
}

// Evento para abrir el zoom en cada imagen.
imagenesZoom.forEach(function(imagen) {
    imagen.addEventListener('click', abrirVisor);
});

// Evento para cerrar desde el boton.
if (cerrarVisor) {
    cerrarVisor.addEventListener('click', cerrarImagen);
}

// Evento para cerrar al hacer clic fuera de la imagen.
if (visorImagen) {
    visorImagen.addEventListener('click', function(evento) {
        if (evento.target === visorImagen) {
            cerrarImagen();
        }
    });
}
