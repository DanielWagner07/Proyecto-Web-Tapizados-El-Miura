/**
 * @file Información y contacto (informacion.html), RF09 / CU09.
 *
 * Servicios que usa:
 *   - GET /api/informacion  trayectoria, contacto, horario, cobertura, garantía y redes
 *   - GET /api/servicios    servicios del taller
 *
 * Todo lo que se ve aquí se cambia desde el panel (Información del negocio),
 * sin tocar el código.
 */

import { obtener } from "./comun/api.js";
import { crear, enlaceWhatsApp } from "./comun/formato.js";
import { icono } from "./comun/iconos.js";
import {
    dato, datosContacto, formatoCelular, iniciarPlantilla, informacionNegocio, redesSociales
} from "./comun/plantilla.js";

iniciarPlantilla();
cargar();

async function cargar() {
    const [info, servicios] = await Promise.all([
        informacionNegocio(),
        obtener("servicios").catch(() => [])
    ]);
    if (Object.keys(info).length === 0) {
        document.getElementById("trayectoria").replaceChildren(crear("p", {
            clase: "estado estado--error",
            texto: "No fue posible cargar la información del negocio. Intente de nuevo en unos minutos."
        }));
        return;
    }
    mostrarTrayectoria(info);
    mostrarContacto(info);
    mostrarServicios(servicios);
    mostrarPoliticas(info);
    mostrarRedes(info);
}

/** Texto de trayectoria (grupo "nosotros"); los saltos de línea se respetan como párrafos. */
function mostrarTrayectoria(info) {
    const texto = dato(info, "trayectoria");
    const parrafos = texto.split(/\n+/).map((p) => p.trim()).filter(Boolean);
    document.getElementById("trayectoria").replaceChildren(...parrafos.map((p) => crear("p", { texto: p })));
}

/** Tarjeta de contacto: botón de WhatsApp y los demás datos con su enlace. */
function mostrarContacto(info) {
    const numero = dato(info, "whatsapp");
    if (numero) {
        document.getElementById("boton-whatsapp").replaceChildren(crear("a", {
            clase: "boton boton--whatsapp tarjeta-contacto__whatsapp",
            atributos: {
                href: enlaceWhatsApp(numero, "Hola, Tapizados El Miura. Quiero más información."),
                target: "_blank",
                rel: "noopener"
            }
        }, [icono("whatsapp"), crear("span", { texto: `Escribir al ${formatoCelular(numero)}` })]));
    }

    // El WhatsApp ya tiene su botón; aquí van teléfono, correo, dirección y horario
    const datos = datosContacto(info).filter((d) => d.nombreIcono !== "whatsapp");
    const titulos = { telefono: "Teléfono", correo: "Correo", ubicacion: "Dirección", reloj: "Horario" };
    document.getElementById("contacto").replaceChildren(...datos.map((d) => crear("li", {}, [
        icono(d.nombreIcono),
        crear("div", {}, [
            crear("span", { clase: "tarjeta-contacto__titulo", texto: titulos[d.nombreIcono] }),
            d.href
                ? crear("a", {
                    texto: d.nombreIcono === "ubicacion" ? `${d.texto} (ver en el mapa)` : d.texto,
                    atributos: { href: d.href, target: d.nombreIcono === "ubicacion" ? "_blank" : null, rel: d.nombreIcono === "ubicacion" ? "noopener" : null }
                })
                : crear("span", { texto: d.texto })
        ])
    ])));
}

function mostrarServicios(servicios) {
    const lista = document.getElementById("servicios");
    if (servicios.length === 0) {
        lista.closest("section").hidden = true;
        return;
    }
    lista.replaceChildren(...servicios.map((s) => crear("li", { clase: "servicio-info" }, [
        crear("h3", { texto: s.nombre }),
        s.descripcion && crear("p", { texto: s.descripcion })
    ])));
}

/** Cobertura de entregas y política de garantía. */
function mostrarPoliticas(info) {
    const bloques = [
        { clave: "cobertura", titulo: "Hasta dónde llegamos", icono: "ruta" },
        { clave: "garantia", titulo: "Garantía", icono: "escudo" }
    ].filter((b) => dato(info, b.clave));
    const contenedor = document.getElementById("politicas");
    if (bloques.length === 0) {
        contenedor.closest("section").hidden = true;
        return;
    }
    contenedor.replaceChildren(...bloques.map((b) => crear("article", { clase: "politica" }, [
        icono(b.icono),
        crear("h2", { texto: b.titulo }),
        crear("p", { texto: dato(info, b.clave) })
    ])));
}

function mostrarRedes(info) {
    const redes = redesSociales(info);
    if (redes.length === 0) {
        return;
    }
    document.getElementById("redes").replaceChildren(...redes.map((r) => crear("a", {
        clase: "red",
        atributos: { href: r.url, target: "_blank", rel: "noopener" }
    }, [icono(r.icono), crear("span", { texto: r.nombre })])));
    document.getElementById("seccion-redes").hidden = false;
}
