/**
 * @file Carrito de cotización (RF03 / CU04).
 *
 * Mientras el cliente arma su cotización, esta vive en su navegador
 * (localStorage), igual que lo define el esquema de la BD: la cotización solo
 * se registra en el servidor cuando el cliente la envía por WhatsApp
 * (POST /api/cotizaciones). Así se conserva al cambiar de página y no se
 * llenan las tablas con carritos abandonados.
 *
 * Cada cambio dispara el evento "carrito:cambio" en window, que usan el
 * contador del encabezado y la página de cotización.
 */

const CLAVE = "miura.carrito";

/** Límites iguales a los de la API (CotizacionValidador). */
export const MAXIMO_UNIDADES = 10;
export const MAXIMO_PRODUCTOS = 20;

/**
 * @typedef {object} ItemCarrito
 * @property {number} idProducto
 * @property {number} cantidad
 * @property {string} nombre    copia para mostrar sin consultar la API
 * @property {number} precio    precio de referencia; el definitivo lo calcula el servidor
 * @property {string} imagen
 * @property {string} referencia
 */

/**
 * Lee el carrito guardado. Si el navegador bloquea el almacenamiento o el
 * dato está dañado, se trabaja con un carrito vacío.
 *
 * @returns {ItemCarrito[]}
 */
export function leer() {
    try {
        const datos = JSON.parse(localStorage.getItem(CLAVE) ?? "[]");
        return Array.isArray(datos) ? datos.filter((i) => i && Number.isInteger(i.idProducto)) : [];
    } catch {
        return [];
    }
}

/** Guarda el carrito y avisa a la página del cambio. */
function guardar(items) {
    try {
        localStorage.setItem(CLAVE, JSON.stringify(items));
    } catch {
        // Almacenamiento no disponible (modo privado estricto): el carrito dura solo esta página
    }
    window.dispatchEvent(new CustomEvent("carrito:cambio", { detail: items }));
}

/**
 * Agrega un producto o suma unidades si ya estaba.
 *
 * @param {{idProducto: number, nombre: string, precio: number, imagen: string, referencia: string}} producto
 * @param {number} [cantidad=1]
 * @returns {{ok: boolean, mensaje: string}} resultado para mostrar al usuario
 */
export function agregar(producto, cantidad = 1) {
    const items = leer();
    const existente = items.find((i) => i.idProducto === producto.idProducto);

    if (!existente && items.length >= MAXIMO_PRODUCTOS) {
        return { ok: false, mensaje: `Una cotización admite máximo ${MAXIMO_PRODUCTOS} productos distintos.` };
    }
    const actual = existente?.cantidad ?? 0;
    if (actual >= MAXIMO_UNIDADES) {
        return { ok: false, mensaje: `Ya tiene ${MAXIMO_UNIDADES} unidades de este producto, el máximo por cotización.` };
    }

    const nuevaCantidad = Math.min(actual + cantidad, MAXIMO_UNIDADES);
    if (existente) {
        existente.cantidad = nuevaCantidad;
    } else {
        items.push({
            idProducto: producto.idProducto,
            cantidad: nuevaCantidad,
            nombre: producto.nombre,
            precio: Number(producto.precio),
            imagen: producto.imagen,
            referencia: producto.referencia
        });
    }
    guardar(items);
    const ajustado = nuevaCantidad < actual + cantidad ? ` (máximo ${MAXIMO_UNIDADES} unidades)` : "";
    return { ok: true, mensaje: `Agregado al carrito: ${producto.nombre}${ajustado}` };
}

/**
 * Cambia la cantidad de un producto (entre 1 y el máximo).
 *
 * @param {number} idProducto
 * @param {number} cantidad
 */
export function cambiarCantidad(idProducto, cantidad) {
    const items = leer();
    const item = items.find((i) => i.idProducto === idProducto);
    if (item) {
        item.cantidad = Math.max(1, Math.min(MAXIMO_UNIDADES, Math.trunc(cantidad) || 1));
        guardar(items);
    }
}

/**
 * Actualiza los datos de un producto con lo que respondió la API (precio o
 * nombre pudieron cambiar desde que se agregó).
 *
 * @param {number} idProducto
 * @param {{nombre: string, precio: number, imagen: string, referencia: string}} datos
 */
export function actualizarDatos(idProducto, datos) {
    const items = leer();
    const item = items.find((i) => i.idProducto === idProducto);
    if (item) {
        Object.assign(item, {
            nombre: datos.nombre,
            precio: Number(datos.precio),
            imagen: datos.imagen,
            referencia: datos.referencia
        });
        guardar(items);
    }
}

/**
 * Quita productos del carrito.
 *
 * @param {...number} ids identificadores a quitar
 */
export function quitar(...ids) {
    guardar(leer().filter((i) => !ids.includes(i.idProducto)));
}

/** Vacía el carrito (después de enviar la cotización o a pedido del cliente). */
export function vaciar() {
    guardar([]);
}

/** @returns {number} total de unidades, para el contador del encabezado */
export function totalUnidades() {
    return leer().reduce((suma, i) => suma + i.cantidad, 0);
}
