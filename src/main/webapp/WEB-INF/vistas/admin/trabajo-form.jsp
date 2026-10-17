<%--
  CU08 - Formulario para publicar o editar un trabajo del portafolio.
  Se envía a TrabajoServlet (POST /admin/trabajos, accion=guardar) como multipart/form-data.
  Atributos: trabajo (Trabajo), errores (Map campo -> mensaje), servicios (List<Servicio>),
             maximoFotos (int).
--%>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="seccion" value="galeria" />
<c:set var="esNuevo" value="${trabajo.idTrabajo == 0}" />
<c:set var="tituloPagina" value="${esNuevo ? 'Nuevo trabajo' : 'Editar trabajo'}" />
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${tituloPagina} - Tapizados El Miura</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/admin/css/admin.css">
</head>
<body>

<%@ include file="fragmentos/cabecera.jspf" %>

<main class="contenido-admin">
    <p class="miga"><a href="${ctx}/admin/trabajos">Galería de trabajos</a> / ${tituloPagina}</p>
    <h1>${tituloPagina}</h1>

    <c:if test="${not empty errores}">
        <p class="aviso aviso-error" role="alert">Revise los campos marcados en rojo.</p>
    </c:if>

    <form action="${ctx}/admin/trabajos" method="post" enctype="multipart/form-data" class="formulario" id="formTrabajo">
        <input type="hidden" name="accion" value="guardar">
        <input type="hidden" name="idTrabajo" value="${trabajo.idTrabajo}">

        <%-- ===== Tipo de trabajo ===== --%>
        <fieldset>
            <legend>Tipo de trabajo</legend>
            <div class="opciones-tipo ${not empty errores.tipo ? 'con-error' : ''}">
                <label class="opcion-tipo">
                    <input type="radio" name="tipo" value="restauracion" ${trabajo.tipo == 'restauracion' ? 'checked' : ''}>
                    <span><strong>Restauración</strong>
                        <small>Comparativo antes y después de un mueble. Ambas fotos son obligatorias.</small></span>
                </label>
                <label class="opcion-tipo">
                    <input type="radio" name="tipo" value="colaboracion" ${trabajo.tipo == 'colaboracion' ? 'checked' : ''}>
                    <span><strong>Colaboración</strong>
                        <small>Proyecto para otro negocio (restaurante, discoteca...) con varias fotos.</small></span>
                </label>
            </div>
            <small class="error">${errores.tipo}</small>
        </fieldset>

        <%-- ===== Datos del trabajo ===== --%>
        <fieldset>
            <legend>Datos del trabajo</legend>
            <div class="rejilla">
                <div class="campo campo-ancho ${not empty errores.titulo ? 'con-error' : ''}">
                    <label for="titulo">Título *</label>
                    <input type="text" id="titulo" name="titulo" maxlength="150" required
                           placeholder="Ej. Sofá capitoné amarillo" value="${fn:escapeXml(trabajo.titulo)}">
                    <small class="error">${errores.titulo}</small>
                </div>
                <div class="campo ${not empty errores.idServicio ? 'con-error' : ''}">
                    <label for="idServicio">Servicio realizado</label>
                    <select id="idServicio" name="idServicio">
                        <option value="">Sin especificar</option>
                        <c:forEach var="s" items="${servicios}">
                            <option value="${s.idServicio}" ${s.idServicio == trabajo.idServicio ? 'selected' : ''}>
                                <c:out value="${s.nombre}" />
                            </option>
                        </c:forEach>
                    </select>
                    <small class="error">${errores.idServicio}</small>
                </div>
                <div class="campo campo-ancho ${not empty errores.cliente ? 'con-error' : ''}">
                    <label for="cliente">Cliente o negocio aliado</label>
                    <input type="text" id="cliente" name="cliente" maxlength="150"
                           placeholder="Ej. Discoteca Zona 0 (opcional en restauraciones)" value="${fn:escapeXml(trabajo.cliente)}">
                    <small class="error">${errores.cliente}</small>
                </div>
                <div class="campo campo-completo ${not empty errores.descripcion ? 'con-error' : ''}">
                    <label for="descripcion">Descripción del trabajo</label>
                    <textarea id="descripcion" name="descripcion" rows="4" maxlength="2000"
                              placeholder="Qué se hizo: cambio de tela, espuma, refuerzo de estructura...">${fn:escapeXml(trabajo.descripcion)}</textarea>
                    <small class="error">${errores.descripcion}</small>
                </div>
            </div>
        </fieldset>

        <%-- ===== Antes y después ===== --%>
        <fieldset>
            <legend>Fotos de antes y después</legend>
            <p class="ayuda">Obligatorias en restauraciones; opcionales en colaboraciones. JPG, PNG o WEBP, máximo 5 MB.</p>
            <div class="rejilla rejilla-2">
                <div class="campo-imagen ${not empty errores.imagenAntes ? 'con-error' : ''}">
                    <img class="vista-previa" alt="Foto del antes"
                         src="${ctx}/${empty trabajo.imagenAntes ? 'img/sin-imagen.svg' : trabajo.imagenAntes}">
                    <div>
                        <label for="imagenAntes">Antes ${empty trabajo.imagenAntes ? '' : '(cambiar)'}</label>
                        <input type="file" id="imagenAntes" name="imagenAntes" accept=".jpg,.jpeg,.png,.webp">
                        <small class="error">${errores.imagenAntes}</small>
                    </div>
                </div>
                <div class="campo-imagen ${not empty errores.imagenDespues ? 'con-error' : ''}">
                    <img class="vista-previa" alt="Foto del después"
                         src="${ctx}/${empty trabajo.imagenDespues ? 'img/sin-imagen.svg' : trabajo.imagenDespues}">
                    <div>
                        <label for="imagenDespues">Después ${empty trabajo.imagenDespues ? '' : '(cambiar)'}</label>
                        <input type="file" id="imagenDespues" name="imagenDespues" accept=".jpg,.jpeg,.png,.webp">
                        <small class="error">${errores.imagenDespues}</small>
                    </div>
                </div>
            </div>
        </fieldset>

        <%-- ===== Fotos adicionales ===== --%>
        <fieldset>
            <legend>Fotos adicionales</legend>
            <div class="campo ${not empty errores.fotos ? 'con-error' : ''}">
                <label for="fotos">Agregar fotos (puede elegir varias a la vez)</label>
                <input type="file" id="fotos" name="fotos" accept=".jpg,.jpeg,.png,.webp" multiple>
                <small>Tiene ${fn:length(trabajo.fotos)} de ${maximoFotos} fotos. Las nuevas se agregan al final.</small>
                <small class="error">${errores.fotos}</small>
            </div>
        </fieldset>

        <fieldset>
            <legend>Visibilidad</legend>
            <label class="campo-casilla">
                <input type="checkbox" name="estado" ${trabajo.estado ? 'checked' : ''}> Mostrar en la galería pública
            </label>
        </fieldset>

        <div class="botones">
            <a class="boton-secundario" href="${ctx}/admin/trabajos">Cancelar</a>
            <button type="submit" class="boton-principal">${esNuevo ? 'Publicar trabajo' : 'Guardar cambios'}</button>
        </div>
    </form>

    <%-- Fotos adicionales ya guardadas. Va fuera del formulario principal porque
         cada botón "Quitar" es un formulario propio (no se pueden anidar formularios). --%>
    <c:if test="${not empty trabajo.fotos}">
        <section class="fotos-guardadas">
            <h2>Fotos adicionales guardadas</h2>
            <div class="rejilla-fotos">
                <c:forEach var="foto" items="${trabajo.fotos}">
                    <figure>
                        <img src="${ctx}/${foto.ruta}" alt="<c:out value='${foto.textoAlternativo}' />">
                        <form action="${ctx}/admin/trabajos" method="post"
                              onsubmit="return confirm('¿Quitar esta foto?');">
                            <input type="hidden" name="accion" value="eliminarFoto">
                            <input type="hidden" name="id" value="${trabajo.idTrabajo}">
                            <input type="hidden" name="idFoto" value="${foto.idTrabajoImagen}">
                            <button type="submit" class="boton-enlace enlace-peligro">Quitar</button>
                        </form>
                    </figure>
                </c:forEach>
            </div>
        </section>
    </c:if>
</main>

<jsp:include page="fragmentos/pie.jsp" />

</body>
</html>
