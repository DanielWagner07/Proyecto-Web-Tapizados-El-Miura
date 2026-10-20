/**
 * @file Carrito de cotización (cotizacion.html), RF03 / RF04, CU04 / CU05.
 *
 * Servicios que usa:
 *   - GET  /api/productos/{id}  al abrir la página, para tener precio, nombre y
 *                               disponibilidad actuales de cada mueble del carrito
 *   - POST /api/cotizaciones    registra la cotización y devuelve el enlace de WhatsApp
 *
 * El total que se muestra es una estimación con los precios de la API; el
 * servidor vuelve a calcularlo al registrar la cotización (nunca confía en
 * el precio que envía el navegador).
 */

import { ErrorApi, enviar, obtener } from "./comun/api.js";
import { mostrarAviso } from "./comun/avisos.js";
import * as carrito from "./comun/carrito.js";
import { crear, pesos, plural } from "./comun/formato.js";
import { iniciarPlantilla } from "./comun/plantilla.js";

/** Última cotización enviada: se muestra al volver de WhatsApp a esta pestaña. */
const CLAVE_ULTIMA = "miura.ultimaCotizacion";

/** Disponibilidad actual por producto (id → {stock, agotado}), consultada a la API. */
const disponibilidad = new Map();

const elementos = {
    lista: document.getElementById("lista"),
    conteo: document.getElementById("conteo"),
    enviar: document.getElementById("enviar"),
    vaciar: document.getElementById("vaciar"),
    productos: document.getElementById("resumen-productos"),
    unidades: document.getElementById("resumen-unidades"),
    total: document.getElementById("resumen-total"),
    notaFabricacion: document.getElementById("nota-fabricacion"),
    confirmacion: document.getElementById("confirmacion"),
    carrito: document.getElementById("carrito")
};

iniciarPlantilla();
mostrarUltimaCotizacion();
dibujar();
actualizarDesdeApi();

elementos.enviar.addEventListener("click", enviarCotizacion);
elementos.vaciar.addEventListener("click", () => {
    carrito.vaciar();
    mostrarAviso("Carrito vacío.");
});
// Otra pestaña del sitio pudo cambiar el carrito
window.addEventListener("storage", dibujar);

// ---------------------------------------------------------------------
// Datos actuales
// ---------------------------------------------------------------------

/**
 * Consulta cada producto del carrito. Si alguno ya no está disponible (404),
 * se retira y se avisa; si cambió de precio o nombre, se actualiza.
 */
async function actualizarDesdeApi() {
    const items = carrito.leer();
    if (items.length === 0) {
        return;
    }
    const resultados = await Promise.allSettled(items.map((i) => obtener(`productos/${i.idProducto}`)));
    const retirados = [];
    resultados.forEach((resultado, posicion) => {
        const item = items[posicion];
        if (resultado.status === "fulfilled") {
            const p = resultado.value;
            disponibilidad.set(p.idProducto, { stock: p.stock, agotado: p.agotado });
            carrito.actualizarDatos(p.idProducto, p);
        } else if (resultado.reason instanceof ErrorApi && resultado.reason.estado === 404) {
            retirados.push(item);
        }
        // Otros errores (sin conexión): se conserva el dato guardado
    });
    if (retirados.length > 0) {
        carrito.quitar(...retirados.map((i) => i.idProducto));
        mostrarAviso(`Ya no está disponible y se quitó del carrito: ${retirados.map((i) => i.nombre).join(", ")}.`, { tipo: "error" });
    }
    dibujar();
}

// ---------------------------------------------------------------------
// Dibujo del carrito
// ---------------------------------------------------------------------

function dibujar() {
    const items = carrito.leer();
    const unidades = items.reduce((s, i) => s + i.cantidad, 0);
    const total = items.reduce((s, i) => s + i.precio * i.cantidad, 0);

    elementos.conteo.textContent = items.length ? `(${plural(items.length, "mueble")})` : "";
    elementos.productos.textContent = String(items.length);
    elementos.unidades.textContent = String(unidades);
    elementos.total.textContent = pesos(total);
    // CU05 flujo alterno 2a: sin productos no se puede enviar
    elementos.enviar.disabled = items.length === 0;
    elementos.vaciar.hidden = items.length === 0;
    elementos.notaFabricacion.hidden = !items.some((i) => requiereFabricacion(i));
    elementos.carrito.classList.toggle("carrito--vacio", items.length === 0);

    if (items.length === 0) {
        elementos.lista.replaceChildren(crear("li", { clase: "estado carrito__vacio" }, [
            crear("h2", { texto: "Tu carrito está vacío" }),
            crear("p", { texto: "Agrega los muebles que te interesan desde el catálogo y aquí verás el total estimado." }),
            crear("a", { clase: "boton boton--principal", texto: "Ver muebles", atributos: { href: "muebles.html" } })
        ]));
        return;
    }
    elementos.lista.replaceChildren(...items.map(filaProducto));
}

/** @returns {boolean} true si se piden más unidades de las que hay en stock */
function requiereFabricacion(item) {
    const d = disponibilidad.get(item.idProducto);
    return Boolean(d) && item.cantidad > d.stock;
}

/** Una fila del carrito: foto, nombre, precio, cantidad, subtotal y quitar. */
function filaProducto(item) {
    const enlace = `producto.html?id=${item.idProducto}`;
    const d = disponibilidad.get(item.idProducto);

    let aviso = null;
    if (d?.agotado) {
        aviso = crear("p", { clase: "fila__aviso", texto: "Agotado: se fabrica bajo pedido" });
    } else if (d && item.cantidad > d.stock) {
        aviso = crear("p", { clase: "fila__aviso", texto: `Hay ${plural(d.stock, "unidad", "unidades")}; el resto se fabrica bajo pedido` });
    }

    const campo = crear("input", {
        atributos: {
            type: "number", min: "1", max: String(carrito.MAXIMO_UNIDADES), value: String(item.cantidad),
            inputmode: "numeric", "aria-label": `Cantidad de ${item.nombre}`
        }
    });
    const menos = crear("button", { texto: "−", atributos: { type: "button", "aria-label": `Quitar una unidad de ${item.nombre}`, disabled: item.cantidad <= 1 } });
    const mas = crear("button", { texto: "+", atributos: { type: "button", "aria-label": `Agregar una unidad de ${item.nombre}`, disabled: item.cantidad >= carrito.MAXIMO_UNIDADES } });
    menos.addEventListener("click", () => cambiar(item, item.cantidad - 1));
    mas.addEventListener("click", () => cambiar(item, item.cantidad + 1));
    campo.addEventListener("change", () => cambiar(item, Number(campo.value)));

    const quitar = crear("button", { clase: "boton boton--texto", texto: "Quitar", atributos: { type: "button", "aria-label": `Quitar ${item.nombre} del carrito` } });
    quitar.addEventListener("click", () => {
        carrito.quitar(item.idProducto);
        mostrarAviso(`Se quitó del carrito: ${item.nombre}`);
    });

    return crear("li", { clase: "fila" }, [
        crear("a", { clase: "fila__foto", atributos: { href: enlace, tabindex: "-1", "aria-hidden": "true" } }, [
            crear("img", { atributos: { src: item.imagen, alt: "", loading: "lazy" } })
        ]),
        crear("div", { clase: "fila__datos" }, [
            crear("a", { clase: "fila__nombre", texto: item.nombre, atributos: { href: enlace } }),
            crear("p", { clase: "fila__ref", texto: `Ref. ${item.referencia}` }),
            crear("p", { clase: "fila__ref", texto: `Precio por unidad: ${pesos(item.precio)}` }),
            aviso
        ]),
        crear("div", { clase: "fila__cantidad" }, [crear("div", { clase: "cantidad" }, [menos, campo, mas]), quitar]),
        crear("p", { clase: "fila__subtotal", texto: pesos(item.precio * item.cantidad) })
    ]);
}

function cambiar(item, cantidad) {
    carrito.cambiarCantidad(item.idProducto, cantidad);
}

// El carrito se vuelve a dibujar con cada cambio
window.addEventListener("carrito:cambio", dibujar);

// ---------------------------------------------------------------------
// Envío (RF04 / CU05)
// ---------------------------------------------------------------------

async function enviarCotizacion() {
    const items = carrito.leer();
    if (items.length === 0) {
        mostrarAviso("Carrito vacío: agrega al menos un mueble.", { tipo: "error" });
        return;
    }
    elementos.enviar.disabled = true;
    elementos.enviar.textContent = "Enviando…";
    try {
        const respuesta = await enviar("cotizaciones", {
            items: items.map((i) => ({ idProducto: i.idProducto, cantidad: i.cantidad }))
        });
        const cotizacion = respuesta.datos;
        guardarUltima(cotizacion);
        carrito.vaciar();
        mostrarUltimaCotizacion();
        // CU05 paso 3: abre WhatsApp con el mensaje listo
        window.location.assign(cotizacion.enlaceWhatsapp);
    } catch (error) {
        manejarErrorEnvio(error);
    } finally {
        elementos.enviar.textContent = "Enviar cotización por WhatsApp";
        dibujar();
    }
}

/**
 * Si la API rechaza productos que ya no están disponibles, los retira del
 * carrito para que el cliente pueda volver a enviar.
 */
function manejarErrorEnvio(error) {
    const ids = Object.keys(error.errores ?? {})
        .map((clave) => /^producto (\d+)$/.exec(clave)?.[1])
        .filter(Boolean)
        .map(Number);
    if (ids.length > 0) {
        const nombres = carrito.leer().filter((i) => ids.includes(i.idProducto)).map((i) => i.nombre);
        carrito.quitar(...ids);
        mostrarAviso(`Se quitaron del carrito porque ya no están disponibles: ${nombres.join(", ")}. Revisa y envía de nuevo.`, { tipo: "error" });
        return;
    }
    mostrarAviso(error.message, { tipo: "error" });
}

// ---------------------------------------------------------------------
// Confirmación de la última cotización
// ---------------------------------------------------------------------

function guardarUltima(cotizacion) {
    try {
        sessionStorage.setItem(CLAVE_ULTIMA, JSON.stringify({
            idCotizacion: cotizacion.idCotizacion,
            enlace: cotizacion.enlaceWhatsapp,
            total: cotizacion.total
        }));
    } catch {
        // Sin almacenamiento: solo se pierde el recordatorio
    }
}

/** Al volver de WhatsApp, recuerda qué cotización se envió y permite abrir el chat de nuevo. */
function mostrarUltimaCotizacion() {
    let ultima = null;
    try {
        ultima = JSON.parse(sessionStorage.getItem(CLAVE_ULTIMA) ?? "null");
    } catch {
        ultima = null;
    }
    if (!ultima) {
        elementos.confirmacion.hidden = true;
        return;
    }
    const cerrar = crear("button", { clase: "boton boton--texto", texto: "Cerrar", atributos: { type: "button" } });
    cerrar.addEventListener("click", () => {
        sessionStorage.removeItem(CLAVE_ULTIMA);
        elementos.confirmacion.hidden = true;
    });
    elementos.confirmacion.className = "confirmacion";
    elementos.confirmacion.replaceChildren(
        crear("div", {}, [
            crear("h2", { texto: `Cotización N.º ${ultima.idCotizacion} registrada` }),
            crear("p", { texto: `Total estimado: ${pesos(ultima.total)}. Si no se abrió WhatsApp o no enviaste el mensaje, ábrelo de nuevo.` })
        ]),
        crear("div", { clase: "confirmacion__acciones" }, [
            crear("a", { clase: "boton boton--whatsapp", texto: "Abrir WhatsApp", atributos: { href: ultima.enlace } }),
            cerrar
        ])
    );
    elementos.confirmacion.hidden = false;
}
