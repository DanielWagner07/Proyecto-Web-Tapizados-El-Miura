/**
 * @file Avisos breves en la parte inferior de la pantalla (por ejemplo
 * "Agregado al carrito"). El contenedor tiene aria-live para que los lectores
 * de pantalla también los anuncien.
 */

import { crear } from "./formato.js";

const DURACION_MS = 4000;

/**
 * Muestra un aviso que desaparece solo.
 *
 * @param {string} mensaje texto del aviso
 * @param {object} [opciones]
 * @param {"normal"|"error"} [opciones.tipo="normal"]
 * @param {{texto: string, href: string}} [opciones.accion] enlace opcional, ej. "Ver carrito"
 */
export function mostrarAviso(mensaje, { tipo = "normal", accion } = {}) {
    let contenedor = document.getElementById("avisos");
    if (!contenedor) {
        contenedor = crear("div", { clase: "avisos", atributos: { id: "avisos", "aria-live": "polite" } });
        document.body.append(contenedor);
    }
    const aviso = crear("div", { clase: `aviso${tipo === "error" ? " aviso--error" : ""}`, atributos: { role: "status" } }, [
        crear("span", { texto: mensaje }),
        accion && crear("a", { texto: accion.texto, atributos: { href: accion.href } })
    ]);
    contenedor.append(aviso);
    setTimeout(() => aviso.remove(), tipo === "error" ? DURACION_MS * 1.5 : DURACION_MS);
}
