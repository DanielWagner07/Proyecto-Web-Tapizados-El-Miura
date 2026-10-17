var filtros = document.querySelectorAll('.sidebar-filtros input[type="checkbox"]');
var tarjetas = document.querySelectorAll('.tarjeta-producto');
var resultadoProductos = document.getElementById('resultadoProductos');
var botonLimpiar = document.getElementById('limpiarFiltros');

function obtenerFiltrosActivos() {
    var activos = {};

    filtros.forEach(function(filtro) {
        if (!filtro.checked) {
            return;
        }

        var tipo = filtro.dataset.filter;
        if (!activos[tipo]) {
            activos[tipo] = [];
        }

        activos[tipo].push(filtro.value);
    });

    return activos;
}

function tarjetaCoincide(tarjeta, activos) {
    for (var tipo in activos) {
        if (!activos.hasOwnProperty(tipo)) {
            continue;
        }

        if (activos[tipo].indexOf(tarjeta.dataset[tipo]) === -1) {
            return false;
        }
    }

    return true;
}

function actualizarResultado(total) {
    if (!resultadoProductos) {
        return;
    }

    if (total === 1) {
        resultadoProductos.textContent = '1 producto';
    } else {
        resultadoProductos.textContent = total + ' productos';
    }
}

function aplicarFiltros() {
    var activos = obtenerFiltrosActivos();
    var visibles = 0;

    tarjetas.forEach(function(tarjeta) {
        var mostrar = tarjetaCoincide(tarjeta, activos);
        tarjeta.classList.toggle('oculto', !mostrar);

        if (mostrar) {
            visibles = visibles + 1;
        }
    });

    actualizarResultado(visibles);
}

filtros.forEach(function(filtro) {
    filtro.addEventListener('change', aplicarFiltros);
});

if (botonLimpiar) {
    botonLimpiar.addEventListener('click', function() {
        filtros.forEach(function(filtro) {
            filtro.checked = false;
        });

        aplicarFiltros();
    });
}

actualizarResultado(tarjetas.length);
