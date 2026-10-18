package co.tapizadosmiura.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Formato de pesos colombianos para los textos que lee el cliente,
 * por ejemplo {@code 1999999.00 → "$1.999.999"} (punto de miles, sin centavos).
 */
public final class FormatoMoneda {

    private FormatoMoneda() {
    }

    /**
     * @param valor cantidad en pesos (null se trata como 0)
     * @return texto con signo de pesos y punto de miles
     */
    public static String pesos(BigDecimal valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        // DecimalFormat no es seguro entre hilos: se crea uno por llamada.
        DecimalFormat formato = new DecimalFormat("#,##0", simbolos);
        BigDecimal redondeado = (valor == null ? BigDecimal.ZERO : valor).setScale(0, RoundingMode.HALF_UP);
        return "$" + formato.format(redondeado);
    }
}
