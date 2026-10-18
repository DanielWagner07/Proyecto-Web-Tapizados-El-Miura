package co.tapizadosmiura.api.dto;

import co.tapizadosmiura.modelo.Categoria;

/**
 * Categoría del catálogo con la cantidad de productos visibles.
 * Con {@code totalProductos = 0} el front-end muestra "No hay productos disponibles" (CU02-2a).
 */
public record CategoriaDTO(int idCategoria, String nombre, String descripcion, int totalProductos) {

    public static CategoriaDTO de(Categoria c) {
        return new CategoriaDTO(c.getIdCategoria(), c.getNombre(), c.getDescripcion(), c.getTotalProductos());
    }
}
