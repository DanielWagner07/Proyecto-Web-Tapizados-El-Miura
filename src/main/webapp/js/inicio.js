/**
 * @file Página de inicio (index.html).
 *
 * Servicios que usa:
 *   - GET /api/productos   vitrina de la portada, imagen de cada categoría y "Lo que ofrecemos"
 *   - GET /api/categorias  tarjetas de categoría (solo las que tienen productos)
 *   - GET /api/trabajos    trabajos recientes
 */

import { obtener } from "./comun/api.js";
import { crear, plural } from "./comun/formato.js";
import { iniciarPlantilla } from "./comun/plantilla.js";
import { tarjetaProducto, tarjetasCargando } from "./comun/tarjeta-producto.js";

/** Cantidad de productos en "Lo que ofrecemos". */
const PRODUCTOS_INICIO = 8;
/** Cantidad de trabajos en la vista previa. */
const TRABAJOS_INICIO = 3;

iniciarPlantilla();
cargarPagina();

async function cargarPagina() {
    const contProductos = document.getElementById("productos");
    contProductos.replaceChildren(...tarjetasCargando(PRODUCTOS_INICIO));

    // Las tres consultas van en paralelo; si una falla, las demás se muestran igual
    const [productos, categorias, trabajos] = await Promise.allSettled([
        obtener("productos"),
        obtener("categorias"),
        obtener("trabajos")
    ]);

    if (productos.status === "fulfilled") {
        const lista = ordenarDisponiblesPrimero(productos.value);
        mostrarVitrina(lista);
        mostrarProductos(contProductos, lista.slice(0, PRODUCTOS_INICIO));
        if (categorias.status === "fulfilled") {
            mostrarCategorias(categorias.value, productos.value);
        }
    } else {
        contProductos.replaceChildren(estadoError(productos.reason.message));
    }

    if (trabajos.status === "fulfilled") {
        mostrarTrabajos(trabajos.value.slice(0, TRABAJOS_INICIO));
    } else {
        document.getElementById("trabajos").replaceChildren(estadoError(trabajos.reason.message));
    }
}

/**
 * Los productos disponibles van primero; los agotados al final.
 * La BD no tiene un campo "destacado", así que esta es la regla del inicio.
 */
function ordenarDisponiblesPrimero(productos) {
    return [...productos].sort((a, b) => Number(a.agotado) - Number(b.agotado));
}

/** Portada: tres fotos de productos de categorías distintas sobre la forma naranja del logo. */
function mostrarVitrina(productos) {
    const elegidos = [];
    const categoriasUsadas = new Set();
    for (const p of productos) {
        if (!categoriasUsadas.has(p.idCategoria) && !p.imagen.endsWith("sin-imagen.svg")) {
            elegidos.push(p);
            categoriasUsadas.add(p.idCategoria);
        }
        if (elegidos.length === 3) {
            break;
        }
    }
    const vitrina = document.getElementById("vitrina");
    elegidos.forEach((p, i) => {
        vitrina.append(crear("div", { clase: `vitrina__marco vitrina__marco--${i + 1}` }, [
            crear("img", { atributos: { src: p.imagen, alt: "", width: "600", height: "600" } })
        ]));
    });
}

function mostrarProductos(contenedor, productos) {
    if (productos.length === 0) {
        contenedor.replaceChildren(crear("p", { clase: "estado", texto: "No hay productos disponibles por el momento." }));
        return;
    }
    contenedor.replaceChildren(...productos.map(tarjetaProducto));
}

/**
 * Tarjetas de categoría. La imagen es la del primer producto de la categoría,
 * porque la tabla categoria no guarda foto. Las categorías sin productos no se muestran.
 */
function mostrarCategorias(categorias, productos) {
    const contenedor = document.getElementById("categorias");
    const conProductos = categorias.filter((c) => c.totalProductos > 0);
    contenedor.replaceChildren(...conProductos.map((categoria) => {
        const foto = productos.find((p) => p.idCategoria === categoria.idCategoria)?.imagen ?? "img/sin-imagen.svg";
        return crear("a", { clase: "categoria", atributos: { href: `muebles.html?categoria=${categoria.idCategoria}` } }, [
            crear("span", { clase: "categoria__foto" }, [
                crear("img", { atributos: { src: foto, alt: "", loading: "lazy", width: "300", height: "300" } })
            ]),
            crear("span", { clase: "categoria__nombre", texto: categoria.nombre }),
            crear("span", { clase: "categoria__conteo", texto: plural(categoria.totalProductos, "producto") })
        ]);
    }));
}

/** Vista previa de trabajos; cada uno lleva a su galería según el tipo. */
function mostrarTrabajos(trabajos) {
    const contenedor = document.getElementById("trabajos");
    if (trabajos.length === 0) {
        contenedor.replaceChildren(crear("p", { clase: "estado", texto: "Pronto publicaremos nuestros trabajos." }));
        return;
    }
    contenedor.replaceChildren(...trabajos.map((t) => {
        const pagina = t.tipo === "restauracion" ? "restauraciones.html" : "renovaciones.html";
        return crear("a", { clase: "trabajo", atributos: { href: `${pagina}#trabajo-${t.idTrabajo}` } }, [
            crear("span", { clase: "trabajo__foto" }, [
                crear("img", { atributos: { src: t.imagenPortada, alt: "", loading: "lazy" } })
            ]),
            crear("span", { clase: "trabajo__tipo", texto: t.tipoLegible }),
            crear("span", { clase: "trabajo__titulo", texto: t.titulo })
        ]);
    }));
}

function estadoError(mensaje) {
    return crear("p", { clase: "estado estado--error", texto: mensaje });
}
