/**
 * @file Comunicación con los servicios web del proyecto (docs/API.md).
 *
 * Todas las páginas piden sus datos a través de este módulo, así el manejo
 * de errores es igual en todo el sitio. Las rutas son relativas a la página
 * (por ejemplo "api/productos"), porque las páginas y la API se sirven desde
 * la misma aplicación: http://localhost:8081/tapizados-miura/.
 */

/** Error devuelto por la API o por la red, con el formato único del proyecto. */
export class ErrorApi extends Error {
    /**
     * @param {number} estado código HTTP (0 si no hubo respuesta del servidor)
     * @param {string} mensaje explicación para mostrar al usuario
     * @param {Object<string, string>} [errores] detalle por campo, si la API lo envió
     */
    constructor(estado, mensaje, errores = {}) {
        super(mensaje);
        this.name = "ErrorApi";
        this.estado = estado;
        this.errores = errores;
    }
}

const SIN_CONEXION = "No hay conexión con el servidor. Revise su internet e intente de nuevo.";

/**
 * Hace la petición y convierte la respuesta JSON.
 *
 * @param {string} ruta ruta del servicio sin el prefijo "api/", ej. "productos?categoria=1"
 * @param {RequestInit} [opciones] método, cabeceras y cuerpo
 * @returns {Promise<any>} el cuerpo de la respuesta ya convertido
 * @throws {ErrorApi} si el servidor responde con error o no responde
 */
async function solicitar(ruta, opciones = {}) {
    let respuesta;
    try {
        respuesta = await fetch(`api/${ruta}`, {
            ...opciones,
            headers: { Accept: "application/json", ...opciones.headers }
        });
    } catch {
        throw new ErrorApi(0, SIN_CONEXION);
    }

    let cuerpo = null;
    try {
        cuerpo = await respuesta.json();
    } catch {
        // Respuesta sin JSON (por ejemplo, el servidor está reiniciando)
    }

    if (!respuesta.ok) {
        const mensaje = cuerpo?.mensaje ?? "Ocurrió un error en el servidor. Intente de nuevo.";
        throw new ErrorApi(respuesta.status, mensaje, cuerpo?.errores ?? {});
    }
    return cuerpo;
}

/**
 * Consulta un servicio (GET).
 *
 * @param {string} ruta ej. "productos/7" o "trabajos?tipo=restauracion"
 * @returns {Promise<any>}
 */
export function obtener(ruta) {
    return solicitar(ruta);
}

/**
 * Envía datos a un servicio (POST con cuerpo JSON).
 *
 * @param {string} ruta ej. "cotizaciones"
 * @param {object} datos objeto que se enviará como JSON
 * @returns {Promise<{mensaje: string, datos: any}>}
 */
export function enviar(ruta, datos) {
    return solicitar(ruta, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(datos)
    });
}
