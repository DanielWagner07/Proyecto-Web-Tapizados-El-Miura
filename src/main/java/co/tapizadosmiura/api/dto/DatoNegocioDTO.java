package co.tapizadosmiura.api.dto;

import co.tapizadosmiura.modelo.InformacionNegocio;

/**
 * Un dato de la información del negocio. La API los entrega en un objeto
 * indexado por clave: {@code {"whatsapp": {"etiqueta": ..., "valor": ..., "grupo": ...}}},
 * así el front-end lee directamente {@code info.whatsapp.valor}.
 */
public record DatoNegocioDTO(String etiqueta, String valor, String grupo) {

    public static DatoNegocioDTO de(InformacionNegocio dato) {
        return new DatoNegocioDTO(dato.getEtiqueta(), dato.getValor(), dato.getGrupo());
    }
}
