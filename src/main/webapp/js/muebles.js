/**
 * @file Catálogo de productos (muebles.html), RF01 / CU02.
 *
 * Servicios que usa:
 *   - GET /api/categorias       opciones del filtro de categoría
 *   - GET /api/productos?q=...  productos visibles; la búsqueda la resuelve el servidor
 *
 * La categoría, los filtros de la ficha técnica y el orden se aplican en el
 * navegador sobre la lista recibida, así cambian al instante sin volver a
 * consultar. La categoría elegida se guarda en la dirección (?categoria=N)
 * para que se pueda compartir el enlace.
 */

import { obtener } from "./comun/api.js";
import { crear, parametroUrl, plural } from "./comun/formato.js";
import { iniciarPlantilla } from "./comun/plantilla.js";
import { tarjetaProducto, tarjetasCargando } from "./comun/tarjeta-producto.js";

/**
 * Filtros que se arman con la ficha técnica. Las opciones salen de los
 * valores que tienen los productos, por eso siempre coinciden con la BD.
 */
const FILTROS_FICHA = [
    { campo: "color", titulo: "Color" },
    { campo: "materialTapiz", titulo: "Tela o tapiz" },
    { campo: "materialEstructura", titulo: "Estructura" },
    { campo: "tipoRelleno", titulo: "Relleno" }
];

/** Rangos de precio (en pesos) del filtro "Precio". */
const RANGOS_PRECIO = [
    { valor: "0-1000000", texto: "Hasta $ 1.000.000", min: 0, max: 1_000_000 },
    { valor: "1000000-2000000", texto: "$ 1.000.000 a $ 2.000.000", min: 1_000_000, max: 2_000_000 },
    { valor: "2000000-", texto: "Más de $ 2.000.000", min: 2_000_000, max: Infinity }
];

const estado = {
    productos: [],
    categorias: [],
    busqueda: parametroUrl("q"),
    idCategoria: Number(parametroUrl("categoria")) || 0
};

const elementos = {
    productos: document.getElementById("productos"),
    formulario: document.getElementById("formulario-filtros"),
    titulo: document.getElementById("titulo"),
    conteo: document.getElementById("conteo"),
    descripcion: document.getElementById("descripcion"),
    migas: document.getElementById("migas"),
    orden: document.getElementById("orden"),
    panel: document.getElementById("filtros"),
    abrir: document.getElementById("abrir-filtros"),
    activos: document.getElementById("filtros-activos"),
    busquedaActiva: document.getElementById("busqueda-activa")
};

iniciarPlantilla();
prepararEventos();
cargar();

async function cargar() {
    elementos.productos.replaceChildren(...tarjetasCargando(6));
    try {
        const ruta = estado.busqueda ? `productos?q=${encodeURIComponent(estado.busqueda)}` : "productos";
        const [productos, categorias] = await Promise.all([obtener(ruta), obtener("categorias")]);
        estado.productos = productos;
        estado.categorias = categorias;
        if (!categorias.some((c) => c.idCategoria === estado.idCategoria)) {
            estado.idCategoria = 0; // categoría inexistente u oculta en la dirección
        }
        dibujarFiltros();
        mostrarBusqueda();
        aplicar();
    } catch (error) {
        elementos.productos.replaceChildren(crear("div", { clase: "estado estado--error" }, [
            crear("p", { texto: error.message }),
            crear("button", { clase: "boton boton--contorno", texto: "Intentar de nuevo", atributos: { type: "button" } })
        ]));
        elementos.productos.querySelector("button").addEventListener("click", cargar);
    }
}

// ---------------------------------------------------------------------
// Filtros
// ---------------------------------------------------------------------

/** Dibuja los grupos de filtros como acordeones (<details>). */
function dibujarFiltros() {
    const grupos = [];

    // Categoría: una sola a la vez (radio), incluye "Todas"
    const opcionesCategoria = [{ valor: "0", texto: "Todas", conteo: estado.productos.length }]
        .concat(estado.categorias
            .map((c) => ({ valor: String(c.idCategoria), texto: c.nombre, conteo: contar((p) => p.idCategoria === c.idCategoria) }))
            .filter((o) => o.conteo > 0));
    grupos.push(grupoFiltro("Categoría", "categoria", "radio", opcionesCategoria, true));

    // Ficha técnica: varias opciones a la vez (checkbox)
    for (const filtro of FILTROS_FICHA) {
        const valores = [...new Set(estado.productos.map((p) => p.ficha?.[filtro.campo]).filter(Boolean))]
            .sort((a, b) => a.localeCompare(b, "es"));
        if (valores.length > 1) {
            grupos.push(grupoFiltro(filtro.titulo, filtro.campo, "checkbox",
                valores.map((v) => ({ valor: v, texto: v, conteo: contar((p) => p.ficha?.[filtro.campo] === v) }))));
        }
    }

    grupos.push(grupoFiltro("Precio", "precio", "checkbox", RANGOS_PRECIO.map((r) => ({
        valor: r.valor, texto: r.texto, conteo: contar((p) => p.precio >= r.min && p.precio < r.max)
    })).filter((o) => o.conteo > 0)));

    grupos.push(grupoFiltro("Disponibilidad", "disponible", "checkbox", [
        { valor: "si", texto: "Solo disponibles para entrega", conteo: contar((p) => !p.agotado) }
    ]));

    elementos.formulario.replaceChildren(...grupos);
    const radio = elementos.formulario.querySelector(`input[name="categoria"][value="${estado.idCategoria}"]`);
    if (radio) {
        radio.checked = true;
    }
}

function contar(condicion) {
    return estado.productos.filter(condicion).length;
}

/**
 * Un grupo de filtros desplegable.
 *
 * @param {string} titulo
 * @param {string} nombre atributo name de los campos
 * @param {"radio"|"checkbox"} tipo
 * @param {Array<{valor: string, texto: string, conteo: number}>} opciones
 * @param {boolean} [abierto=false]
 */
function grupoFiltro(titulo, nombre, tipo, opciones, abierto = false) {
    return crear("details", { clase: "filtro", atributos: { open: abierto } }, [
        crear("summary", { texto: titulo }),
        crear("fieldset", {}, [
            crear("legend", { clase: "solo-lector", texto: titulo }),
            ...opciones.map((o) => crear("label", { clase: "filtro__opcion" }, [
                crear("input", { atributos: { type: tipo, name: nombre, value: o.valor } }),
                crear("span", { texto: o.texto }),
                crear("span", { clase: "filtro__conteo", texto: `(${o.conteo})` })
            ]))
        ])
    ]);
}

/** Valores marcados de un grupo. */
function marcados(nombre) {
    return [...elementos.formulario.querySelectorAll(`input[name="${nombre}"]:checked`)].map((i) => i.value);
}

/** Aplica categoría, filtros y orden, y dibuja el resultado. */
function aplicar() {
    estado.idCategoria = Number(marcados("categoria")[0] ?? 0);
    let lista = estado.productos.filter((p) => estado.idCategoria === 0 || p.idCategoria === estado.idCategoria);

    let filtrosActivos = 0;
    for (const filtro of FILTROS_FICHA) {
        const valores = marcados(filtro.campo);
        if (valores.length > 0) {
            filtrosActivos++;
            lista = lista.filter((p) => valores.includes(p.ficha?.[filtro.campo]));
        }
    }
    const rangos = RANGOS_PRECIO.filter((r) => marcados("precio").includes(r.valor));
    if (rangos.length > 0) {
        filtrosActivos++;
        lista = lista.filter((p) => rangos.some((r) => p.precio >= r.min && p.precio < r.max));
    }
    if (marcados("disponible").length > 0) {
        filtrosActivos++;
        lista = lista.filter((p) => !p.agotado);
    }

    ordenar(lista, elementos.orden.value);
    actualizarEncabezado(lista.length, filtrosActivos);
    dibujarProductos(lista, filtrosActivos);
}

function ordenar(lista, criterio) {
    const comparadores = {
        "recomendados": (a, b) => Number(a.agotado) - Number(b.agotado) || a.nombre.localeCompare(b.nombre, "es"),
        "precio-menor": (a, b) => a.precio - b.precio,
        "precio-mayor": (a, b) => b.precio - a.precio,
        "nombre": (a, b) => a.nombre.localeCompare(b.nombre, "es")
    };
    lista.sort(comparadores[criterio] ?? comparadores.recomendados);
}

// ---------------------------------------------------------------------
// Presentación
// ---------------------------------------------------------------------

/** Título, conteo, descripción, migas de pan y dirección según la categoría. */
function actualizarEncabezado(total, filtrosActivos) {
    const categoria = estado.categorias.find((c) => c.idCategoria === estado.idCategoria);
    const nombre = categoria?.nombre ?? "Muebles";
    elementos.titulo.textContent = nombre;
    elementos.conteo.textContent = `(${plural(total, "producto")})`;
    elementos.activos.textContent = filtrosActivos > 0 ? `(${filtrosActivos})` : "";
    document.title = `${nombre} | Tapizados El Miura`;
    if (categoria?.descripcion) {
        elementos.descripcion.textContent = categoria.descripcion;
    }

    const migas = [
        crear("li", {}, [crear("a", { texto: "Inicio", atributos: { href: "index.html" } })]),
        crear("li", {}, [crear("a", { texto: "Muebles", atributos: { href: "muebles.html" } })])
    ];
    if (categoria) {
        migas.push(crear("li", { texto: categoria.nombre, atributos: { "aria-current": "page" } }));
    }
    elementos.migas.replaceChildren(...migas);

    // La dirección refleja la categoría elegida (sin recargar la página)
    const parametros = new URLSearchParams(window.location.search);
    if (estado.idCategoria) {
        parametros.set("categoria", estado.idCategoria);
    } else {
        parametros.delete("categoria");
    }
    const consulta = parametros.toString();
    history.replaceState(null, "", consulta ? `?${consulta}` : window.location.pathname);
}

function dibujarProductos(lista, filtrosActivos) {
    if (lista.length > 0) {
        elementos.productos.replaceChildren(...lista.map(tarjetaProducto));
        return;
    }
    // CU02 flujo alterno 2a
    const vacio = crear("div", { clase: "estado catalogo__vacio" }, [
        crear("h2", { texto: "No hay productos disponibles por el momento" }),
        crear("p", {
            texto: filtrosActivos > 0 || estado.busqueda
                ? "Ningún mueble coincide con lo que elegiste. Quita algún filtro o busca con otras palabras."
                : "Pronto agregaremos muebles a esta categoría. Escríbenos si buscas algo en particular."
        })
    ]);
    elementos.productos.replaceChildren(vacio);
}

/** Muestra el texto buscado con la opción de quitar la búsqueda. */
function mostrarBusqueda() {
    if (!estado.busqueda) {
        return;
    }
    elementos.busquedaActiva.hidden = false;
    elementos.busquedaActiva.replaceChildren(
        crear("span", { texto: `Resultados para "${estado.busqueda}"` }),
        crear("a", { texto: "Quitar búsqueda", atributos: { href: "muebles.html" } })
    );
}

// ---------------------------------------------------------------------
// Eventos
// ---------------------------------------------------------------------

function prepararEventos() {
    elementos.formulario.addEventListener("change", aplicar);
    elementos.orden.addEventListener("change", aplicar);

    document.getElementById("limpiar-filtros").addEventListener("click", () => {
        elementos.formulario.querySelectorAll("input:checked").forEach((i) => { i.checked = false; });
        const todas = elementos.formulario.querySelector('input[name="categoria"][value="0"]');
        if (todas) {
            todas.checked = true;
        }
        aplicar();
    });

    // Panel de filtros en celular
    const abrirPanel = (abierto) => {
        elementos.panel.classList.toggle("filtros--abierto", abierto);
        elementos.abrir.setAttribute("aria-expanded", String(abierto));
        document.body.classList.toggle("sin-desplazamiento", abierto);
    };
    elementos.abrir.addEventListener("click", () => abrirPanel(true));
    document.getElementById("cerrar-filtros").addEventListener("click", () => abrirPanel(false));
    document.getElementById("ver-resultados").addEventListener("click", () => abrirPanel(false));
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            abrirPanel(false);
        }
    });
}
