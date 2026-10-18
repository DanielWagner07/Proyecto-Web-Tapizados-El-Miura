package co.tapizadosmiura.api.dto;

import co.tapizadosmiura.modelo.Servicio;

/** Servicio que ofrece el taller (Tapizado, Restauración, Fabricación a medida). */
public record ServicioDTO(int idServicio, String nombre, String descripcion) {

    public static ServicioDTO de(Servicio s) {
        return new ServicioDTO(s.getIdServicio(), s.getNombre(), s.getDescripcion());
    }
}
