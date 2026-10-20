/**
 * @file Tapizado y restauración (restauraciones.html), RF05 / CU06.
 *
 * Servicios que usa:
 *   - GET /api/servicios                   servicios del taller
 *   - GET /api/trabajos?tipo=restauracion  restauraciones visibles con antes, después y fotos
 *   - GET /api/informacion                 WhatsApp para "Solicitar restauración" (vía la plantilla)
 *
 * Cada restauración se muestra con un comparador: el cliente desliza para ver
 * el antes y el después (CU06, paso 3).
 */

import { obtener } from "./comun/api.js";
import { crear, enlaceWhatsApp } from "./comun/formato.js";
import { icono } from "./comun/iconos.js";
import { dato, iniciarPlantilla, informacionNegocio } from "./comun/plantilla.js";
import { miniaturaAmpliable } from "./comun/visor.js";

iniciarPlantilla();
cargarServicios();
cargarRestauraciones();
agregarBotonWhatsApp();

async function cargarServicios() {
    const lista = document.getElementById("servicios");
    try {
        const servicios = await obtener("servicios");
        lista.replaceChildren(...servicios.map((s) => crear("li", { clase: "servicio" }, [
            crear("h2", { clase: "servicio__nombre", texto: s.nombre }),
            s.descripcion && crear("p", { texto: s.descripcion })
        ])));
    } catch {
        lista.remove(); // la galería sigue siendo útil sin esta lista
    }
}

async function cargarRestauraciones() {
    const contenedor = document.getElementById("restauraciones");
    contenedor.replaceChildren(crear("div", { clase: "esqueleto restauraciones__cargando" }));
    try {
        const trabajos = await obtener("trabajos?tipo=restauracion");
        if (trabajos.length === 0) {
            contenedor.replaceChildren(crear("p", { clase: "estado", texto: "Pronto publicaremos nuestras restauraciones." }));
            return;
        }
        contenedor.replaceChildren(...trabajos.map(restauracion));
        // Si se llegó desde el inicio con #trabajo-N, se lleva la vista a ese trabajo
        if (window.location.hash) {
            document.querySelector(window.location.hash)?.scrollIntoView();
        }
    } catch (error) {
        contenedor.replaceChildren(crear("p", { clase: "estado estado--error", texto: error.message }));
    }
}

/** Una restauración: comparador antes/después, datos y fotos adicionales. */
function restauracion(t) {
    return crear("article", { clase: "restauracion", atributos: { id: `trabajo-${t.idTrabajo}` } }, [
        comparador(t),
        crear("div", { clase: "restauracion__texto" }, [
            t.servicio && crear("p", { clase: "restauracion__servicio", texto: t.servicio }),
            crear("h2", { clase: "restauracion__titulo", texto: t.titulo }),
            t.cliente && crear("p", { clase: "restauracion__cliente", texto: `Cliente: ${t.cliente}` }),
            t.descripcion && crear("p", { texto: t.descripcion }),
            t.fotos.length > 0 && crear("div", { clase: "miniaturas" }, t.fotos.map((f) => miniaturaAmpliable(f)))
        ])
    ]);
}

/**
 * Comparador antes/después. La foto del "antes" se recorta según el valor de un
 * control deslizante (input range), que también funciona con el teclado.
 *
 * @param {object} t restauración (siempre trae imagenAntes e imagenDespues, RF08)
 * @returns {HTMLElement}
 */
function comparador(t) {
    const caja = crear("div", { clase: "comparador" }, [
        crear("img", { clase: "comparador__despues", atributos: { src: t.imagenDespues, alt: `${t.titulo}, después de la restauración`, loading: "lazy" } }),
        crear("img", { clase: "comparador__antes", atributos: { src: t.imagenAntes, alt: `${t.titulo}, antes de la restauración`, loading: "lazy" } }),
        crear("span", { clase: "comparador__etiqueta comparador__etiqueta--antes", texto: "Antes", atributos: { "aria-hidden": "true" } }),
        crear("span", { clase: "comparador__etiqueta comparador__etiqueta--despues", texto: "Después", atributos: { "aria-hidden": "true" } }),
        crear("span", { clase: "comparador__linea", atributos: { "aria-hidden": "true" } })
    ]);
    const control = crear("input", {
        clase: "comparador__control",
        atributos: { type: "range", min: "0", max: "100", value: "50", "aria-label": `Comparar antes y después: ${t.titulo}` }
    });
    const mover = () => caja.style.setProperty("--corte", `${control.value}%`);
    control.addEventListener("input", mover);
    mover();
    caja.append(control);
    return caja;
}

/** Botón "Solicitar restauración" con un mensaje listo para WhatsApp. */
async function agregarBotonWhatsApp() {
    const numero = dato(await informacionNegocio(), "whatsapp");
    if (!numero) {
        return;
    }
    document.getElementById("llamado").append(crear("a", {
        clase: "boton boton--whatsapp",
        atributos: {
            href: enlaceWhatsApp(numero, "Hola, Tapizados El Miura. Quiero cotizar la restauración de un mueble. Les envío fotos."),
            target: "_blank",
            rel: "noopener"
        }
    }, [icono("whatsapp"), crear("span", { texto: "Solicitar restauración" })]));
}
