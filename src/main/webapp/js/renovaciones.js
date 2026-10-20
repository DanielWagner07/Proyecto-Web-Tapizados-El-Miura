/**
 * @file Renovaciones para negocios (renovaciones.html), RF05 / CU06.
 *
 * Servicios que usa:
 *   - GET /api/trabajos?tipo=colaboracion  proyectos visibles con sus fotos
 *   - GET /api/informacion                 WhatsApp para el botón de contacto (vía la plantilla)
 */

import { obtener } from "./comun/api.js";
import { crear, enlaceWhatsApp } from "./comun/formato.js";
import { icono } from "./comun/iconos.js";
import { dato, iniciarPlantilla, informacionNegocio } from "./comun/plantilla.js";
import { abrirVisor, miniaturaAmpliable } from "./comun/visor.js";

iniciarPlantilla();
cargarProyectos();
agregarBotonWhatsApp();

async function cargarProyectos() {
    const contenedor = document.getElementById("proyectos");
    contenedor.replaceChildren(crear("div", { clase: "esqueleto proyectos__cargando" }));
    try {
        const proyectos = await obtener("trabajos?tipo=colaboracion");
        if (proyectos.length === 0) {
            contenedor.replaceChildren(crear("p", { clase: "estado", texto: "Pronto publicaremos los proyectos que hemos hecho para negocios." }));
            return;
        }
        contenedor.replaceChildren(...proyectos.map(proyecto));
        if (window.location.hash) {
            document.querySelector(window.location.hash)?.scrollIntoView();
        }
    } catch (error) {
        contenedor.replaceChildren(crear("p", { clase: "estado estado--error", texto: error.message }));
    }
}

/**
 * Un proyecto: título, cliente, servicio, descripción y galería.
 * Si el proyecto también tiene fotos de antes y después, se agregan a la galería.
 */
function proyecto(t) {
    const fotos = [
        t.imagenAntes && { url: t.imagenAntes, textoAlternativo: `${t.titulo}, antes` },
        t.imagenDespues && { url: t.imagenDespues, textoAlternativo: `${t.titulo}, después` },
        ...t.fotos.map((f) => ({ ...f, textoAlternativo: f.textoAlternativo || t.titulo }))
    ].filter(Boolean);

    // Foto principal grande; las demás como miniaturas
    const [principal, ...resto] = fotos.length > 0 ? fotos : [{ url: t.imagenPortada, textoAlternativo: t.titulo }];
    const fotoPrincipal = crear("button", {
        clase: "proyecto__foto",
        atributos: { type: "button", "aria-label": `Ampliar foto: ${principal.textoAlternativo}` }
    }, [crear("img", { atributos: { src: principal.url, alt: "", loading: "lazy" } })]);
    fotoPrincipal.addEventListener("click", () => abrirVisor(principal.url, principal.textoAlternativo));

    const datos = [
        t.cliente && ["Cliente", t.cliente],
        t.servicio && ["Servicio", t.servicio],
        ["Fotos", String(fotos.length)]
    ].filter(Boolean);

    return crear("article", { clase: "proyecto", atributos: { id: `trabajo-${t.idTrabajo}` } }, [
        crear("div", { clase: "proyecto__galeria" }, [
            fotoPrincipal,
            resto.length > 0 && crear("div", { clase: "miniaturas" }, resto.map((f) => miniaturaAmpliable(f)))
        ]),
        crear("div", { clase: "proyecto__texto" }, [
            crear("h2", { clase: "proyecto__titulo", texto: t.titulo }),
            crear("dl", { clase: "proyecto__datos" }, datos.map(([titulo, valor]) => crear("div", {}, [
                crear("dt", { texto: titulo }), crear("dd", { texto: valor })
            ]))),
            t.descripcion && crear("p", { texto: t.descripcion })
        ])
    ]);
}

async function agregarBotonWhatsApp() {
    const numero = dato(await informacionNegocio(), "whatsapp");
    if (!numero) {
        return;
    }
    document.getElementById("llamado").append(crear("a", {
        clase: "boton boton--whatsapp",
        atributos: {
            href: enlaceWhatsApp(numero, "Hola, Tapizados El Miura. Tengo un negocio y quiero renovar el mobiliario."),
            target: "_blank",
            rel: "noopener"
        }
    }, [icono("whatsapp"), crear("span", { texto: "Escribir por WhatsApp" })]));
}
