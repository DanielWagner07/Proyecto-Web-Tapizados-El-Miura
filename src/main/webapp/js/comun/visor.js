/**
 * @file Visor para ampliar una foto (galerías de trabajos y detalle de producto).
 *
 * Usa el elemento nativo <dialog>: se cierra con Esc, con el botón o haciendo
 * clic fuera de la imagen, y devuelve el foco al elemento que lo abrió.
 */

import { crear } from "./formato.js";

let dialogo = null;
let imagen = null;
let leyenda = null;

/** Crea el diálogo la primera vez que se necesita. */
function preparar() {
    if (dialogo) {
        return;
    }
    imagen = crear("img", { atributos: { alt: "" } });
    leyenda = crear("span");
    const cerrar = crear("button", { clase: "visor__cerrar", texto: "Cerrar", atributos: { type: "button" } });
    dialogo = crear("dialog", { clase: "visor", atributos: { "aria-label": "Foto ampliada" } }, [
        imagen,
        crear("div", { clase: "visor__pie" }, [leyenda, cerrar])
    ]);
    cerrar.addEventListener("click", () => dialogo.close());
    // Clic en el fondo oscuro (fuera del contenido) también cierra
    dialogo.addEventListener("click", (evento) => {
        if (evento.target === dialogo) {
            dialogo.close();
        }
    });
    document.body.append(dialogo);
}

/**
 * Muestra una foto ampliada.
 *
 * @param {string} url dirección de la foto
 * @param {string} [descripcion] texto alternativo y leyenda
 */
export function abrirVisor(url, descripcion = "") {
    preparar();
    imagen.src = url;
    imagen.alt = descripcion;
    leyenda.textContent = descripcion;
    dialogo.showModal();
}

/**
 * Crea una miniatura que abre la foto en el visor.
 *
 * @param {{url: string, textoAlternativo?: string}} foto
 * @param {string} [clase] clase CSS del botón
 * @returns {HTMLButtonElement}
 */
export function miniaturaAmpliable(foto, clase = "miniatura") {
    const descripcion = foto.textoAlternativo ?? "";
    const boton = crear("button", {
        clase,
        atributos: { type: "button", "aria-label": `Ampliar foto${descripcion ? `: ${descripcion}` : ""}` }
    }, [crear("img", { atributos: { src: foto.url, alt: "", loading: "lazy" } })]);
    boton.addEventListener("click", () => abrirVisor(foto.url, descripcion));
    return boton;
}
