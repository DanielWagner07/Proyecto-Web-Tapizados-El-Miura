/**
 * @file Utilidades de presentación compartidas: precios, creación segura de
 * elementos HTML y enlaces de WhatsApp.
 */

const FORMATO_PESOS = new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0
});

/**
 * Precio en pesos colombianos, ej. 1999999 → "$ 1.999.999".
 *
 * @param {number} valor
 * @returns {string}
 */
export function pesos(valor) {
    return FORMATO_PESOS.format(Number(valor) || 0);
}

/**
 * Crea un elemento HTML de forma segura.
 *
 * El texto se asigna con textContent, nunca como HTML: así un dato que venga
 * de la base de datos no puede inyectar código en la página (protección XSS).
 *
 * @param {string} etiqueta nombre de la etiqueta, ej. "p"
 * @param {object} [opciones]
 * @param {string} [opciones.clase] clases CSS
 * @param {string|number} [opciones.texto] contenido de texto
 * @param {Object<string, string>} [opciones.atributos] atributos (href, src, alt...)
 * @param {Array<Node|null|false>} [hijos] elementos hijos (los vacíos se ignoran)
 * @returns {HTMLElement}
 */
export function crear(etiqueta, { clase, texto, atributos } = {}, hijos = []) {
    const elemento = document.createElement(etiqueta);
    if (clase) {
        elemento.className = clase;
    }
    if (texto !== undefined && texto !== null) {
        elemento.textContent = String(texto);
    }
    if (atributos) {
        for (const [nombre, valor] of Object.entries(atributos)) {
            if (valor !== undefined && valor !== null && valor !== false) {
                elemento.setAttribute(nombre, valor === true ? "" : String(valor));
            }
        }
    }
    hijos.filter(Boolean).forEach((hijo) => elemento.append(hijo));
    return elemento;
}

/**
 * Enlace de WhatsApp con un mensaje escrito. Los espacios se codifican como %20
 * (encodeURIComponent), que WhatsApp interpreta correctamente (RNF08).
 *
 * @param {string} numero número con indicativo, solo dígitos (ej. 573128628147)
 * @param {string} mensaje texto que aparecerá escrito en el chat
 * @returns {string}
 */
export function enlaceWhatsApp(numero, mensaje) {
    return `https://wa.me/${numero}?text=${encodeURIComponent(mensaje)}`;
}

/**
 * Lee un parámetro de la dirección de la página, ej. "id" en producto.html?id=7.
 *
 * @param {string} nombre
 * @returns {string} el valor sin espacios sobrantes, o cadena vacía
 */
export function parametroUrl(nombre) {
    return (new URLSearchParams(window.location.search).get(nombre) ?? "").trim();
}

/**
 * Plural sencillo en español: plural(3, "producto") → "3 productos".
 *
 * @param {number} cantidad
 * @param {string} singular
 * @param {string} [plural] forma plural si no termina en "s" al agregarla
 * @returns {string}
 */
export function plural(cantidad, singular, plural = `${singular}s`) {
    return `${cantidad} ${cantidad === 1 ? singular : plural}`;
}
