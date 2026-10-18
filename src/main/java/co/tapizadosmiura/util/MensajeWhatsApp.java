package co.tapizadosmiura.util;

import co.tapizadosmiura.modelo.DetalleCotizacion;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Arma el mensaje de una cotización y el enlace de WhatsApp (RF04 / CU05 / RNF08).
 *
 * <p>Ejemplo de mensaje:</p>
 * <pre>
 * Hola, Tapizados El Miura. Quiero esta cotización (N.º 3):
 * - 1 x Sofácama Cajón Soren (140 cm) Azul: $1.999.999
 * - 2 x Poltrona Reclinable Azul Noche ($1.299.999 c/u): $2.599.998
 * - 1 x Sofá Roman 2 Puestos Gris Claro: $1.399.999 (agotado, sujeto a fabricación)
 * Total estimado: $5.999.996
 * </pre>
 */
public final class MensajeWhatsApp {

    /** Formato en que se guarda el WhatsApp del negocio: indicativo + número, solo dígitos. */
    private static final Pattern NUMERO = Pattern.compile("\\d{10,15}");

    private MensajeWhatsApp() {
    }

    /**
     * Texto de la cotización tal como lo recibirá el negocio.
     *
     * @param idCotizacion número de la cotización registrada
     * @param detalles     productos con cantidad, precio y stock del momento
     * @param total        suma de los subtotales
     * @return mensaje en varias líneas
     */
    public static String texto(int idCotizacion, List<DetalleCotizacion> detalles, BigDecimal total) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hola, Tapizados El Miura. Quiero esta cotización (N.º ").append(idCotizacion).append("):\n");
        for (DetalleCotizacion d : detalles) {
            sb.append("- ").append(d.getCantidad()).append(" x ").append(d.getNombreProducto());
            if (d.getCantidad() > 1) {
                sb.append(" (").append(FormatoMoneda.pesos(d.getPrecioUnitario())).append(" c/u)");
            }
            sb.append(": ").append(FormatoMoneda.pesos(d.getSubtotal()));
            sb.append(notaDisponibilidad(d));
            sb.append('\n');
        }
        sb.append("Total estimado: ").append(FormatoMoneda.pesos(total));
        return sb.toString();
    }

    /**
     * Enlace {@code wa.me} que abre el chat del negocio con el mensaje escrito.
     *
     * <p>{@link URLEncoder} codifica los espacios como "+", que WhatsApp mostraría
     * literalmente; por eso se cambian por {@code %20} (RNF08). Un "+" escrito en
     * el texto ya llega como {@code %2B}, así que el reemplazo no lo afecta.</p>
     *
     * @param numero WhatsApp del negocio, solo dígitos con indicativo (ej. 573128628147)
     * @param texto  mensaje a enviar
     * @return enlace listo para abrir
     */
    public static String enlace(String numero, String texto) {
        String codificado = URLEncoder.encode(texto, StandardCharsets.UTF_8).replace("+", "%20");
        return "https://wa.me/" + numero + "?text=" + codificado;
    }

    /** @return true si el número tiene el formato que exige wa.me */
    public static boolean numeroValido(String numero) {
        return numero != null && NUMERO.matcher(numero).matches();
    }

    /** Aviso cuando no hay stock suficiente (decisión del proyecto: se puede cotizar igual). */
    private static String notaDisponibilidad(DetalleCotizacion d) {
        Integer stock = d.getStockDisponible();
        if (stock == null || d.getCantidad() <= stock) {
            return "";
        }
        if (stock == 0) {
            return " (agotado, sujeto a fabricación)";
        }
        return " (" + stock + (stock == 1 ? " disponible" : " disponibles") + "; el resto, sujeto a fabricación)";
    }
}
