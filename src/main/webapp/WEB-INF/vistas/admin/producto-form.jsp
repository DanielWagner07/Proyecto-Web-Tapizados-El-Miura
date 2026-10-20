<%--
  CU07 - Formulario para crear o editar un producto. Lo envía a ProductoServlet
  (POST /admin/productos, accion=guardar) como multipart/form-data porque incluye la foto.
  Atributos: producto (Producto), errores (Map campo -> mensaje), categorias (List<Categoria>).
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="seccion" value="catalogo" />
<c:set var="esNuevo" value="${producto.idProducto == 0}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${esNuevo ? 'Nuevo producto' : 'Editar producto'} - Tapizados El Miura</title>
    <link rel="icon" href="${pageContext.request.contextPath}/img/favicon.png" type="image/png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/admin/css/admin.css">
</head>
<body>

<%@ include file="fragmentos/cabecera.jspf" %>

<main class="contenido-admin">
    <p class="miga"><a href="${ctx}/admin/productos">Gestor de catálogo</a> / ${esNuevo ? 'Nuevo producto' : 'Editar producto'}</p>
    <h1>${esNuevo ? 'Nuevo producto' : 'Editar producto'}</h1>

    <c:if test="${not empty errores}">
        <p class="aviso aviso-error" role="alert">Revise los campos marcados en rojo.</p>
    </c:if>

    <form action="${ctx}/admin/productos" method="post" enctype="multipart/form-data" class="formulario formulario-producto">
        <input type="hidden" name="accion" value="guardar">
        <input type="hidden" name="idProducto" value="${producto.idProducto}">

        <%-- ===== Datos básicos (obligatorios) ===== --%>
        <fieldset>
            <legend>Datos básicos</legend>
            <div class="rejilla">
                <div class="campo ${not empty errores.referencia ? 'con-error' : ''}">
                    <label for="referencia">Referencia *</label>
                    <input type="text" id="referencia" name="referencia" maxlength="20" required
                           placeholder="Ej. 00014" value="${fn:escapeXml(producto.referencia)}">
                    <small class="error">${errores.referencia}</small>
                </div>
                <div class="campo campo-ancho ${not empty errores.nombre ? 'con-error' : ''}">
                    <label for="nombre">Nombre *</label>
                    <input type="text" id="nombre" name="nombre" maxlength="150" required
                           placeholder="Ej. Sofá Roman 3 puestos beige" value="${fn:escapeXml(producto.nombre)}">
                    <small class="error">${errores.nombre}</small>
                </div>
                <div class="campo ${not empty errores.idCategoria ? 'con-error' : ''}">
                    <label for="idCategoria">Categoría *</label>
                    <select id="idCategoria" name="idCategoria" required>
                        <option value="">Seleccione...</option>
                        <c:forEach var="cat" items="${categorias}">
                            <option value="${cat.idCategoria}" ${cat.idCategoria == producto.idCategoria ? 'selected' : ''}>
                                <c:out value="${cat.nombre}" />
                            </option>
                        </c:forEach>
                    </select>
                    <small class="error">${errores.idCategoria}</small>
                </div>
                <div class="campo ${not empty errores.precio ? 'con-error' : ''}">
                    <label for="precio">Precio (COP) *</label>
                    <input type="number" id="precio" name="precio" min="1" step="0.01" required
                           value="${producto.precio}">
                    <small class="error">${errores.precio}</small>
                </div>
                <div class="campo ${not empty errores.stock ? 'con-error' : ''}">
                    <label for="stock">Stock *</label>
                    <input type="number" id="stock" name="stock" min="0" step="1" required
                           value="${producto.stock}">
                    <small class="error">${errores.stock}</small>
                </div>
                <div class="campo campo-completo ${not empty errores.descripcion ? 'con-error' : ''}">
                    <label for="descripcion">Descripción</label>
                    <textarea id="descripcion" name="descripcion" rows="3" maxlength="2000">${fn:escapeXml(producto.descripcion)}</textarea>
                    <small class="error">${errores.descripcion}</small>
                </div>
            </div>
        </fieldset>

        <%-- ===== Ficha técnica (opcional; alimenta los filtros del catálogo) ===== --%>
        <fieldset>
            <legend>Ficha técnica</legend>
            <div class="rejilla">
                <div class="campo ${not empty errores.estilo ? 'con-error' : ''}">
                    <label for="estilo">Estilo</label>
                    <input type="text" id="estilo" name="estilo" maxlength="60" value="${fn:escapeXml(producto.estilo)}">
                    <small class="error">${errores.estilo}</small>
                </div>
                <div class="campo ${not empty errores.color ? 'con-error' : ''}">
                    <label for="color">Color</label>
                    <input type="text" id="color" name="color" maxlength="60" value="${fn:escapeXml(producto.color)}">
                    <small class="error">${errores.color}</small>
                </div>
                <div class="campo ${not empty errores.materialTapiz ? 'con-error' : ''}">
                    <label for="materialTapiz">Material del tapiz</label>
                    <input type="text" id="materialTapiz" name="materialTapiz" maxlength="80" value="${fn:escapeXml(producto.materialTapiz)}">
                    <small class="error">${errores.materialTapiz}</small>
                </div>
                <div class="campo ${not empty errores.materialEstructura ? 'con-error' : ''}">
                    <label for="materialEstructura">Estructura</label>
                    <input type="text" id="materialEstructura" name="materialEstructura" maxlength="80" value="${fn:escapeXml(producto.materialEstructura)}">
                    <small class="error">${errores.materialEstructura}</small>
                </div>
                <div class="campo ${not empty errores.tipoRelleno ? 'con-error' : ''}">
                    <label for="tipoRelleno">Relleno</label>
                    <input type="text" id="tipoRelleno" name="tipoRelleno" maxlength="80" value="${fn:escapeXml(producto.tipoRelleno)}">
                    <small class="error">${errores.tipoRelleno}</small>
                </div>
                <div class="campo ${not empty errores.tipoPata ? 'con-error' : ''}">
                    <label for="tipoPata">Tipo de pata</label>
                    <input type="text" id="tipoPata" name="tipoPata" maxlength="80" value="${fn:escapeXml(producto.tipoPata)}">
                    <small class="error">${errores.tipoPata}</small>
                </div>
            </div>
        </fieldset>

        <%-- ===== Medidas ===== --%>
        <fieldset>
            <legend>Medidas</legend>
            <div class="rejilla">
                <div class="campo ${not empty errores.altoCm ? 'con-error' : ''}">
                    <label for="altoCm">Alto (cm)</label>
                    <input type="number" id="altoCm" name="altoCm" min="0.1" step="0.1" value="${producto.altoCm}">
                    <small class="error">${errores.altoCm}</small>
                </div>
                <div class="campo ${not empty errores.anchoCm ? 'con-error' : ''}">
                    <label for="anchoCm">Ancho (cm)</label>
                    <input type="number" id="anchoCm" name="anchoCm" min="0.1" step="0.1" value="${producto.anchoCm}">
                    <small class="error">${errores.anchoCm}</small>
                </div>
                <div class="campo ${not empty errores.profundidadCm ? 'con-error' : ''}">
                    <label for="profundidadCm">Profundidad (cm)</label>
                    <input type="number" id="profundidadCm" name="profundidadCm" min="0.1" step="0.1" value="${producto.profundidadCm}">
                    <small class="error">${errores.profundidadCm}</small>
                </div>
                <div class="campo ${not empty errores.pesoKg ? 'con-error' : ''}">
                    <label for="pesoKg">Peso (kg)</label>
                    <input type="number" id="pesoKg" name="pesoKg" min="0.1" step="0.1" value="${producto.pesoKg}">
                    <small class="error">${errores.pesoKg}</small>
                </div>
                <div class="campo ${not empty errores.resistenciaKgPuesto ? 'con-error' : ''}">
                    <label for="resistenciaKgPuesto">Resistencia (kg por puesto)</label>
                    <input type="number" id="resistenciaKgPuesto" name="resistenciaKgPuesto" min="1" step="1" value="${producto.resistenciaKgPuesto}">
                    <small class="error">${errores.resistenciaKgPuesto}</small>
                </div>
                <div class="campo campo-casilla">
                    <label><input type="checkbox" name="requiereArmado" ${producto.requiereArmado ? 'checked' : ''}> Requiere armado</label>
                </div>
            </div>
        </fieldset>

        <%-- ===== Foto principal ===== --%>
        <fieldset>
            <legend>Foto principal</legend>
            <div class="campo-imagen ${not empty errores.imagen ? 'con-error' : ''}">
                <c:if test="${not empty producto.imagenPrincipal}">
                    <img class="vista-previa" src="${ctx}/${producto.imagenPrincipal}" alt="Foto actual del producto">
                </c:if>
                <div>
                    <label for="imagen">${esNuevo ? 'Imagen *' : (empty producto.imagenPrincipal ? 'Imagen (necesaria para mostrarlo en el catálogo)' : 'Cambiar imagen (opcional)')}</label>
                    <input type="file" id="imagen" name="imagen" accept=".jpg,.jpeg,.png,.webp" ${esNuevo ? 'required' : ''}>
                    <small>JPG, PNG o WEBP, máximo 5 MB.</small>
                    <small class="error">${errores.imagen}</small>
                </div>
            </div>
        </fieldset>

        <fieldset>
            <legend>Visibilidad</legend>
            <label class="campo-casilla">
                <input type="checkbox" name="estado" ${producto.estado ? 'checked' : ''}> Mostrar en el catálogo público
            </label>
        </fieldset>

        <div class="botones">
            <a class="boton-secundario" href="${ctx}/admin/productos">Cancelar</a>
            <button type="submit" class="boton-principal">${esNuevo ? 'Registrar producto' : 'Guardar cambios'}</button>
        </div>
    </form>
</main>

<jsp:include page="fragmentos/pie.jsp" />

</body>
</html>
