package co.tapizadosmiura.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Configuración única de Gson para toda la API.
 *
 * <ul>
 *   <li>Los campos {@code null} no se escriben: cada respuesta muestra solo lo que aplica.</li>
 *   <li>Las fechas ({@link LocalDateTime}) salen como texto ISO-8601, por ejemplo
 *       {@code "2026-10-16T00:48:28"}. Gson no sabe convertirlas por sí solo en Java 17+.</li>
 *   <li>No se escapan caracteres HTML: un enlace como {@code https://wa.me/57...?text=Hola}
 *       sale legible y no como {@code =}.</li>
 * </ul>
 */
public final class Json {

    /** Instancia compartida: Gson es seguro para usarse desde varios hilos. */
    public static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(LocalDateTime.class, new FechaHoraAdapter())
            .create();

    private Json() {
    }

    /** Convierte {@link LocalDateTime} a texto ISO-8601 y de vuelta. */
    private static final class FechaHoraAdapter extends TypeAdapter<LocalDateTime> {
        @Override
        public void write(JsonWriter salida, LocalDateTime valor) throws IOException {
            if (valor == null) {
                salida.nullValue();
            } else {
                salida.value(valor.toString());
            }
        }

        @Override
        public LocalDateTime read(JsonReader entrada) throws IOException {
            return LocalDateTime.parse(entrada.nextString());
        }
    }
}
