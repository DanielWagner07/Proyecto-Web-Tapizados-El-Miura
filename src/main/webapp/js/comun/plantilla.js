/**
 * @file Encabezado, pie de página y botón de WhatsApp comunes a todo el sitio.
 *
 * Se escriben una sola vez aquí y cada página los inserta llamando a
 * iniciarPlantilla(). Antes, cada HTML repetía su propio encabezado y era
 * fácil que quedaran diferentes.
 *
 * También expone los datos del negocio (GET /api/informacion), que el pie y
 * varias páginas necesitan, para pedirlos una sola vez por página.
 */

import { obtener } from "./api.js";
import * as carrito from "./carrito.js";
import { crear, enlaceWhatsApp, parametroUrl } from "./formato.js";
import { icono } from "./iconos.js";

/** Páginas del menú principal, en orden. La clave coincide con data-pagina del <body>. */
const MENU = [
    { clave: "muebles", texto: "Muebles", href: "muebles.html" },
    { clave: "restauraciones", texto: "Tapizado y restauración", href: "restauraciones.html" },
    { clave: "renovaciones", texto: "Renovaciones", href: "renovaciones.html" },
    { clave: "informacion", texto: "Información", href: "informacion.html" }
];

const LOGO = "img/logo-miura.png";
const NOMBRE_NEGOCIO = "Tapizados El Miura";
const MENSAJE_GENERAL = "Hola, Tapizados El Miura. Quiero más información.";

let promesaInformacion = null;

/**
 * Datos del negocio indexados por clave (whatsapp, telefono, correo...).
 * Se piden a la API una sola vez por página; si falla, se devuelve un objeto
 * vacío para que la página siga funcionando.
 *
 * @returns {Promise<Object<string, {etiqueta: string, valor: string, grupo: string}>>}
 */
export function informacionNegocio() {
    if (!promesaInformacion) {
        promesaInformacion = obtener("informacion").catch(() => ({}));
    }
    return promesaInformacion;
}

/**
 * Valor de un dato del negocio, o cadena vacía si no existe.
 *
 * @param {object} info resultado de informacionNegocio()
 * @param {string} clave ej. "whatsapp"
 * @returns {string}
 */
export function dato(info, clave) {
    return info?.[clave]?.valor ?? "";
}

/**
 * Inserta el encabezado, el pie y el botón flotante de WhatsApp.
 * La página activa del menú se toma del atributo data-pagina del <body>.
 */
export function iniciarPlantilla() {
    const paginaActual = document.body.dataset.pagina ?? "";
    const encabezado = document.getElementById("encabezado");
    const pie = document.getElementById("pie");
    if (encabezado) {
        dibujarEncabezado(encabezado, paginaActual);
    }
    informacionNegocio().then((info) => {
        if (pie) {
            dibujarPie(pie, info);
        }
        dibujarWhatsAppFlotante(info);
    });
}

// ---------------------------------------------------------------------
// Encabezado
// ---------------------------------------------------------------------

function dibujarEncabezado(contenedor, paginaActual) {
    const botonMenu = crear("button", {
        clase: "icono-boton encabezado__menu-boton",
        atributos: { type: "button", "aria-expanded": "false", "aria-controls": "navegacion", "aria-label": "Abrir menú" }
    }, [icono("menu")]);

    const logo = crear("a", { clase: "encabezado__logo", atributos: { href: "index.html" } }, [
        crear("img", { atributos: { src: LOGO, alt: `${NOMBRE_NEGOCIO}, ir al inicio`, width: "210", height: "97" } })
    ]);

    // El buscador lleva al catálogo con ?q=texto (lo resuelve GET /api/productos?q=)
    const campo = crear("input", {
        atributos: {
            type: "search", name: "q", placeholder: "Busca sofás, comedores, mecedoras…",
            "aria-label": "Buscar muebles", maxlength: "100", value: parametroUrl("q")
        }
    });
    const buscador = crear("form", { clase: "buscador", atributos: { action: "muebles.html", method: "get", role: "search" } }, [
        campo,
        crear("button", { atributos: { type: "submit", "aria-label": "Buscar" } }, [icono("buscar")])
    ]);

    const contador = crear("span", { clase: "carrito-enlace__contador", atributos: { "aria-hidden": "true" } });
    const textoCarrito = crear("span", { clase: "carrito-enlace__texto", texto: "Carrito" });
    const enlaceCarrito = crear("a", {
        clase: "icono-boton carrito-enlace encabezado__carrito",
        atributos: { href: "cotizacion.html" }
    }, [icono("carrito"), textoCarrito, contador]);

    const lista = crear("ul", {}, MENU.map((opcion) => crear("li", {}, [
        crear("a", {
            texto: opcion.texto,
            atributos: { href: opcion.href, "aria-current": opcion.clave === paginaActual ? "page" : null }
        })
    ])));
    const navegacion = crear("nav", { clase: "navegacion", atributos: { id: "navegacion", "aria-label": "Menú principal" } }, [
        crear("div", { clase: "contenedor" }, [lista])
    ]);

    contenedor.replaceChildren(
        crear("div", { clase: "contenedor encabezado__fila" }, [botonMenu, logo, buscador, enlaceCarrito]),
        navegacion
    );

    botonMenu.addEventListener("click", () => {
        const abierto = navegacion.classList.toggle("navegacion--abierta");
        botonMenu.setAttribute("aria-expanded", String(abierto));
        botonMenu.setAttribute("aria-label", abierto ? "Cerrar menú" : "Abrir menú");
        botonMenu.replaceChildren(icono(abierto ? "cerrar" : "menu"));
    });

    // Contador del carrito: se actualiza con los cambios de esta pestaña y de otras
    const actualizarContador = () => {
        const unidades = carrito.totalUnidades();
        contador.textContent = String(unidades);
        contador.hidden = unidades === 0;
        enlaceCarrito.setAttribute("aria-label", unidades === 0
            ? "Carrito de cotización, vacío"
            : `Carrito de cotización, ${unidades} ${unidades === 1 ? "unidad" : "unidades"}`);
    };
    actualizarContador();
    window.addEventListener("carrito:cambio", actualizarContador);
    window.addEventListener("storage", actualizarContador);
}

// ---------------------------------------------------------------------
// Pie de página
// ---------------------------------------------------------------------

function dibujarPie(contenedor, info) {
    const marca = crear("div", {}, [
        crear("a", { clase: "pie__logo", atributos: { href: "index.html" } }, [
            crear("img", { atributos: { src: LOGO, alt: NOMBRE_NEGOCIO, width: "104", height: "48", loading: "lazy" } })
        ]),
        crear("p", { texto: "Taller de tapicería, restauración y fabricación de muebles." })
    ]);

    const sitio = crear("div", {}, [
        crear("h2", { texto: "El sitio" }),
        crear("ul", {}, [{ texto: "Inicio", href: "index.html" }, ...MENU, { texto: "Carrito de cotización", href: "cotizacion.html" }]
            .map((o) => crear("li", {}, [crear("a", { texto: o.texto, atributos: { href: o.href } })])))
    ]);

    const contacto = crear("div", {}, [
        crear("h2", { texto: "Contacto" }),
        crear("ul", {}, datosContacto(info).map(({ nombreIcono, texto, href }) => crear("li", { clase: "pie__dato" }, [
            icono(nombreIcono),
            href ? crear("a", { texto, atributos: { href } }) : crear("span", { texto })
        ])))
    ]);

    const redes = redesSociales(info);
    const siguenos = crear("div", {}, [
        crear("h2", { texto: "Síguenos" }),
        redes.length > 0
            ? crear("div", { clase: "pie__redes" }, redes.map((red) => crear("a", {
                atributos: { href: red.url, target: "_blank", rel: "noopener", "aria-label": red.nombre }
            }, [icono(red.icono)])))
            : crear("p", { texto: "Pronto en redes sociales." })
    ]);

    contenedor.replaceChildren(
        crear("div", { clase: "contenedor pie__columnas" }, [marca, sitio, contacto, siguenos]),
        crear("div", { clase: "contenedor pie__final" }, [
            crear("p", { texto: `© ${new Date().getFullYear()} ${NOMBRE_NEGOCIO}. Caucasia, Antioquia.` })
        ])
    );
}

/**
 * Datos de contacto con su ícono y enlace (WhatsApp, llamada, correo, mapa).
 * Solo se incluyen los que existen en la BD.
 *
 * @param {object} info datos del negocio
 * @returns {Array<{nombreIcono: string, texto: string, href?: string}>}
 */
export function datosContacto(info) {
    const datos = [];
    const whatsapp = dato(info, "whatsapp");
    const telefono = dato(info, "telefono");
    const correo = dato(info, "correo");
    const direccion = dato(info, "direccion");
    const horario = dato(info, "horario");
    if (whatsapp) {
        datos.push({ nombreIcono: "whatsapp", texto: `WhatsApp ${formatoCelular(whatsapp)}`, href: enlaceWhatsApp(whatsapp, MENSAJE_GENERAL) });
    }
    if (telefono) {
        datos.push({ nombreIcono: "telefono", texto: telefono, href: `tel:${telefono.replace(/[^\d+]/g, "")}` });
    }
    if (correo) {
        datos.push({ nombreIcono: "correo", texto: correo, href: `mailto:${correo}` });
    }
    if (direccion) {
        datos.push({ nombreIcono: "ubicacion", texto: direccion, href: `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(direccion)}` });
    }
    if (horario) {
        datos.push({ nombreIcono: "reloj", texto: horario });
    }
    return datos;
}

/**
 * Redes sociales registradas en la BD (grupo "redes"), con su ícono.
 *
 * @param {object} info datos del negocio
 * @returns {Array<{nombre: string, url: string, icono: string}>}
 */
export function redesSociales(info) {
    const conocidas = { instagram: "instagram", facebook: "facebook", tiktok: "tiktok" };
    return Object.entries(info ?? {})
        .filter(([, d]) => d.grupo === "redes" && d.valor)
        .map(([clave, d]) => ({ nombre: d.etiqueta, url: d.valor, icono: conocidas[clave] ?? "ruta" }));
}

/** 573128628147 → "+57 312 862 8147" (solo para mostrar). */
export function formatoCelular(numero) {
    const m = /^(57)(\d{3})(\d{3})(\d{4})$/.exec(numero);
    return m ? `+${m[1]} ${m[2]} ${m[3]} ${m[4]}` : `+${numero}`;
}

// ---------------------------------------------------------------------
// Botón flotante de WhatsApp
// ---------------------------------------------------------------------

function dibujarWhatsAppFlotante(info) {
    const numero = dato(info, "whatsapp");
    if (!numero || document.querySelector(".whatsapp-flotante")) {
        return;
    }
    document.body.append(crear("a", {
        clase: "whatsapp-flotante",
        atributos: {
            href: enlaceWhatsApp(numero, MENSAJE_GENERAL),
            target: "_blank",
            rel: "noopener",
            "aria-label": `Escribir a ${NOMBRE_NEGOCIO} por WhatsApp`
        }
    }, [icono("whatsapp")]));
}
