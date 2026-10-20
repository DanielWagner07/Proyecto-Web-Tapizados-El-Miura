/**
 * @file Tarjeta de producto del catálogo (RF01), usada en el inicio, en el
 * catálogo y en "También te puede interesar" del detalle.
 *
 * Al hacer clic en la foto o el nombre se abre la ficha del producto
 * (producto.html?id=N). El botón "+" lo agrega al carrito sin salir de la página.
 */

import { mostrarAviso } from "./avisos.js";
import * as carrito from "./carrito.js";
import { crear, pesos } from "./formato.js";
import { icono } from "./iconos.js";

/**
 * Crea la tarjeta de un producto.
 *
 * @param {object} producto producto tal como lo entrega GET /api/productos
 * @returns {HTMLElement}
 */
export function tarjetaProducto(producto) {
    const enlace = `producto.html?id=${producto.idProducto}`;

    const agregar = crear("button", {
        clase: "tarjeta-producto__agregar",
        atributos: { type: "button", "aria-label": `Agregar ${producto.nombre} al carrito`, title: "Agregar al carrito" }
    }, [icono("mas")]);
    agregar.addEventListener("click", () => agregarAlCarrito(producto));

    const foto = crear("div", { clase: "tarjeta-producto__foto" }, [
        // La foto repite el enlace del nombre: se oculta a lectores y al tabulador para no duplicarlo
        crear("a", { atributos: { href: enlace, tabindex: "-1", "aria-hidden": "true" } }, [
            crear("img", { atributos: { src: producto.imagen, alt: "", loading: "lazy", width: "400", height: "400" } })
        ]),
        producto.agotado && crear("span", { clase: "sello sello--agotado", texto: "Agotado" }),
        agregar
    ]);

    return crear("article", { clase: "tarjeta-producto" }, [
        foto,
        crear("a", { clase: "tarjeta-producto__enlace", atributos: { href: enlace } }, [
            crear("h3", { clase: "tarjeta-producto__nombre", texto: producto.nombre })
        ]),
        crear("p", { clase: "tarjeta-producto__precio", texto: pesos(producto.precio) }),
        crear("p", { clase: "tarjeta-producto__ref", texto: `Ref. ${producto.referencia}` })
    ]);
}

/**
 * Agrega una unidad al carrito y lo confirma con un aviso.
 * Si el producto está agotado se avisa que se fabrica bajo pedido.
 *
 * @param {object} producto
 * @param {number} [cantidad=1]
 */
export function agregarAlCarrito(producto, cantidad = 1) {
    const resultado = carrito.agregar(producto, cantidad);
    const nota = resultado.ok && producto.agotado ? " Está agotado: se fabrica bajo pedido." : "";
    mostrarAviso(resultado.mensaje + nota, {
        tipo: resultado.ok ? "normal" : "error",
        accion: resultado.ok ? { texto: "Ver carrito", href: "cotizacion.html" } : undefined
    });
}

/**
 * Tarjetas grises mientras llegan los productos.
 *
 * @param {number} cantidad número de tarjetas
 * @returns {HTMLElement[]}
 */
export function tarjetasCargando(cantidad) {
    return Array.from({ length: cantidad }, () => crear("div", { atributos: { "aria-hidden": "true" } }, [
        crear("div", { clase: "esqueleto esqueleto--foto" }),
        crear("div", { clase: "esqueleto esqueleto--linea" }),
        crear("div", { clase: "esqueleto esqueleto--linea esqueleto--corta" })
    ]));
}
