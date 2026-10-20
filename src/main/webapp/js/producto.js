/**
 * @file Detalle de producto (producto.html?id=N), RF02 / CU03 y RF03 / CU04.
 *
 * Servicios que usa:
 *   - GET /api/productos/{id}            ficha técnica y fotos (404 si no existe o está oculto)
 *   - GET /api/productos?categoria={id}  sugerencias de la misma categoría
 *   - GET /api/informacion               garantía, cobertura y WhatsApp (vía la plantilla)
 */

import { ErrorApi, obtener } from "./comun/api.js";
import { MAXIMO_UNIDADES } from "./comun/carrito.js";
import { crear, enlaceWhatsApp, parametroUrl, pesos, plural } from "./comun/formato.js";
import { icono } from "./comun/iconos.js";
import { dato, iniciarPlantilla, informacionNegocio } from "./comun/plantilla.js";
import { agregarAlCarrito, tarjetaProducto } from "./comun/tarjeta-producto.js";
import { abrirVisor } from "./comun/visor.js";

/** Filas de la ficha técnica: campo de la API, título y unidad. */
const FICHA = [
    { campo: "estilo", titulo: "Estilo" },
    { campo: "color", titulo: "Color" },
    { campo: "materialTapiz", titulo: "Tela o tapiz" },
    { campo: "materialEstructura", titulo: "Estructura" },
    { campo: "tipoRelleno", titulo: "Relleno" },
    { campo: "tipoPata", titulo: "Patas" },
    { campo: "altoCm", titulo: "Alto", unidad: "cm" },
    { campo: "anchoCm", titulo: "Ancho", unidad: "cm" },
    { campo: "profundidadCm", titulo: "Profundidad", unidad: "cm" },
    { campo: "pesoKg", titulo: "Peso", unidad: "kg" },
    { campo: "resistenciaKgPuesto", titulo: "Resistencia por puesto", unidad: "kg" }
];

const contenedor = document.getElementById("producto");

iniciarPlantilla();
cargar();

async function cargar() {
    const id = Number(parametroUrl("id"));
    if (!Number.isInteger(id) || id <= 0) {
        mostrarNoDisponible();
        return;
    }
    try {
        const [producto, info] = await Promise.all([obtener(`productos/${id}`), informacionNegocio()]);
        mostrarProducto(producto, info);
        cargarRelacionados(producto);
    } catch (error) {
        if (error instanceof ErrorApi && (error.estado === 404 || error.estado === 400)) {
            mostrarNoDisponible();
        } else {
            contenedor.replaceChildren(crear("p", { clase: "estado estado--error", texto: error.message }));
        }
    } finally {
        contenedor.removeAttribute("aria-busy");
    }
}

/** CU03: el producto no existe o ya no está publicado. */
function mostrarNoDisponible() {
    document.title = "Producto no disponible | Tapizados El Miura";
    contenedor.replaceChildren(crear("div", { clase: "estado producto__no-disponible" }, [
        crear("h1", { texto: "Este mueble ya no está disponible" }),
        crear("p", { texto: "Puede que lo hayamos retirado del catálogo. Mira los demás muebles o escríbenos si lo buscabas." }),
        crear("a", { clase: "boton boton--principal", texto: "Ver el catálogo", atributos: { href: "muebles.html" } })
    ]));
}

// ---------------------------------------------------------------------
// Ficha del producto
// ---------------------------------------------------------------------

function mostrarProducto(p, info) {
    document.title = `${p.nombre} | Tapizados El Miura`;
    actualizarMigas(p);
    contenedor.replaceChildren(galeria(p), informacion(p, info), fichaTecnica(p));
}

function actualizarMigas(p) {
    document.getElementById("migas").replaceChildren(
        crear("li", {}, [crear("a", { texto: "Inicio", atributos: { href: "index.html" } })]),
        crear("li", {}, [crear("a", { texto: "Muebles", atributos: { href: "muebles.html" } })]),
        crear("li", {}, [crear("a", { texto: p.categoria, atributos: { href: `muebles.html?categoria=${p.idCategoria}` } })]),
        crear("li", { texto: p.nombre, atributos: { "aria-current": "page" } })
    );
}

/** Foto principal con miniaturas para cambiarla; clic en la foto la amplía. */
function galeria(p) {
    const fotos = p.imagenes?.length ? p.imagenes : [{ url: p.imagen, textoAlternativo: p.nombre }];
    const principal = crear("img", { atributos: { src: fotos[0].url, alt: fotos[0].textoAlternativo || p.nombre, width: "800", height: "800" } });
    const botonPrincipal = crear("button", {
        clase: "galeria__principal",
        atributos: { type: "button", "aria-label": "Ampliar foto" }
    }, [principal, p.agotado && crear("span", { clase: "sello sello--agotado", texto: "Agotado" })]);
    botonPrincipal.addEventListener("click", () => abrirVisor(principal.src, principal.alt));

    const miniaturas = fotos.length > 1
        ? crear("div", { clase: "galeria__miniaturas" }, fotos.map((foto, i) => {
            const boton = crear("button", {
                clase: "galeria__miniatura",
                atributos: { type: "button", "aria-label": `Ver foto ${i + 1} de ${fotos.length}`, "aria-pressed": String(i === 0) }
            }, [crear("img", { atributos: { src: foto.url, alt: "", loading: "lazy" } })]);
            boton.addEventListener("click", () => {
                principal.src = foto.url;
                principal.alt = foto.textoAlternativo || p.nombre;
                boton.parentElement.querySelectorAll("button").forEach((b) => b.setAttribute("aria-pressed", String(b === boton)));
            });
            return boton;
        }))
        : null;

    return crear("div", { clase: "producto__galeria" }, [botonPrincipal, miniaturas]);
}

/** Nombre, precio, disponibilidad, cantidad y botones de acción. */
function informacion(p, info) {
    const disponibilidad = p.agotado
        ? crear("p", { clase: "disponibilidad disponibilidad--agotado", texto: "Agotado: lo fabricamos bajo pedido" })
        : crear("p", { clase: "disponibilidad", texto: `Disponible: ${plural(p.stock, "unidad", "unidades")}` });

    // Selector de cantidad (1 a 10, igual que la API)
    const campo = crear("input", {
        atributos: { type: "number", id: "cantidad", min: "1", max: String(MAXIMO_UNIDADES), value: "1", inputmode: "numeric" }
    });
    const menos = crear("button", { texto: "−", atributos: { type: "button", "aria-label": "Quitar una unidad" } });
    const mas = crear("button", { texto: "+", atributos: { type: "button", "aria-label": "Agregar una unidad" } });
    const normalizar = () => {
        const valor = Math.max(1, Math.min(MAXIMO_UNIDADES, Math.trunc(Number(campo.value)) || 1));
        campo.value = String(valor);
        menos.disabled = valor <= 1;
        mas.disabled = valor >= MAXIMO_UNIDADES;
        return valor;
    };
    menos.addEventListener("click", () => { campo.value = String(Number(campo.value) - 1); normalizar(); });
    mas.addEventListener("click", () => { campo.value = String(Number(campo.value) + 1); normalizar(); });
    campo.addEventListener("change", normalizar);
    normalizar();

    const agregar = crear("button", { clase: "boton boton--principal producto__agregar", texto: "Agregar al carrito", atributos: { type: "button" } });
    agregar.addEventListener("click", () => agregarAlCarrito(p, normalizar()));

    const whatsapp = dato(info, "whatsapp");
    const consultar = whatsapp
        ? crear("a", {
            clase: "boton boton--contorno",
            atributos: {
                href: enlaceWhatsApp(whatsapp, `Hola, Tapizados El Miura. Quiero información sobre el mueble "${p.nombre}" (Ref. ${p.referencia}).`),
                target: "_blank",
                rel: "noopener"
            }
        }, [icono("whatsapp"), crear("span", { texto: "Preguntar por WhatsApp" })])
        : null;

    const garantia = dato(info, "garantia");
    const cobertura = dato(info, "cobertura");
    const notas = crear("ul", { clase: "producto__notas" }, [
        garantia && crear("li", {}, [icono("escudo"), crear("span", {}, [crear("strong", { texto: "Garantía. " }), document.createTextNode(garantia)])]),
        cobertura && crear("li", {}, [icono("ruta"), crear("span", {}, [crear("strong", { texto: "Entregas. " }), document.createTextNode(cobertura)])])
    ]);

    return crear("div", { clase: "producto__info" }, [
        crear("a", { clase: "producto__categoria", texto: p.categoria, atributos: { href: `muebles.html?categoria=${p.idCategoria}` } }),
        crear("h1", { clase: "producto__nombre", texto: p.nombre }),
        crear("p", { clase: "producto__ref", texto: `Ref. ${p.referencia}` }),
        crear("p", { clase: "producto__precio", texto: pesos(p.precio) }),
        disponibilidad,
        p.descripcion && crear("p", { clase: "producto__descripcion", texto: p.descripcion }),
        crear("div", { clase: "producto__compra" }, [
            crear("label", { texto: "Cantidad", atributos: { for: "cantidad" } }),
            crear("div", { clase: "cantidad" }, [menos, campo, mas])
        ]),
        crear("div", { clase: "producto__acciones" }, [agregar, consultar]),
        notas.children.length > 0 ? notas : null
    ]);
}

/** Ficha técnica como lista de definiciones; los datos vacíos no se muestran. */
function fichaTecnica(p) {
    const filas = FICHA
        .filter((f) => p.ficha?.[f.campo] !== undefined && p.ficha?.[f.campo] !== null && p.ficha?.[f.campo] !== "")
        .map((f) => [f.titulo, f.unidad ? `${Number(p.ficha[f.campo]).toLocaleString("es-CO")} ${f.unidad}` : p.ficha[f.campo]]);
    filas.push(["Requiere armado", p.ficha?.requiereArmado ? "Sí" : "No"]);

    return crear("section", { clase: "producto__ficha", atributos: { "aria-labelledby": "titulo-ficha" } }, [
        crear("h2", { texto: "Ficha técnica", atributos: { id: "titulo-ficha" } }),
        crear("dl", {}, filas.flatMap(([titulo, valor]) => [
            crear("div", { clase: "ficha__fila" }, [crear("dt", { texto: titulo }), crear("dd", { texto: valor })])
        ]))
    ]);
}

// ---------------------------------------------------------------------
// Relacionados
// ---------------------------------------------------------------------

/** Hasta 4 productos de la misma categoría (sin el actual). Si falla, la sección no se muestra. */
async function cargarRelacionados(p) {
    try {
        const productos = await obtener(`productos?categoria=${p.idCategoria}`);
        const otros = productos.filter((o) => o.idProducto !== p.idProducto).slice(0, 4);
        if (otros.length > 0) {
            document.getElementById("lista-relacionados").replaceChildren(...otros.map(tarjetaProducto));
            document.getElementById("relacionados").hidden = false;
        }
    } catch {
        // Las sugerencias son opcionales
    }
}
