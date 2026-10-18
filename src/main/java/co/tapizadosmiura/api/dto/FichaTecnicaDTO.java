package co.tapizadosmiura.api.dto;

import co.tapizadosmiura.modelo.Producto;

import java.math.BigDecimal;

/**
 * Ficha técnica de un mueble (prototipo "Detalle de producto"). Los campos
 * color, materialTapiz, materialEstructura y tipoRelleno alimentan además los
 * filtros del catálogo. Los campos vacíos se omiten.
 */
public record FichaTecnicaDTO(String estilo, String color, String materialTapiz, String materialEstructura,
                              String tipoRelleno, String tipoPata, BigDecimal altoCm, BigDecimal anchoCm,
                              BigDecimal profundidadCm, BigDecimal pesoKg, Integer resistenciaKgPuesto,
                              boolean requiereArmado) {

    public static FichaTecnicaDTO de(Producto p) {
        return new FichaTecnicaDTO(p.getEstilo(), p.getColor(), p.getMaterialTapiz(), p.getMaterialEstructura(),
                p.getTipoRelleno(), p.getTipoPata(), p.getAltoCm(), p.getAnchoCm(), p.getProfundidadCm(),
                p.getPesoKg(), p.getResistenciaKgPuesto(), p.isRequiereArmado());
    }
}
